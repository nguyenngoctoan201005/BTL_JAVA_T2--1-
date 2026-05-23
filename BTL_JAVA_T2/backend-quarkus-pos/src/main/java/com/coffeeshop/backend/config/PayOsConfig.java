package com.coffeeshop.backend.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import vn.payos.PayOS;

@ApplicationScoped
public class PayOsConfig {

    @ConfigProperty(name = "payos.client-id")
    String clientId;

    @ConfigProperty(name = "payos.api-key")
    String apiKey;

    @ConfigProperty(name = "payos.checksum-key")
    String checksumKey;

    /**
     * Chỉ cần dùng @Produces (Mặc định sẽ là @Dependent).
     * Sẽ không còn bị lỗi Proxy Constructor của Quarkus nữa!
     */
    @Produces
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}