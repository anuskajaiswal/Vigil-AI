package com.anuska.agenttrustledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentEventRepository extends JpaRepository<AgentEvent, Long> {
    List<AgentEvent> findByAgentName(String agentName);
    List<AgentEvent> findByFlaggedTrue();
}