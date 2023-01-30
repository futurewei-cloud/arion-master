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

import com.futurewei.alcor.schema.SecurityGroup;
import com.futurewei.arion.schema.Arionmaster;
import com.futurewei.arion.schema.Common;
import com.futurewei.arionmaster.data.SecurityGroupPortBindingRepository;
import com.futurewei.arionmaster.data.SecurityGroupRuleRepository;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.arionmaster.data.NeighborStateRepository;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import com.futurewei.arionmaster.version.VersionManager;
import com.futurewei.common.model.SecurityGroupPortBinding;
import com.futurewei.common.model.SecurityGroupRule;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.map.QueryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Service
public class GoalStatePersistenceServiceImpl implements GoalStatePersistenceService {

    private static final Logger logger = LoggerFactory.getLogger(GoalStatePersistenceServiceImpl.class);

    @Autowired
    private VersionManager versionManager;

    @Autowired
    private NeighborStateRepository neighborStateRepository;

    @Autowired
    private SecurityGroupPortBindingRepository securityGroupPortBindingRepository;

    @Autowired
    private SecurityGroupRuleRepository securityGroupRuleRepository;

    @Autowired
    private QueryCache neighborQueryCache;

    @Override
    public void goalstateProcess(Goalstateprovisioner.ArionGoalStateRequest arionGoalStateRequest) throws Exception {
        var neighborStates = getNeighborStateList(arionGoalStateRequest);
        if (neighborStates.f0().size() > 0) updateNeighborState(neighborStates.f0());
        if (neighborStates.f1().size() > 0) deleteNeighborState(neighborStates.f1());
        var securityGroupPortBinding = getSecurityGroupPortBinding(arionGoalStateRequest);
        if (securityGroupPortBinding.f0().size() > 0) updateSecurityGroupPortBinding(securityGroupPortBinding.f0());
        if (securityGroupPortBinding.f1().size() > 0) deleteSecurityGroupPortBinding(securityGroupPortBinding.f1());
    }

