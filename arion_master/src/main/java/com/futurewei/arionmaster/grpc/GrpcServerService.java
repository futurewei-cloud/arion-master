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

import com.futurewei.alcor.schema.*;
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
public class GrpcServerService extends ArionMasterServiceGrpc.ArionMasterServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerService.class);

    @Autowired
    private GoalStatePersistenceService goalStateProcess;

    @Override
    public void pushGoalstates(Arionmaster.NeighborRulesRequest request, StreamObserver<Goalstateprovisioner.GoalStateOperationReply> responseObserver) {
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
    public void requestGoalstates (Goalstateprovisioner.HostRequest request, StreamObserver<Arionmaster.NeighborRulesResponse> responseObserver) {
        try {
            var neighborRuleResponse = goalStateProcess.getNeighborRules(request);
            responseObserver.onNext(neighborRuleResponse);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.info(e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public StreamObserver<Goalstateprovisioner.HostRequest> requestGoalstateStream (StreamObserver<Arionmaster.NeighborRulesResponse> responseObserver) {
        return new StreamObserver<Goalstateprovisioner.HostRequest>() {
            @Override
            public void onNext(Goalstateprovisioner.HostRequest hostRequest) {
                CompletableFuture future = CompletableFuture.supplyAsync(() -> {
                    try {
                        goalStateProcess.getNeighborRulesResponse(hostRequest, r -> {
                            synchronized (responseObserver) {
                                responseObserver.onNext(r);
                            }
                        });
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                    return null;

                }, AsyncExecutor.executor);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.info(throwable.getMessage());
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}




