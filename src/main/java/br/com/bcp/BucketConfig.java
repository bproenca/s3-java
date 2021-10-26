package br.com.bcp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;

@Configuration
public class BucketConfig {

    @Value("${cloud.aws.credentials.accessKey}")
    private String awsId;

    @Value("${cloud.aws.credentials.secretKey}")
    private String awsKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
            .endpoint("s3.amazonaws.com", 443, true)
            .credentials(awsId, awsKey)
            .region(region)
            .build();
    }
}
