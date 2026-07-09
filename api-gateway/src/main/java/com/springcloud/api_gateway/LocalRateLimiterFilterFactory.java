package com.springcloud.api_gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocalRateLimiterFilterFactory extends AbstractGatewayFilterFactory<LocalRateLimiterFilterFactory.Config> {

    private final Map<String, TokenBucketState> buckets = new ConcurrentHashMap<>();

    public LocalRateLimiterFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";

            String routeId = exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR) != null
                    ? exchange.getAttribute(org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR).toString()
                    : "default";

            String trackingKey = ip + "-" + routeId;
            long now = System.currentTimeMillis();

            TokenBucketState bucket = buckets.compute(trackingKey, (k, currentBucket) -> {
                if (currentBucket == null) {
                    return new TokenBucketState(config.getBurstCapacity(), now);
                }

                long elapsedSeconds = (now - currentBucket.lastRefillTime) / 1000;
                double tokensToAdd = elapsedSeconds * config.getReplenishRate();
                double totalTokens = Math.min(config.getBurstCapacity(), currentBucket.tokens + tokensToAdd);

                long updatedRefillTime = elapsedSeconds > 0 ? now : currentBucket.lastRefillTime;
                return new TokenBucketState(totalTokens, updatedRefillTime);
            });

            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                final int remainingTokens = (int) Math.floor(bucket.tokens);

                // Use .then() to alter headers down-chain when execution context is flexible
                return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                    if (!exchange.getResponse().isCommitted()) {
                        exchange.getResponse().getHeaders().add("X-Local-RateLimit-Remaining", String.valueOf(remainingTokens));
                    }
                }));
            }

            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        };
    }

    public static class Config {
        private int replenishRate = 10;
        private int burstCapacity = 20;

        public int getReplenishRate() { return replenishRate; }
        public void setReplenishRate(int replenishRate) { this.replenishRate = replenishRate; }
        public int getBurstCapacity() { return burstCapacity; }
        public void setBurstCapacity(int burstCapacity) { this.burstCapacity = burstCapacity; }
    }

    // Added this class to solve the compile error
    private static class TokenBucketState {
        double tokens;
        long lastRefillTime;

        TokenBucketState(double tokens, long lastRefillTime) {
            this.tokens = tokens;
            this.lastRefillTime = lastRefillTime;
        }
    }
}
