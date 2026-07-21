package com.anuska.agenttrustledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentEventRepository extends JpaRepository<AgentEvent, Long> {
    List<AgentEvent> findByAgentName(String agentName);
    List<AgentEvent> findByFlaggedTrue();

    // Scoped to a single organization, so one company's agents are never
    // compared against or mixed with another company's data.
    List<AgentEvent> findByOrganizationIdAndAgentName(Long organizationId, String agentName);
    List<AgentEvent> findByOrganizationId(Long organizationId);
    List<AgentEvent> findByOrganizationIdAndFlaggedTrue(Long organizationId);
}
