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

import com.futurewei.arion.schema.Arionmaster;
import com.futurewei.arion.schema.Common;
import com.futurewei.arionmaster.service.Watcher;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.common.service.NeighborRuleService;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapEvent;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(prefix = "arion.db", name = "dbname", havingValue = "hazelcast")
public final class HazelcastWatcher implements Watcher {

    private static final Logger logger = LoggerFactory.getLogger(Watcher.class);

    @Autowired HazelcastInstance hazelcastInstance;

    public Runnable watch(Arionmaster.ArionWingRequest req, String mapName, String cacheName, Consumer<Arionmaster.NeighborRule> resConsumer) {
        IMap<String, NeighborRule> map = (IMap) hazelcastInstance.getMap(mapName);
        var neighborRuleListener = new NeighborRuleListener(resConsumer);
        var neighborRules = getNeighborRules(map, req.getGroup(), req.getRev());
        for (var neighborRule : neighborRules) {
            try {
                resConsumer.accept(neighborRuleListener.buildNeighborRule(neighborRule, Common.OperationType.CREATE));
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        var id = map.addEntryListener(neighborRuleListener, new NeighborRuleService(req.getGroup(), req.getRev()), true);
        return () -> {
            map.removeEntryListener(id);
        };
    }

    public Collection<NeighborRule> getNeighborRules (IMap neighborRuleMap, String group, long rev) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        Predicate groupPredicate = e.get("arionGroup").equal(group);
        Predicate predicate = e.get("version").greaterEqual(rev).and(groupPredicate);
        return neighborRuleMap.values(predicate);
    }
}



class NeighborRuleListener implements EntryListener {

    private static final Logger logger = LoggerFactory.getLogger(NeighborRuleListener.class);

    private Consumer<Arionmaster.NeighborRule> resConsumer;


    public NeighborRuleListener (Consumer<Arionmaster.NeighborRule> resConsumer) {
        this.resConsumer = resConsumer;
    }

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

    }

    @Override
    public void entryAdded(EntryEvent entryEvent) {
        try {
            resConsumer.accept(buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.CREATE));
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    }

    public Arionmaster.NeighborRule buildNeighborRule(NeighborRule neighborRule, Common.OperationType operationType) throws Exception {
        Arionmaster.NeighborRule.Builder neighborRuleBuilder = Arionmaster.NeighborRule.newBuilder();
        neighborRuleBuilder.setOperationType(operationType);
        neighborRuleBuilder.setIp(neighborRule.getIp());
        neighborRuleBuilder.setMac(neighborRule.getMac());
        neighborRuleBuilder.setHostip(neighborRule.getHostIp());
        neighborRuleBuilder.setHostmac(neighborRule.getHostMac());
        neighborRuleBuilder.setArionwingGroup(neighborRule.getArionGroup());
        neighborRuleBuilder.setTunnelId(neighborRule.getVni());
        neighborRuleBuilder.setVersion(neighborRule.getVersion());
        return neighborRuleBuilder.build();
    }
}
