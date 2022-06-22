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

import com.futurewei.alcor.schema.Arionmaster;
import com.futurewei.alcor.schema.Common;
import com.futurewei.arionmaster.grpc.GrpcServerService;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.common.service.NeighborRuleService;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.QueryCacheConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.function.Consumer;

@Service
public final class Watcher {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerService.class);

    @Autowired
    private ClientConfig clientConfig;

    @Autowired HazelcastInstance hazelcastInstance;

    public Runnable watch(Arionmaster.ArionWingRequest req, String mapName, String cacheName, Consumer<Arionmaster.NeighborRule> resConsumer) {
            QueryCacheConfig queryCacheConfig = new QueryCacheConfig(cacheName);
            queryCacheConfig.getPredicateConfig().setImplementation(new NeighborRuleService(req.getGroup(), req.getRev()));
            clientConfig.addQueryCacheConfig(mapName, queryCacheConfig);
            HazelcastInstance client = HazelcastClient.newHazelcastClient(clientConfig);
            IMap<String, NeighborRule> map = (IMap) client.getMap(mapName);
            var cache = map.getQueryCache(cacheName);

        for (var neighborRule : cache.values()) {
            try {
                resConsumer.accept(buildNeighborRule(neighborRule, Common.OperationType.CREATE));
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        cache.addEntryListener(new EntryListener() {
            @Override
            public void mapEvicted(MapEvent mapEvent) {

            }

            @Override
            public void mapCleared(MapEvent mapEvent) {

            }

            @Override
            public void entryUpdated(EntryEvent entryEvent) {
                try {
                    resConsumer.accept(buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.CREATE));
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }

            @Override
            public void entryRemoved(EntryEvent entryEvent) {
                try {
                    resConsumer.accept(buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.DELETE));
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }

            @Override
            public void entryExpired(EntryEvent entryEvent) {

            }

            @Override
            public void entryEvicted(EntryEvent entryEvent) {
                try {
                    resConsumer.accept(buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.CREATE));
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }

            @Override
            public void entryAdded(EntryEvent entryEvent) {
                try {
                    resConsumer.accept(buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.CREATE));
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }

            }
        }, true);
        return () -> {
            cache.destroy();
        };
    }

    public Arionmaster.NeighborRule buildNeighborRule(NeighborRule neighborRule, Common.OperationType operationType) throws Exception {
        Arionmaster.NeighborRule.Builder neighborRuleBuilder = Arionmaster.NeighborRule.newBuilder();
        neighborRuleBuilder.setOperationType(operationType);
        neighborRuleBuilder.setIp(neighborRule.getIp());
        neighborRuleBuilder.setMac(neighborRule.getMac());
        neighborRuleBuilder.setHostip(neighborRule.getHostIp());
        neighborRuleBuilder.setHostmac(neighborRule.getHostMac());
        neighborRuleBuilder.setArionwingGroup(neighborRule.getArionGroup());
        neighborRuleBuilder.setVersion(neighborRule.getVersion());
        return neighborRuleBuilder.build();
    }
}
