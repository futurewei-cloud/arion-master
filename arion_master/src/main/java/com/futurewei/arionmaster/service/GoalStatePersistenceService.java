package com.futurewei.arionmaster.service;

import com.futurewei.arion.schema.Goalstateprovisioner;

public interface GoalStatePersistenceService {
    void goalstateProcess(Goalstateprovisioner.RoutingRulesRequest routingRulesRequest) throws Exception;
}
