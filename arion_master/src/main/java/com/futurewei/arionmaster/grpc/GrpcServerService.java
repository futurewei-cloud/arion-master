/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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
            logger.info(e.getMessage());
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
