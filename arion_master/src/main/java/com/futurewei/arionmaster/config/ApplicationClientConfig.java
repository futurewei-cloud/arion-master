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

package com.futurewei.arionmaster.config;


import com.futurewei.common.model.ArionDataSerializableFactory;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.QueryCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.QueryCache;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.PredicateBuilder;
import com.hazelcast.query.Predicates;
import com.hazelcast.spring.transaction.HazelcastTransactionManager;
import com.hazelcast.spring.transaction.ManagedTransactionalTaskContext;
import com.hazelcast.transaction.TransactionalTaskContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.TransactionManager;

import java.util.Arrays;

@Configuration
@ConditionalOnProperty(prefix = "arion.hazelcast", name = "deployment", havingValue = "client")
public class ApplicationClientConfig {

    @Value("${arion.kubernetes.config:false}")
    private boolean kubernetesconfig;

    @Value("${arion.hazelcast.config.addresses:127.0.0.1:5701}")
    private String addresses;

    @Value("${arion.hazelcast.config.namespace:default}")
    private String namespace;

    @Value("${arion.hazelcast.config.service.name:default}")
    private String serviceName;

    @Value("${arion.hazelcast.clientusercodedeployment:false}")
    private boolean clientUserCodeDeployment;

    @Value("${arion.hazelcast.cachesize:10000}")
    private int cacheSize;

    public static String neighborMapName = "com.futurewei.common.model.NeighborRule";

    public static String securityGroupPortBindingMapName = "com.futurewei.common.model.SecurityGroupPortBinding";

    public static String securityGroupRulesMapName = "com.futurewei.common.model.SecurityGroupRule";

    public static String neighborCacheName = "neighborCache";

    public static String securityGroupPortBindingCacheName = "securityGroupPortBindingCache";

    public static String securityGroupRulesCacheName = "securityGroupRulesCache";

    @Bean
    @Scope("singleton")
    public ClientConfig clientConfig() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSerializationConfig().addDataSerializableFactory(ArionDataSerializableFactory.FACTORY_ID, new ArionDataSerializableFactory());

        QueryCacheConfig neighborQueryCacheConfig = new QueryCacheConfig(neighborCacheName);
        neighborQueryCacheConfig.getEvictionConfig().setSize(cacheSize);
        clientConfig.addQueryCacheConfig(neighborMapName, neighborQueryCacheConfig);

        QueryCacheConfig securityGroupPortBindingQueryCacheConfig = new QueryCacheConfig(securityGroupPortBindingCacheName);
        securityGroupPortBindingQueryCacheConfig.getEvictionConfig().setSize(cacheSize);
        clientConfig.addQueryCacheConfig(securityGroupPortBindingMapName, securityGroupPortBindingQueryCacheConfig);

        QueryCacheConfig securityGroupRulesQueryCacheConfig = new QueryCacheConfig(securityGroupRulesCacheName);
        securityGroupRulesQueryCacheConfig.getEvictionConfig().setSize(cacheSize);
        clientConfig.addQueryCacheConfig(securityGroupRulesMapName, securityGroupRulesQueryCacheConfig);

        if (kubernetesconfig) {
            clientConfig.getNetworkConfig().getKubernetesConfig().setEnabled(true)
                    .setProperty("namespace", namespace)
                    .setProperty("service-name", serviceName);
        } else {
            clientConfig
                    .getNetworkConfig()
                    .setAddresses(Arrays.asList(addresses.split(",")));
        }

        return clientConfig;
    }

    @Bean
    public QueryCache neighborQueryCache(HazelcastInstance hazelcastInstance) {
        IMap<Integer, Integer> clientMap = (IMap) hazelcastInstance.getMap(neighborMapName);
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        QueryCache queryCache = clientMap.getQueryCache(neighborCacheName, e.get("version").greaterEqual(1), true);
        return queryCache;
    }

    @Bean
    public QueryCache securityGroupPortBindingQueryCache(HazelcastInstance hazelcastInstance) {
        IMap<Integer, Integer> clientMap = (IMap) hazelcastInstance.getMap(securityGroupPortBindingMapName);
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        QueryCache queryCache = clientMap.getQueryCache(securityGroupPortBindingCacheName, e.get("version").greaterEqual(1), true);
        return queryCache;
    }

    @Bean
    public QueryCache securityGroupRulesQueryCache(HazelcastInstance hazelcastInstance) {
        IMap<Integer, Integer> clientMap = (IMap) hazelcastInstance.getMap(securityGroupRulesMapName);
        PredicateBuilder.EntryObject e = Predicates.newPredicateBuilder().getEntryObject();
        QueryCache queryCache = clientMap.getQueryCache(securityGroupRulesCacheName, e.get("version").greaterEqual(1), true);
        return queryCache;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    @Bean
    public TransactionManager transactionManager(HazelcastInstance hazelcastInstance) {
        return new HazelcastTransactionManager(hazelcastInstance);
    }

    @Bean
    public TransactionalTaskContext transactionalTaskContext(TransactionManager transactionManager) {
        return new ManagedTransactionalTaskContext((HazelcastTransactionManager) transactionManager);
    }
}
