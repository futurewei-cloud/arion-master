/*
MIT License
Copyright(c) 2022 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.arionmaster.service.impl;

import com.futurewei.arion.schema.Arionmaster;
import com.futurewei.arion.schema.Common;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import com.futurewei.arionmaster.version.VersionManager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.map.QueryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


@Service
public class GoalStatePersistenceServiceImpl implements GoalStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(GoalStatePersistenceServiceImpl.class);

    @Autowired
    private VersionManager versionManager;

    @Autowired
    private NeighborStateRepository repository;

    @Autowired
    private QueryCache queryCache;

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Override
    public void goalstateProcess(Goalstateprovisioner.NeighborRulesRequest neighborRulesRequest) throws Exception {

        var version = versionManager.getVersion();
        var neighborStates = getNeighborStateList(neighborRulesRequest, version);
        if (neighborStates.f0().size() > 0) updateNeighborState(neighborStates.f0());
        if (neighborStates.f1().size() > 0) deleteNeighborState(neighborStates.f1());
    }

    @Override
    public void getNeighborRulesResponse(Arionmaster.HostRequest hostRequest, Consumer<Arionmaster.NeighborRulesResponse> resConsumer) {
        resConsumer.accept(getNeighborRules(hostRequest));
    }

    @Override
    public Arionmaster.NeighborRulesResponse getNeighborRules (Arionmaster.HostRequest hostRequest) {
        Arionmaster.NeighborRulesResponse.Builder neighborRuleResponse = Arionmaster.NeighborRulesResponse.newBuilder();
        for (Arionmaster.HostRequest.ResourceStateRequest resourceStateRequest : hostRequest.getStateRequestsList()) {
            Arionmaster.NeighborRule.Builder neighborRuleBuilder = Arionmaster.NeighborRule.newBuilder();
            Arionmaster.NeighborRulesResponse.NeighborRuleReply.Builder neighborRuleReplyBuilder = Arionmaster.NeighborRulesResponse.NeighborRuleReply.newBuilder();
            var vni = resourceStateRequest.getTunnelId();
            var neighborIp = resourceStateRequest.getDestinationIp();
            NeighborRule neighborRule;
            var key = String.join("-", String.valueOf(vni), neighborIp);
            if (queryCache.containsKey(key)) {
                neighborRule = (NeighborRule) queryCache.get(key);
            } else {
                neighborRule = repository.findById(key).get();
            }
            neighborRuleBuilder.setOperationType(Common.OperationType.INFO);
            neighborRuleBuilder.setIp(neighborRule.getIp());
            neighborRuleBuilder.setMac(neighborRule.getMac());
            neighborRuleBuilder.setArionwingGroup(neighborRule.getArionGroup());
            neighborRuleBuilder.setHostmac(neighborRule.getHostMac());
            neighborRuleBuilder.setHostip(neighborRule.getHostIp());
            neighborRuleBuilder.setTunnelId(neighborRule.getVni());
            neighborRuleBuilder.setVersion(neighborRule.getVersion());
            neighborRuleReplyBuilder.setNeighborrule(neighborRuleBuilder.build());
            neighborRuleReplyBuilder.setRequestId(resourceStateRequest.getRequestId());
            neighborRuleResponse.addNeighborrules(neighborRuleReplyBuilder.build());
        }
        return neighborRuleResponse.build();
    }

    private Tuple2<List<NeighborRule>, List<NeighborRule>> getNeighborStateList(Goalstateprovisioner.NeighborRulesRequest neighborRulesRequest, long version) throws Exception{
        List<NeighborRule> neighborStateUpdateList = new ArrayList<>();
        List<NeighborRule> neighborStateDeleteList = new ArrayList<>();
        neighborRulesRequest.getNeigborstatesList()
                .forEach(neighborState -> {
                    neighborState.getConfiguration().getFixedIpsList().forEach(fixedIp -> {
                        NeighborRule neighborRule = new NeighborRule(
                                String.join("-", String.valueOf(fixedIp.getTunnelId()), fixedIp.getIpAddress()),
                                neighborState.getConfiguration().getMacAddress(),
                                fixedIp.getArionGroup(),
                                neighborState.getConfiguration().getHostMacAddress(),
                                neighborState.getConfiguration().getHostIpAddress(),
                                fixedIp.getIpAddress(),
                                fixedIp.getTunnelId(),
                                version
                        );
                        if (neighborState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.CREATE)   ||
                                neighborState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.INFO) ||
                                neighborState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.UPDATE)
                        ) {
                            neighborStateUpdateList.add(neighborRule);
                        } else if (neighborState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.DELETE)) {
                            neighborStateDeleteList.add(neighborRule);
                        }
                    });
                });
        return Tuple2.tuple2(neighborStateUpdateList, neighborStateDeleteList);
    }

    private void updateNeighborState (List<NeighborRule> neighborStateList) {
        try {
            repository.saveAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteNeighborState (List<NeighborRule> neighborStateList) {
        try {
            repository.deleteAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }
}
