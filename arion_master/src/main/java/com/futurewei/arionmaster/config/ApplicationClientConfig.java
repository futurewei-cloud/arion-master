package com.futurewei.arionmaster.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import com.hazelcast.config.*;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.properties.ClusterProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Collections;

@Configuration
@ConditionalOnProperty(prefix = "hazelcast", name = "deployment", havingValue = "client")
public class ApplicationClientConfig {

    @Value("${kubernetesconfig:false}")
    private boolean kubernetesconfig;

    @Value("${namespace:default}")
    private String namespace;

    @Value("${service.name:default}")
    private String serviceName;

    @Bean
    @Scope("singleton")
    public ClientConfig clientConfig() throws Exception {
        ClientConfig clientConfig = new ClientConfig();

        if (kubernetesconfig) {
            /*
            HazelcastKubernetesDiscoveryStrategyFactory hazelcastKubernetesDiscoveryStrategyFactory
                    = new HazelcastKubernetesDiscoveryStrategyFactory();
            DiscoveryStrategyConfig discoveryStrategyConfig =
                    new DiscoveryStrategyConfig(hazelcastKubernetesDiscoveryStrategyFactory);
            discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(),
                    serviceName + "." + namespace + ".default.svc.cluster.local");

            clientConfig.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
            clientConfig
                    .getNetworkConfig()
                    .getDiscoveryConfig()
                    .addDiscoveryStrategyConfig(discoveryStrategyConfig);

             */

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
