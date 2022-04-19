package com.futurewei.arionmaster.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.*;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.kubernetes.KubernetesProperties;
import com.hazelcast.spi.properties.ClusterProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class ApplicationClientConfig {

    @Value("${kubernetesconfig:false}")
    private boolean kubernetesconfig;

    @Value("${namespace:default}")
    private String namespace;

    @Value("${service.name:default}")
    private String serviceName;

    private static final String DEFAULT_FALSE = "false";
    private static final String HAZELCAST_SERVICE_NAME = "service-hazelcast-server.default.svc.cluster.local";

    @Bean
    public ClientConfig clientConfig() throws Exception {
        ClientConfig clientConfig = new ClientConfig();

        if (kubernetesconfig) {
            // Step (1) in docs above
            HazelcastKubernetesDiscoveryStrategyFactory hazelcastKubernetesDiscoveryStrategyFactory
                    = new HazelcastKubernetesDiscoveryStrategyFactory();
            DiscoveryStrategyConfig discoveryStrategyConfig =
                    new DiscoveryStrategyConfig(hazelcastKubernetesDiscoveryStrategyFactory);
            discoveryStrategyConfig.addProperty(KubernetesProperties.SERVICE_DNS.key(),
                    HAZELCAST_SERVICE_NAME);

            // Step (2) in docs above
            clientConfig.setProperty(ClusterProperty.DISCOVERY_SPI_ENABLED.toString(), "true");
            clientConfig
                    .getNetworkConfig()
                    .getDiscoveryConfig()
                    .addDiscoveryStrategyConfig(discoveryStrategyConfig);
        } else {
            clientConfig
                    .getNetworkConfig()
                    .setAddresses(Collections.singletonList("127.0.0.1:5701"));
        }

        return clientConfig;
    }
}
