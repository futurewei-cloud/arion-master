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

import com.futurewei.alcor.schema.Arionmaster;
import com.futurewei.alcor.schema.WatchGrpc;
import com.futurewei.arionmaster.service.impl.Watcher;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class Watch extends WatchGrpc.WatchImplBase{



    private String mapName = "com.futurewei.common.model.NeighborRule";

    @Autowired
    private Watcher watcher;


    @Override
    public StreamObserver<Arionmaster.ArionWingRequest> watch(final StreamObserver<Arionmaster.NeighborRule> responseObserver) {
        return new StreamObserver<Arionmaster.ArionWingRequest>() {
            private Runnable cancellation = () -> {};
            @Override
            public void onNext(Arionmaster.ArionWingRequest req) {
                cancellation = watcher.watch(req, mapName, req.getIp(), r -> {
                    synchronized (responseObserver) {
                        responseObserver.onNext(r);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                cancellation.run();
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
