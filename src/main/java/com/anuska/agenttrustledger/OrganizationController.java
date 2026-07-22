package com.anuska.agenttrustledger;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationRepository repository;

    public static class SignupRequest {
        public String name;
        public String password;
    }

    // Sign up a new organization with a name and a password they choose.
    // The password is hashed before storage — never saved or returned in plain text.
    @PostMapping
    public ResponseEntity<?> createOrganization(@RequestBody SignupRequest request) {
        if (request.name == null || request.name.isBlank()) {
            return ResponseEntity.badRequest().body("Organization name is required.");
        }
        if (request.password == null || request.password.length() < 4) {
            return ResponseEntity.badRequest().body("Password must be at least 4 characters.");
        }
        if (repository.findByName(request.name).isPresent()) {
            return ResponseEntity.status(409).body("An organization with this name already exists.");
        }

        String hashed = BCrypt.hashpw(request.password, BCrypt.gensalt());
        Organization org = new Organization(request.name, hashed);
        Organization saved = repository.save(org);

        // Return only the id and name — never the password or its hash.
        return ResponseEntity.ok(new Object() {
            public final Long id = saved.getId();
            public final String name = saved.getName();
        });
    }
}
