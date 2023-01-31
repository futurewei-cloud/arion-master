package com.futurewei.benchmark.service;

import com.futurewei.alcor.schema.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ArionmasterSecurityGroupTest {

    @GrpcClient("local-grpc-server")
    private GoalStateProvisionerGrpc.GoalStateProvisionerStub stub;

    @Value("${arion.securitygroup.portnumber:1}")
    private int portNumber;

    @Value("${arion.securitygroup.securitygroupnumber:1}")
    private int securityGroupNumber;

    @Value("${arion.securitygroup.securitygrouprulesnumber:1}")
    private int securityGroupRulesNumber;

    public void insertSecurityGroup () {
        for (int i = 0; i < securityGroupNumber; i++) {
            String securityGroupId = UUID.randomUUID().toString();
            SecurityGroup.SecurityGroupState securityGroupState = getSecurityGroupState(securityGroupId);
            System.out.println("SecurityGroupId: " + securityGroupId);
            stub.pushSecurityGroupGoalState(securityGroupState, new StreamObserver<Goalstateprovisioner.GoalStateOperationReply>() {
                @Override
                public void onNext(Goalstateprovisioner.GoalStateOperationReply goalStateOperationReply) {
                    System.out.println("SecurityGroupRule: " + goalStateOperationReply);
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onCompleted() {

                }
            });
            for (int j = 0; j < portNumber; j++) {
                String portId = UUID.randomUUID().toString();
                Port.PortState portState = getPortState(portId, securityGroupId);
                List<Port.PortState> portStateList = new ArrayList<>();
                portStateList.add(portState);
                Goalstateprovisioner.ArionGoalStateRequest.Builder arionGoalStateRequestBuilder = Goalstateprovisioner.ArionGoalStateRequest.newBuilder();
                arionGoalStateRequestBuilder.addAllPortstates(portStateList);
                System.out.println("PortId: " + portId);
                stub.pushGoalstates(arionGoalStateRequestBuilder.build(), new StreamObserver<Goalstateprovisioner.GoalStateOperationReply>() {
                    @Override
                    public void onNext(Goalstateprovisioner.GoalStateOperationReply goalStateOperationReply) {
                        System.out.println("SecurityGroupPortBinding: " + goalStateOperationReply);
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                });

            }
        }
    }

    private SecurityGroup.SecurityGroupState getSecurityGroupState (String securitygroupid) {
        SecurityGroup.SecurityGroupState.Builder securityGroupBuilder = SecurityGroup.SecurityGroupState.newBuilder();
        securityGroupBuilder.setOperationType(Common.OperationType.CREATE);
        SecurityGroup.SecurityGroupConfiguration.Builder securityGroupConfigurationBuilder = SecurityGroup.SecurityGroupConfiguration.newBuilder();
        securityGroupConfigurationBuilder.setVpcId("888");
        for (int i = 0; i < securityGroupRulesNumber; i++) {
            SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.Builder securityGroupRuleBuilder = SecurityGroup.SecurityGroupConfiguration.SecurityGroupRule.newBuilder();
            securityGroupRuleBuilder.setOperationType(Common.OperationType.CREATE);
            securityGroupRuleBuilder.setSecurityGroupId(securitygroupid);
            securityGroupRuleBuilder.setId(UUID.randomUUID().toString());
            securityGroupRuleBuilder.setDirection(SecurityGroup.SecurityGroupConfiguration.Direction.INGRESS);
            securityGroupRuleBuilder.setEthertype(Common.EtherType.IPV4);
            securityGroupRuleBuilder.setProtocol(Common.Protocol.TCP);
            securityGroupRuleBuilder.setPortRangeMax(30016);
            securityGroupRuleBuilder.setPortRangeMin(30001);
            securityGroupRuleBuilder.setRemoteIpPrefix("192.168.0.0/16");
            securityGroupRuleBuilder.setRemoteGroupId("");
            securityGroupConfigurationBuilder.addSecurityGroupRules(securityGroupRuleBuilder);
        }
        securityGroupBuilder.setConfiguration(securityGroupConfigurationBuilder);
        return securityGroupBuilder.build();
    }

    private Port.PortState getPortState(String portid, String securityGroupId) {
        Port.PortState.Builder portStateBuilder = Port.PortState.newBuilder();
        Port.PortConfiguration.Builder portConfigurationBuilder = Port.PortConfiguration.newBuilder();
        portConfigurationBuilder.setId(portid);
        Port.PortConfiguration.SecurityGroupId.Builder securityGroupIdBuilder = Port.PortConfiguration.SecurityGroupId.newBuilder();
        securityGroupIdBuilder.setId(securityGroupId);
        portConfigurationBuilder.addSecurityGroupIds(securityGroupIdBuilder.build());
        portStateBuilder.setConfiguration(portConfigurationBuilder);
        return portStateBuilder.build();
    }

}