    @Override
    public void goalstateProcess(SecurityGroup.SecurityGroupState securityGroupState) throws Exception {
        var securityGroupStates = getSecurityGroupRules(securityGroupState);
        if (securityGroupStates.f0().size() > 0) updateSecurityGroupState(securityGroupStates.f0());
        if (securityGroupStates.f1().size() > 0) deleteSecurityGroupState(securityGroupStates.f1());
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
            if (neighborQueryCache.containsKey(key)) {
                neighborRule = (NeighborRule) neighborQueryCache.get(key);
            } else {
                neighborRule = neighborStateRepository.findById(key).get();
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

    private Tuple2<List<NeighborRule>, List<NeighborRule>> getNeighborStateList(Goalstateprovisioner.ArionGoalStateRequest arionGoalStateRequest) throws Exception{
        List<NeighborRule> neighborStateUpdateList = new ArrayList<>();
        List<NeighborRule> neighborStateDeleteList = new ArrayList<>();
        arionGoalStateRequest.getNeigborstatesList()
                .forEach(neighborState -> {
                    neighborState.getConfiguration().getFixedIpsList().forEach(fixedIp -> {
                        var version = versionManager.getVersion(fixedIp.getArionGroup());
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
            neighborStateRepository.saveAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteNeighborState (List<NeighborRule> neighborStateList) {
        try {
            neighborStateRepository.deleteAll(neighborStateList);
        } catch (Exception e) {
            throw e;
        }
    }

    private Tuple2<List<SecurityGroupPortBinding>, List<SecurityGroupPortBinding>> getSecurityGroupPortBinding(Goalstateprovisioner.ArionGoalStateRequest arionGoalStateRequest) throws Exception{
        List<SecurityGroupPortBinding> securityGroupPortBindingList = new ArrayList<>();
        List<SecurityGroupPortBinding> securityGroupPortBindingList1 = new ArrayList<>();
        arionGoalStateRequest.getPortstatesList()
                .forEach(portState -> {
                    portState.getConfiguration().getSecurityGroupIdsList().forEach(securityGroupId -> {
                        var version = versionManager.getVersion("securitygroup");
                        SecurityGroupPortBinding securityGroupPortBinding = new SecurityGroupPortBinding();
                        securityGroupPortBinding.setId(String.join("-", portState.getConfiguration().getId(), securityGroupId.getId()));
                        securityGroupPortBinding.setSecurityGroupId(securityGroupId.getId());
                        securityGroupPortBinding.setPortId(portState.getConfiguration().getId());
                        securityGroupPortBinding.setVersion(version);
                        if (portState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.CREATE) ||
                                portState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.UPDATE) ||
                                portState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.INFO)
                        ) {
                            securityGroupPortBindingList.add(securityGroupPortBinding);
                        } else if (portState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.DELETE)) {
                            securityGroupPortBindingList1.add(securityGroupPortBinding);
                        }
                    });
                });
        return Tuple2.tuple2(securityGroupPortBindingList, securityGroupPortBindingList1);
    }

    private void updateSecurityGroupPortBinding (List<SecurityGroupPortBinding> securityGroupPortBindingList) {
        try {
            securityGroupPortBindingRepository.saveAll(securityGroupPortBindingList);
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteSecurityGroupPortBinding (List<SecurityGroupPortBinding> securityGroupPortBindingList) {
        try {
            securityGroupPortBindingRepository.deleteAll(securityGroupPortBindingList);
        } catch (Exception e) {
            throw e;
        }
    }

    private Tuple2<List<SecurityGroupRule>, List<SecurityGroupRule>> getSecurityGroupRules(SecurityGroup.SecurityGroupState securityGroupState) throws Exception{
        List<SecurityGroupRule> securityGroupStateUpdateList = new ArrayList<>();
        List<SecurityGroupRule> securityGroupStateDeleteList = new ArrayList<>();
        securityGroupState.getConfiguration().getSecurityGroupRulesList()
                        .forEach(securityGroupRule -> {
                            var version = versionManager.getVersion("securitygrouprule");
                            SecurityGroupRule securityGroupRule1 = new SecurityGroupRule();
                            securityGroupRule1.setSecurityGroupId(securityGroupRule.getSecurityGroupId());
                            securityGroupRule1.setRemoteGroupId(securityGroupRule.getRemoteGroupId());
                            securityGroupRule1.setDirection(securityGroupRule.getDirection().toString());
                            securityGroupRule1.setRemoteIpPrefix(securityGroupRule.getRemoteIpPrefix());
                            securityGroupRule1.setProtocol(securityGroupRule.getProtocol().toString());
                            securityGroupRule1.setPortRangeMax(securityGroupRule.getPortRangeMax());
                            securityGroupRule1.setPortRangeMin(securityGroupRule.getPortRangeMin());
                            securityGroupRule1.setEthertype(securityGroupRule.getEthertype().toString());
                            securityGroupRule1.setVni(Integer.parseInt(securityGroupState.getConfiguration().getVpcId()));
                            securityGroupRule1.setVersion(version);
                            if (securityGroupState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.CREATE)   ||
                                    securityGroupState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.INFO) ||
                                    securityGroupState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.UPDATE)
                            ) {
                                securityGroupStateUpdateList.add(securityGroupRule1);
                            } else if (securityGroupState.getOperationType().equals(com.futurewei.alcor.schema.Common.OperationType.DELETE)) {
                                securityGroupStateDeleteList.add(securityGroupRule1);
                            }
                        });

        return Tuple2.tuple2(securityGroupStateUpdateList, securityGroupStateDeleteList);
    }

    private void updateSecurityGroupState (List<SecurityGroupRule> securityGroupRuleList) {
        try {
            securityGroupRuleRepository.saveAll(securityGroupRuleList);
        } catch (Exception e) {
            throw e;
        }
    }

    private void deleteSecurityGroupState (List<SecurityGroupRule> securityGroupRuleList) {
        try {
            securityGroupRuleRepository.deleteAll(securityGroupRuleList);
        } catch (Exception e) {
            throw e;
        }
    }
}
