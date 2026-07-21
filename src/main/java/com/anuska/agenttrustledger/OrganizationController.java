package com.anuska.agenttrustledger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationRepository repository;

    // Sign up a new organization and get back an API key.
    // The API key is only ever shown in this response, so save it immediately.
    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody Organization request) {
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Organization name is required.");
        }

        if (repository.findByName(request.getName()).isPresent()) {
            return ResponseEntity.status(409).body("An organization with this name already exists.");
        }

        String apiKey = UUID.randomUUID().toString();
        Organization org = new Organization(request.getName(), apiKey);
        Organization saved = repository.save(org);

        return ResponseEntity.ok(saved);
    }
}
