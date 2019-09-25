package com.duofei.config;

import org.kurento.client.KurentoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author duofei
 * @date 2019/8/21
 */
@Configuration
public class KurentoConfig {

    @Bean
    public KurentoClient kurentoClient() {
        return KurentoClient.create("wss://192.168.3.126:8443/kurento");
    }
}
