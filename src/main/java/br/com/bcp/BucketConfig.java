package br.com.bcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class BucketConfig {

    @Value("${cloud.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.credentials.secretKey}")
    private String secretKey;

    @Value("${cloud.region.static}")
    private String region;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint("grtvagwwdldc.compat.objectstorage.sa-saopaulo-1.oraclecloud.com", 443, true)
            .credentials(accessKey, secretKey)
            .region(region)
            .build();
    }
}
