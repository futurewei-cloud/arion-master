package com.futurewei.arionmaster.config;

import com.hazelcast.config.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "hazelcast", name = "deployment", havingValue = "server")
public class ApplicationServerConfig {

    @Value("${kubernetesconfig:false}")
    private boolean kubernetesconfig;

    @Value("${namespace:default}")
    private String namespace;

    @Value("${service.name:default}")
    private String serviceName;

    @Bean
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
}
