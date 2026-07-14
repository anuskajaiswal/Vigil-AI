package com.anuska.agenttrustledger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/events")
public class AgentEventController {

    @Autowired
    private AgentEventRepository repository;

    // Log a new event
    @PostMapping
    public AgentEvent logEvent(@RequestBody AgentEvent event) {
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        // Get this agent's past events
        List<AgentEvent> pastEvents = repository.findByAgentName(event.getAgentName());

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

        return repository.save(event);
    }

    // Get all events
    @GetMapping
    public List<AgentEvent> getAllEvents() {
        return repository.findAll();
    }

    // Get events for one specific agent
    @GetMapping("/agent/{agentName}")
    public List<AgentEvent> getEventsByAgent(@PathVariable String agentName) {
        return repository.findByAgentName(agentName);
    }

    // Get only flagged (suspicious) events
    @GetMapping("/flagged")
    public List<AgentEvent> getFlaggedEvents() {
        return repository.findByFlaggedTrue();
    }
}