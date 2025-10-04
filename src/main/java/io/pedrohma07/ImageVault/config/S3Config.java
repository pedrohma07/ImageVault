package io.pedrohma07.ImageVault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.credentials.access-key}")
    private String accessKey;

    @Value("${aws.credentials.secret-key}")
    private String secretKey;

    @Value("${aws.s3.endpoint}")
    private String s3Endpoint;

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // 1. Cria as credenciais estáticas que usaremos para o MinIO
        final AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        // 2. Constrói e retorna o S3Client, configurado para o MinIO
        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create(s3Endpoint)) // Aponta para o nosso MinIO local
                .forcePathStyle(true) // Habilita o estilo de acesso por path, necessário para o MinIO
                .accelerate(false)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        final AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create(s3Endpoint))
                .serviceConfiguration(s3Configuration)
                .build();
    }
}