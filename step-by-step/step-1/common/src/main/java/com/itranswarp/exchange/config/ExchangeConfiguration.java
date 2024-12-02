package com.itranswarp.exchange.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
@ConfigurationProperties(prefix = "exchange.config")
public class ExchangeConfiguration {
    private String timeZone = ZoneId.systemDefault().getId();
    private String hmacKey;
    private ApiEndpoints apiEndpoints;
    @Bean
    public ZoneId createZoneId(){return ZoneId.of(this.timeZone);}
    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone.isEmpty() ? ZoneId.systemDefault().getId() : timeZone;
    }
    public void setHmacKey(String hmacKey) {
        this.hmacKey = hmacKey;
    }

    public String getHmacKey() {
        return hmacKey;
    }

    public ApiEndpoints getApiEndpoints() {
        return apiEndpoints;
    }

    public void setApiEndpoints(ApiEndpoints apiEndpoints) {
        this.apiEndpoints = apiEndpoints;
    }

    public static class ApiEndpoints{
        private String tradingApi;
        private String tradingEngineApi;

        public String getTradingApi() {
            return tradingApi;
        }
        public void setTradingApi(String tradingApi) {
            this.tradingApi = tradingApi;
        }
        public String getTradingEngineApi() {
            return tradingEngineApi;
        }
        public void setTradingEngineApi(String tradingEngineApi) {
            this.tradingEngineApi = tradingEngineApi;
        }
    }
}
