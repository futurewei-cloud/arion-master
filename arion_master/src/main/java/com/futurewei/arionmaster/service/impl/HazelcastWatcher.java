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
import com.futurewei.arionmaster.config.ApplicationClientConfig;
import com.futurewei.arionmaster.service.Watcher;
import com.futurewei.common.model.NeighborRule;
import com.futurewei.common.model.SecurityGroupPortBinding;
import com.futurewei.common.model.SecurityGroupRule;
import com.futurewei.common.service.NeighborRuleService;
import com.futurewei.common.service.SecurityGroupPortBindingService;
import com.futurewei.common.service.SecurityGroupRuleService;
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

    public Runnable watchNeighborRule(Arionmaster.ArionWingRequest req, String mapName, Consumer<Arionmaster.ArionWingResponse> resConsumer) {
        IMap<String, NeighborRule> map = (IMap) hazelcastInstance.getMap(mapName);
        var neighborRuleListener = new NeighborRuleListener(resConsumer);
        var neighborRules = getNeighborRules(map, req.getGroup(), req.getRev());
        for (var neighborRule : neighborRules) {
            try {
                Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
                arionWingResponseBuilder.setNeighborRule(neighborRuleListener.buildNeighborRule(neighborRule, Common.OperationType.CREATE));
                resConsumer.accept(arionWingResponseBuilder.build());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        var id = map.addEntryListener(neighborRuleListener, new NeighborRuleService(req.getGroup(), req.getRev()), true);
        return () -> {
            map.removeEntryListener(id);
        };
    }

    public Runnable watchSecurityGroupPortBinding(Arionmaster.ArionWingRequest req, String mapName, Consumer<Arionmaster.ArionWingResponse> resConsumer) {
        IMap<String, SecurityGroupPortBinding> map = (IMap) hazelcastInstance.getMap(mapName);
        var securityGroupPortBindingListener = new SecurityGroupPortBindingListener(resConsumer);
        var securityGroupPortBindingCollection = getSecurityGroupPortBinding(map, req.getGroup(), req.getRev());
        for (var securityGroupPortBinding : securityGroupPortBindingCollection) {
            try {
                Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
                arionWingResponseBuilder.setSecuritygroupportbinding(securityGroupPortBindingListener.buildSecurityGroupPortBinding(securityGroupPortBinding, Common.OperationType.CREATE));
                resConsumer.accept(arionWingResponseBuilder.build());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        var id = map.addEntryListener(securityGroupPortBindingListener, new SecurityGroupPortBindingService(req.getGroup(), req.getRev()), true);
        return () -> {
            map.removeEntryListener(id);
        };
    }

    public Runnable watchSecurityGroupRule(Arionmaster.ArionWingRequest req, String mapName, Consumer<Arionmaster.ArionWingResponse> resConsumer) {
        IMap<String, SecurityGroupRule> map = (IMap) hazelcastInstance.getMap(mapName);
        var securityGroupRuleListener = new SecurityGorupRuleListener(resConsumer);
        var securityGroupRuleCollection = getSecurityGroupRules(map, req.getGroup(), req.getRev());
        
        for (var securityGroupRule : securityGroupRuleCollection) {
            try {
                Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
                arionWingResponseBuilder.setSecuritygrouprule(securityGroupRuleListener.buildSecurityGroupRule(securityGroupRule, Common.OperationType.CREATE));
                resConsumer.accept(arionWingResponseBuilder.build());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }
        }
        var id = map.addEntryListener(securityGroupRuleListener, new SecurityGroupRuleService(req.getGroup(), req.getRev()), true);
        return () -> {
            map.removeEntryListener(id);
        };
    }

    public Collection<NeighborRule> getNeighborRules (IMap neighborRuleMap, String group, long rev) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        Predicate predicate = e.get("version").greaterEqual(rev);
        if (group != null && !group.isEmpty()) {
            Predicate groupPredicate = e.get("arionGroup").equal(group);
            predicate = e.get("version").greaterEqual(rev).and(groupPredicate);
        }

        return neighborRuleMap.values(predicate);
    }

    public Collection<SecurityGroupPortBinding> getSecurityGroupPortBinding (IMap neighborRuleMap, String group, long rev) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        Predicate predicate = e.get("version").greaterEqual(rev);
        if (group != null && !group.isEmpty()) {
            Predicate groupPredicate = e.get("arionGroup").equal(group);
            predicate = e.get("version").greaterEqual(rev).and(groupPredicate);
        }

        return neighborRuleMap.values(predicate);
    }

    public Collection<SecurityGroupRule> getSecurityGroupRules (IMap securityGroupRuleMap, String group, long rev) {
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        Predicate predicate = e.get("version").greaterEqual(rev);
        if (group != null && !group.isEmpty()) {
            Predicate groupPredicate = e.get("arionGroup").equal(group);
            predicate = e.get("version").greaterEqual(rev).and(groupPredicate);
        }

        return securityGroupRuleMap.values(predicate);
    }

    @Override
    public Runnable watch(Arionmaster.ArionWingRequest req, Consumer<Arionmaster.ArionWingResponse> resConsumer) {
        if (req.getMap().equals("NeighborRule")) {
            return watchNeighborRule(req, ApplicationClientConfig.neighborMapName, resConsumer);
        } else if (req.getMap().equals("SecurityGroupPortBinding")) {
            return watchSecurityGroupPortBinding(req, ApplicationClientConfig.securityGroupPortBindingMapName, resConsumer);
        } else if (req.getMap().equals("SecurityGroupRule")) {
            return watchSecurityGroupRule(req, ApplicationClientConfig.securityGroupRulesMapName, resConsumer);
        }
        return null;
    }
}

class NeighborRuleListener implements EntryListener {

    private static final Logger logger = LoggerFactory.getLogger(NeighborRuleListener.class);

    private Consumer<Arionmaster.ArionWingResponse> resConsumer;


    public NeighborRuleListener (Consumer<Arionmaster.ArionWingResponse> resConsumer) {
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
            var neighborRule = buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.CREATE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setNeighborRule(neighborRule);
            resConsumer.accept(arionWingResponseBuilder.build());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void entryRemoved(EntryEvent entryEvent) {
        try {
            var neighborRule = buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.DELETE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setNeighborRule(neighborRule);
            resConsumer.accept(arionWingResponseBuilder.build());
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
            var neighborRule = buildNeighborRule((NeighborRule) entryEvent.getValue(), Common.OperationType.CREATE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setNeighborRule(neighborRule);
            resConsumer.accept(arionWingResponseBuilder.build());
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

class SecurityGroupPortBindingListener implements EntryListener {

    private static final Logger logger = LoggerFactory.getLogger(NeighborRuleListener.class);

    private Consumer<Arionmaster.ArionWingResponse> resConsumer;


    public SecurityGroupPortBindingListener (Consumer<Arionmaster.ArionWingResponse> resConsumer) {
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
            var securityGroupPortBinding = buildSecurityGroupPortBinding((SecurityGroupPortBinding) entryEvent.getValue(), Common.OperationType.CREATE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setSecuritygroupportbinding(securityGroupPortBinding);
            resConsumer.accept(arionWingResponseBuilder.build());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void entryRemoved(EntryEvent entryEvent) {
        try {
            var securityGroupPortBinding = buildSecurityGroupPortBinding((SecurityGroupPortBinding) entryEvent.getValue(), Common.OperationType.DELETE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setSecuritygroupportbinding(securityGroupPortBinding);
            resConsumer.accept(arionWingResponseBuilder.build());
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
            var securityGroupPortBinding = buildSecurityGroupPortBinding((SecurityGroupPortBinding) entryEvent.getValue(), Common.OperationType.CREATE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setSecuritygroupportbinding(securityGroupPortBinding);
            resConsumer.accept(arionWingResponseBuilder.build());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    }

    public Arionmaster.SecurityGroupPortBinding buildSecurityGroupPortBinding(SecurityGroupPortBinding securityGroupPortBinding, Common.OperationType operationType) throws Exception {
        Arionmaster.SecurityGroupPortBinding.Builder securityGroupPortBindingBuilder = Arionmaster.SecurityGroupPortBinding.newBuilder();
        securityGroupPortBindingBuilder.setOperationType(operationType);
        securityGroupPortBindingBuilder.setPortid(securityGroupPortBinding.getPortId());
        securityGroupPortBindingBuilder.setSecuritygroupid(securityGroupPortBinding.getSecurityGroupId());
        securityGroupPortBindingBuilder.setVersion(securityGroupPortBinding.getVersion());
        return securityGroupPortBindingBuilder.build();
    }
}

class SecurityGorupRuleListener implements EntryListener {

    private static final Logger logger = LoggerFactory.getLogger(NeighborRuleListener.class);

    private Consumer<Arionmaster.ArionWingResponse> resConsumer;


    public SecurityGorupRuleListener (Consumer<Arionmaster.ArionWingResponse> resConsumer) {
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
            var securityGroupRule = buildSecurityGroupRule((SecurityGroupRule) entryEvent.getValue(), Common.OperationType.CREATE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setSecuritygrouprule(securityGroupRule);
            resConsumer.accept(arionWingResponseBuilder.build());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    @Override
    public void entryRemoved(EntryEvent entryEvent) {
        try {
            var securityGroupRule = buildSecurityGroupRule((SecurityGroupRule) entryEvent.getValue(), Common.OperationType.DELETE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setSecuritygrouprule(securityGroupRule);
            resConsumer.accept(arionWingResponseBuilder.build());
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
            var securityGroupRule = buildSecurityGroupRule((SecurityGroupRule) entryEvent.getValue(), Common.OperationType.CREATE);
            Arionmaster.ArionWingResponse.Builder arionWingResponseBuilder = Arionmaster.ArionWingResponse.newBuilder();
            arionWingResponseBuilder.setSecuritygrouprule(securityGroupRule);
            resConsumer.accept(arionWingResponseBuilder.build());
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

    }



    public Arionmaster.SecurityGroupRule buildSecurityGroupRule(SecurityGroupRule securityGroupRule, Common.OperationType operationType) throws Exception {
        Arionmaster.SecurityGroupRule.Builder securityGroupRuleBuilder = Arionmaster.SecurityGroupRule.newBuilder();
        securityGroupRuleBuilder.setOperationType(operationType);
        securityGroupRuleBuilder.setSecuritygroupid(securityGroupRule.getSecurityGroupId());
        securityGroupRuleBuilder.setRemotegroupid(securityGroupRule.getRemoteGroupId());
        securityGroupRuleBuilder.setDirection(securityGroupRule.getDirection());
        securityGroupRuleBuilder.setRemoteipprefix(securityGroupRule.getRemoteIpPrefix());
        securityGroupRuleBuilder.setProtocol(securityGroupRule.getProtocol());
        securityGroupRuleBuilder.setPortrangemax(securityGroupRule.getPortRangeMax());
        securityGroupRuleBuilder.setPortrangemin(securityGroupRule.getPortRangeMin());
        securityGroupRuleBuilder.setEthertype(securityGroupRule.getEthertype());
        securityGroupRuleBuilder.setVni(securityGroupRule.getVni());
        securityGroupRuleBuilder.setVersion(securityGroupRule.getVersion());

        return securityGroupRuleBuilder.build();
    }

}
