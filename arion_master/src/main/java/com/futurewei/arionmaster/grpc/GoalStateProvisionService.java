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
package com.futurewei.arionmaster.grpc;


import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.alcor.schema.SecurityGroup;
import com.futurewei.arion.schema.ArionMasterServiceGrpc;
import com.futurewei.arion.schema.Arionmaster;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import com.futurewei.common.executor.AsyncExecutor;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;

@GrpcService
public class GoalStateProvisionService extends GoalStateProvisionerGrpc.GoalStateProvisionerImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GoalStateProvisionService.class);

    @Autowired
    private GoalStatePersistenceService goalStateProcess;

    @Override
    public void pushGoalstates(Goalstateprovisioner.ArionGoalStateRequest request, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
        try {
            goalStateProcess.goalstateProcess(request);
            System.out.println("SecurityGroupPortBinding: " + request);
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
    public void pushSecurityGroupGoalState(SecurityGroup.SecurityGroupState securityGroupState, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
        try {
            goalStateProcess.goalstateProcess(securityGroupState);
            System.out.println("SecurityGroupRule: " + securityGroupState);
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
}




