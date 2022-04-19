package com.futurewei.arionmaster.grpc;

import com.futurewei.arion.schema.Common;
import com.futurewei.arion.schema.GoalStateProvisionerGrpc;
import com.futurewei.arion.schema.Goalstate;
import com.futurewei.arion.schema.Goalstateprovisioner;
import com.futurewei.arionmaster.controller.NeighborStateController;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.logging.Level;

@GrpcService
public class GrpcServerService extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerService.class);

    @Autowired
    private GoalStatePersistenceService goalStateProcess;

    @Override
    public void pushGoalstates(Goalstateprovisioner.RoutingRulesRequest request, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
        try {
            goalStateProcess.goalstateProcess(request);

            var status = Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus.newBuilder()
                    .setOperationStatus(Common.OperationStatus.SUCCESS)
                    .build();
            Goalstateprovisioner.GoalStateOperationReply reply = Goalstateprovisioner
                    .GoalStateOperationReply
                    .newBuilder()
                    .addOperationStatuses(status)
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }

    }

    @Override
    public void pushNetworkResourceStates(Goalstate.GoalState request, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Goalstate.GoalStateV2> pushGoalStatesStream(final StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {

        return new StreamObserver<Goalstate.GoalStateV2>() {
            @Override
            public void onNext(Goalstate.GoalStateV2 value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }
}
