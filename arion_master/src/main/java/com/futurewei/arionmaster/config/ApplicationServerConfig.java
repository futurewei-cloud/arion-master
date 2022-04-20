package com.futurewei.arionmaster.config;

import com.futurewei.arionmaster.controller.NeighborStateController;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

import java.util.Collections;


@Configuration
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
        if (kubernetesconfig) {
            Config config = new Config();
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                    .setProperty("namespace", namespace)
                    .setProperty("service-name", serviceName);
            return config;
        } else {
            return new Config();
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
}
