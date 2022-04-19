package com.futurewei.arionmaster.service.impl;

import com.futurewei.arion.schema.Common;
import com.futurewei.arion.schema.Goalstateprovisioner;
import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.arionmaster.model.RoutingRule;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import com.futurewei.arionmaster.version.VersionManager;
import com.hazelcast.jet.datamodel.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GoalStatePersistenceServiceImpl implements GoalStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(GoalStatePersistenceServiceImpl.class);

    @Autowired
    private VersionManager versionManager;

    @Autowired
    private NeighborStateRepository repository;

    public void goalstateProcess(Goalstateprovisioner.RoutingRulesRequest routingRulesRequest) throws Exception {

        var version = versionManager.getVersion();
        var neighborStates = getNeighborStateList(routingRulesRequest, version);
        if (neighborStates.f0().size() > 0) updateNeighborState(neighborStates.f0());
        if (neighborStates.f1().size() > 0) deleteNeighborState(neighborStates.f1());
    }

    private Tuple2<List<RoutingRule>, List<RoutingRule>> getNeighborStateList(Goalstateprovisioner.RoutingRulesRequest routingRulesRequest, long version) throws Exception{
        List<RoutingRule> neighborStateUpdateList = new ArrayList<>();
        List<RoutingRule> neighborStateDeleteList = new ArrayList<>();
        routingRulesRequest.getRoutingRulesList().forEach(routingRule -> {
            RoutingRule neighbor = new RoutingRule(
                    String.join("/", String.valueOf(routingRule.getTunnelId()), routingRule.getIp()),
                    routingRule.getMac(),
                    routingRule.getHostip(),
                    routingRule.getIp(),
                    routingRule.getTunnelId(),
                    version
            );

            if (routingRule.getOperationType().equals(Common.OperationType.CREATE)   ||
                    routingRule.getOperationType().equals(Common.OperationType.INFO) ||
                    routingRule.getOperationType().equals(Common.OperationType.UPDATE)
            ) {
                neighborStateUpdateList.add(neighbor);
            } else if (routingRule.getOperationType().equals(Common.OperationType.DELETE)) {
                neighborStateDeleteList.add(neighbor);
            }
        });
        return Tuple2.tuple2(neighborStateUpdateList, neighborStateDeleteList);
    }

    private void updateNeighborState (List<RoutingRule> neighborStateList) {
        try {
            repository.saveAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteNeighborState (List<RoutingRule> neighborStateList) {
        try {
            repository.deleteAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }
}
