package com.example.front_office.config;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class PaypalConfig {

    // Spring inyecta las claves desde application.properties
    @Value("${paypal.client.id}")
    private String clientId;

    @Value("${paypal.client.secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode; // 'sandbox' o 'live'

    // Definimos el APIContext como un Bean para que pueda ser inyectado en los servicios
    @Bean
    public APIContext apiContext() {
        APIContext context = new APIContext(clientId, clientSecret);

        // Configuramos el modo de operación (Sandbox)
        Map<String, String> configMap = new HashMap<>();
        configMap.put("mode", mode);
        context.setConfigurationMap(configMap);

        return context;
    }
}