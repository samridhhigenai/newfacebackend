package com.faceattendance.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        try {
            // Create a trust manager that accepts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            ConnectionString connectionString = new ConnectionString(mongoUri);

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> {
                        builder.enabled(true)
                               .invalidHostNameAllowed(true)
                               .context(sslContext);
                    })
                    .applyToConnectionPoolSettings(builder -> {
                        builder.maxSize(10)
                               .minSize(1)
                               .maxConnectionIdleTime(60, TimeUnit.SECONDS)
                               .maxConnectionLifeTime(120, TimeUnit.SECONDS);
                    })
                    .applyToSocketSettings(builder -> {
                        builder.connectTimeout(30, TimeUnit.SECONDS)
                               .readTimeout(30, TimeUnit.SECONDS);
                    })
                    .applyToServerSettings(builder -> {
                        builder.heartbeatFrequency(10, TimeUnit.SECONDS)
                               .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS);
                    })
                    .build();

            return MongoClients.create(settings);
        } catch (Exception e) {
            // Fallback to default configuration
            System.err.println("Failed to create custom SSL MongoDB client, using default: " + e.getMessage());
            return MongoClients.create(mongoUri);
        }
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), databaseName);
    }
}
