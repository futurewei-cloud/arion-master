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

package com.futurewei.arionmaster.config;

import com.futurewei.arionmaster.controller.NeighborStateController;
import com.futurewei.arionmaster.model.RoutingRule;
import com.futurewei.arionmaster.model.RoutintRuleSerializer;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.transaction.ManagedTransactionalTaskContext;
import com.hazelcast.transaction.TransactionalTaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.hazelcast.spring.transaction.HazelcastTransactionManager;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionContext;

import java.util.Collections;


@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(prefix = "arion.hazelcast", name = "deployment", havingValue = "server")
public class ApplicationServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(NeighborStateController.class);

    @Value("${arion.kubernetes.config:false}")
    private boolean kubernetesconfig;

    @Value("${arion.hazelcast.config.namespace:default}")
    private String namespace;

    @Value("${arion.hazelcast.config.service.name:default}")
    private String serviceName;

    @Bean
    @Scope("singleton")
    Config config() {
        Config config = new Config();
        SerializerConfig sc = new SerializerConfig()
                .setImplementation(new RoutintRuleSerializer())
                .setTypeClass(RoutingRule.class);
        config.getSerializationConfig().addSerializerConfig(sc);

        if (kubernetesconfig) {
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("namespace", namespace)
                    .setProperty("service-name", serviceName);
            return config;
        } else {
            return config;
        }
    }

    @Bean
    HazelcastInstance hazelcastInstance(Config config) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        return instance;
    }

    @Bean
    @Scope("singleton")
    public ClientConfig clientConfig() throws Exception {
        ClientConfig clientConfig = new ClientConfig();

        if (kubernetesconfig) {
            clientConfig.getNetworkConfig().getKubernetesConfig().setEnabled(true)
                    .setProperty("namespace", namespace)
                    .setProperty("service-name", serviceName);
            ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
            clientUserCodeDeploymentConfig.addClass("com.futurewei.arionmaster.model.RoutingRule");
            clientUserCodeDeploymentConfig.setEnabled(true);
            clientConfig.setUserCodeDeploymentConfig(clientUserCodeDeploymentConfig);
        } else {
            clientConfig
                    .getNetworkConfig()
                    .setAddresses(Collections.singletonList("127.0.0.1:5701"));
        }
        return clientConfig;
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
