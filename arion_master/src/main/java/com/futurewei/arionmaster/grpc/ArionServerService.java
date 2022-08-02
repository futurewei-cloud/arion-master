/*
 *
 * Copyright 2015 gRPC authors.
 * Copyright 2022 The Arion Authors - file modified.
 * @author Dahai Liu (@dliu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.futurewei.arionmaster.grpc;

import com.futurewei.arion.schema.ArionMasterServiceGrpc;
import com.futurewei.arion.schema.Arionmaster;
import com.futurewei.arionmaster.service.GoalStatePersistenceService;
import com.futurewei.common.executor.AsyncExecutor;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@GrpcService
public class ArionServerService extends ArionMasterServiceGrpc.ArionMasterServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(ArionServerService.class);

    @Autowired
    private GoalStatePersistenceService goalStateProcess;


    @Override
    public void requestGoalstates (Arionmaster.HostRequest request, StreamObserver<Arionmaster.NeighborRulesResponse> responseObserver) {
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
    public StreamObserver<Arionmaster.HostRequest> requestGoalstateStream (StreamObserver<Arionmaster.NeighborRulesResponse> responseObserver) {
        return new StreamObserver<Arionmaster.HostRequest>() {
            @Override
            public void onNext(Arionmaster.HostRequest hostRequest) {
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
