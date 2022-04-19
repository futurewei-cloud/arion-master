package com.futurewei.arionmaster.config;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientUserCodeDeploymentConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Collections;

@Configuration
@ConditionalOnProperty(prefix = "arion.hazelcast", name = "deployment", havingValue = "client")
public class ApplicationClientConfig {

    @Value("${arion.kubernetes.config:false}")
    private boolean kubernetesconfig;

    @Value("${arion.hazelcast.config.namespace:default}")
    private String namespace;

    @Value("${arion.hazelcast.config.service.name:default}")
    private String serviceName;

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
