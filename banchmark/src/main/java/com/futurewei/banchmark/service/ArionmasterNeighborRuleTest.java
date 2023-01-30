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
package com.futurewei.banchmark.service;

import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.GoalStateProvisionerGrpc;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.alcor.schema.Neighbor;
import com.futurewei.banchmark.utils.ThreadPoolExecutorUtils;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ArionmasterNeighborRuleTest {

    private static final Logger logger = LoggerFactory.getLogger(ArionmasterNeighborRuleTest.class);

    @Value("${alcor.vpc.size:1}")
    private int vpcSize;

    @Value("${alcor.subnet.size:1}")
    private int subnetSize;

    @Value("${alcor.port.size:1}")
    private int portSize;

    @Value("${alcor.neighbor.size:1}")
    private int neighborSize;

    @Value("${alcor.batch.size:1}")
    private int batchSize;

    @Value("${arion.group:group1}")
    private String group;

    @GrpcClient("local-grpc-server")
    private GoalStateProvisionerGrpc.GoalStateProvisionerStub stub;

    public void insertNeighborRule () throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 1; i <= vpcSize; i++) {
            for (int j = 1; j <= subnetSize; j++) {
                for (int m = 1; m <= portSize; m++) {
                    String ip = String.join(".", "10", String.valueOf(i), String.valueOf(j), String.valueOf(m));
                    CompletableFuture<String> vpcFuture = CompletableFuture.supplyAsync(() -> {
                        try {
                            var neighborStateRequestBuilder = Goalstateprovisioner.ArionGoalStateRequest.newBuilder();
                            List<Neighbor.NeighborState> neighborStateList = new ArrayList<>();
                            neighborStateList.add(buildNeighborState(ip));
                            neighborStateRequestBuilder.addAllNeigborstates(neighborStateList);

                            stub.pushGoalstates(neighborStateRequestBuilder.build(), new StreamObserver<Goalstateprovisioner.GoalStateOperationReply>() {
                                @Override
                                public void onNext(Goalstateprovisioner.GoalStateOperationReply goalStateOperationReply) {
                                    count.getAndIncrement();
                                    if (count.get() == vpcSize * subnetSize * portSize) {
                                        countDownLatch.countDown();
                                    }
                                }

                                @Override
                                public void onError(Throwable throwable) {
                                }

                                @Override
                                public void onCompleted() {

                                }
                            });

                            return null;
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                    }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR);

                }

            }
        }
        countDownLatch.await();

        logger.info("Insert neighbors size: " + count.get());
    }

    public void bulkInsertNeighborRule () throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        for (int i = 1; i <= neighborSize; i++) {
            for (int j = 1; j <= neighborSize; j++) {
                CompletableFuture<String> vpcFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        var neighborStateRequestBuilder = Goalstateprovisioner.ArionGoalStateRequest.newBuilder();
                        List<Neighbor.NeighborState> neighborStateList = new ArrayList<>();
                        neighborStateList.add(buildNeighborState(getRandomIpv4Address()));
                        neighborStateRequestBuilder.addAllNeigborstates(neighborStateList);

                        stub.pushGoalstates(neighborStateRequestBuilder.build(), new StreamObserver<Goalstateprovisioner.GoalStateOperationReply>() {
                            @Override
                            public void onNext(Goalstateprovisioner.GoalStateOperationReply goalStateOperationReply) {
                                count.getAndIncrement();
                                if (count.get() == batchSize * neighborSize) {
                                    countDownLatch.countDown();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {
                            }

                            @Override
                            public void onCompleted() {

                            }
                        });

                        return null;
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, ThreadPoolExecutorUtils.SELECT_POOL_EXECUTOR);
            }
        }

        countDownLatch.await();

        logger.info("Insert neighbors size: " + count.get());
    }

    public String getRandomIpv4Address() {
        return "10." + (int) (Math.random() * 255) + "."
                + (int) (Math.random() * 255) + "."
                + (int) (Math.random() * 255);
    }

    public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
        CompletableFuture<Void> allFuturesResult =
                CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
        return allFuturesResult.thenApply(v ->
                futuresList.stream().
                        map(future -> future.join()).
                        collect(Collectors.<T>toList())
        );
    }

    public Neighbor.NeighborState buildNeighborState(String ip) throws Exception {
        Neighbor.NeighborConfiguration.Builder neighborConfigBuilder = Neighbor.NeighborConfiguration.newBuilder();
        neighborConfigBuilder.setRevisionNumber(1);
        neighborConfigBuilder.setId(UUID.randomUUID().toString());
        neighborConfigBuilder.setVpcId("vpc1");
        //neighborConfigBuilder.setName();
        neighborConfigBuilder.setMacAddress(randomMACAddress());

        Neighbor.NeighborType neighborType = Neighbor.NeighborType.valueOf(Neighbor.NeighborType.L2_VALUE);

        //TODO:setNeighborHostDvrMac
        //neighborConfigBuilder.setNeighborHostDvrMac();
        Neighbor.NeighborConfiguration.FixedIp.Builder fixedIpBuilder = Neighbor.NeighborConfiguration.FixedIp.newBuilder();
        fixedIpBuilder.setSubnetId(UUID.randomUUID().toString());
        fixedIpBuilder.setIpAddress(ip);
        fixedIpBuilder.setNeighborType(neighborType);

        neighborConfigBuilder.setHostMacAddress(randomMACAddress());
        neighborConfigBuilder.setHostIpAddress("10.0.1.2");

        fixedIpBuilder.setArionGroup(group);
        fixedIpBuilder.setTunnelId(888);

        neighborConfigBuilder.addFixedIps(fixedIpBuilder.build());
        //TODO:setAllowAddressPairs
        //neighborConfigBuilder.setAllowAddressPairs();

        Neighbor.NeighborState.Builder neighborStateBuilder = Neighbor.NeighborState.newBuilder();
        neighborStateBuilder.setOperationType(Common.OperationType.INFO);
        neighborStateBuilder.setConfiguration(neighborConfigBuilder.build());
        return neighborStateBuilder.build();
    }

    private String randomMACAddress(){
        Random rand = new Random();
        byte[] macAddr = new byte[6];
        rand.nextBytes(macAddr);
        macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated
        StringBuilder sb = new StringBuilder(18);
        for(byte b : macAddr){
            if(sb.length() > 0)
                sb.append(":");
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
