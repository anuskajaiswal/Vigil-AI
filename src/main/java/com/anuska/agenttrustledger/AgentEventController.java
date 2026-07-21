package com.anuska.agenttrustledger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/events")
public class AgentEventController {

    @Autowired
    private AgentEventRepository repository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private static final String PUBLIC_DEMO_ORG_NAME = "Public Demo";

    // Resolves which organization this request belongs to.
    // - If a valid API key is provided, use that organization.
    // - If no API key is provided, fall back to a shared "Public Demo" org
    //   (this keeps the public dashboard working with no key required).
    // Returns null if an API key WAS provided but doesn't match anything.
    private Organization resolveOrganization(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return organizationRepository.findByName(PUBLIC_DEMO_ORG_NAME)
                    .orElseGet(() -> organizationRepository.save(
                            new Organization(PUBLIC_DEMO_ORG_NAME, "public-demo-no-key-required")));
        }
        return organizationRepository.findByApiKey(apiKey).orElse(null);
    }

    // Log a new event
    @PostMapping
    public ResponseEntity<?> logEvent(@RequestBody AgentEvent event,
                                       @RequestHeader(value = "X-API-Key", required = false) String apiKey) {

        Organization org = resolveOrganization(apiKey);
        if (org == null) {
            return ResponseEntity.status(401).body("Invalid API key.");
        }

        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        event.setOrganizationId(org.getId());

        // Get this agent's past events, scoped to this organization only
        List<AgentEvent> pastEvents = repository.findByOrganizationIdAndAgentName(org.getId(), event.getAgentName());

        // Check if this action has ever happened before for this agent
        boolean actionSeenBefore = pastEvents.stream()
                .anyMatch(e -> e.getAction().equals(event.getAction()));

        // Check if this resource has ever been touched before for this agent
        boolean resourceSeenBefore = pastEvents.stream()
                .anyMatch(e -> e.getResource().equals(event.getResource()));

        if (!pastEvents.isEmpty() && (!actionSeenBefore || !resourceSeenBefore)) {
            event.setFlagged(true);

            StringBuilder reason = new StringBuilder();

            if (!actionSeenBefore) {
                String pastActions = pastEvents.stream()
                        .map(AgentEvent::getAction)
                        .distinct()
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none");
                reason.append("This agent has never performed '").append(event.getAction())
                        .append("' before. Past actions: ").append(pastActions).append(". ");
            }

            if (!resourceSeenBefore) {
                String pastResources = pastEvents.stream()
                        .map(AgentEvent::getResource)
                        .distinct()
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none");
                reason.append("This agent has never accessed '").append(event.getResource())
                        .append("' before. Past resources: ").append(pastResources).append(".");
            }

            event.setExplanation(reason.toString().trim());
        }

        return ResponseEntity.ok(repository.save(event));
    }

    // Get all events for the caller's organization
    @GetMapping
    public ResponseEntity<?> getAllEvents(@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        Organization org = resolveOrganization(apiKey);
        if (org == null) {
            return ResponseEntity.status(401).body("Invalid API key.");
        }
        return ResponseEntity.ok(repository.findByOrganizationId(org.getId()));
    }

    // Get events for one specific agent, within the caller's organization
    @GetMapping("/agent/{agentName}")
    public ResponseEntity<?> getEventsByAgent(@PathVariable String agentName,
                                               @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        Organization org = resolveOrganization(apiKey);
        if (org == null) {
            return ResponseEntity.status(401).body("Invalid API key.");
        }
        return ResponseEntity.ok(repository.findByOrganizationIdAndAgentName(org.getId(), agentName));
    }

    // Get only flagged (suspicious) events, within the caller's organization
    @GetMapping("/flagged")
    public ResponseEntity<?> getFlaggedEvents(@RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        Organization org = resolveOrganization(apiKey);
        if (org == null) {
            return ResponseEntity.status(401).body("Invalid API key.");
        }
        return ResponseEntity.ok(repository.findByOrganizationIdAndFlaggedTrue(org.getId()));
    }
}
