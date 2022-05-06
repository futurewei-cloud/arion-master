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

import com.futurewei.arionmaster.model.RoutingRule;
import com.futurewei.arionmaster.model.RoutintRuleSerializer;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.transaction.HazelcastTransactionManager;
import com.hazelcast.spring.transaction.ManagedTransactionalTaskContext;
import com.hazelcast.transaction.TransactionalTaskContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.TransactionManager;

import java.util.Arrays;
import java.util.Collections;

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

    @Bean
    @Scope("singleton")
    public ClientConfig clientConfig() throws Exception {
        ClientConfig clientConfig = new ClientConfig();
        SerializerConfig sc = new SerializerConfig()
                .setImplementation(new RoutintRuleSerializer())
                .setTypeClass(RoutingRule.class);
        clientConfig.getSerializationConfig().addSerializerConfig(sc);
        ClientUserCodeDeploymentConfig clientUserCodeDeploymentConfig = new ClientUserCodeDeploymentConfig();
        clientUserCodeDeploymentConfig.addClass("com.futurewei.arionmaster.model.RoutingRule");
        clientUserCodeDeploymentConfig.addClass("com.futurewei.arionmaster.model.RoutintRuleSerializer");
        clientUserCodeDeploymentConfig.setEnabled(true);
        clientConfig.setUserCodeDeploymentConfig(clientUserCodeDeploymentConfig);
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
