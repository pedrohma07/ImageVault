package io.pedrohma07.ImageVault.service;

import io.pedrohma07.ImageVault.model.ImageMetadata;
import io.pedrohma07.ImageVault.model.User;
import io.pedrohma07.ImageVault.model.enums.ImageVisibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;


import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final Set<String> SUPPORTED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/jpg");

    public ImageMetadata uploadFile(MultipartFile file, User owner) {
        log.debug("Starting upload process for user: {}", owner.getEmail());
        if (file.isEmpty()) {
            log.warn("Attempted to upload an empty file by user: {}", owner.getEmail());
            throw new IllegalStateException("Não é possível fazer upload de um arquivo vazio.");
        }

        // Verifica se o arquivo é uma imagem básica (jpg, png, webp)
        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equalsIgnoreCase("image/jpeg") ||
                  contentType.equalsIgnoreCase("image/png") ||
                  contentType.equalsIgnoreCase("image/webp"))) {
            log.warn("Unsupported file type '{}' uploaded by user: {}", contentType, owner.getEmail());
            throw new IllegalStateException("Apenas arquivos de imagem (jpg, png, webp) são permitidos.");
        }

        try {
            // Gera uma chave de armazenamento única
            String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String storageKey = String.format("%s/%s.%s", owner.getId(), UUID.randomUUID(), extension);

            log.debug("Uploading file to S3 with key: {}", storageKey);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storageKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Envia o arquivo
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded file to S3 for user: {} with key: {}", owner.getEmail(), storageKey);

            return ImageMetadata.builder()
                    .owner(owner)
                    .fileName(file.getOriginalFilename())
                    .storageKey(storageKey)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .visibility(ImageVisibility.PRIVATE)
                    .build();

        } catch (IOException e) {
            log.error("Failed to upload file for user: {}", owner.getEmail(), e);
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }

    public void deleteFile(String storageKey) {
        log.debug("Deleting file from S3 with key: {}", storageKey);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
        log.info("Successfully deleted file from S3 with key: {}", storageKey);
    }

    public String generatePresignedUrl(String storageKey) {
        log.debug("Generating presigned URL for key: {}", storageKey);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // A URL será válida por 10 minutos
                .getObjectRequest(getObjectRequest)
                .build();


        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        log.info("Successfully generated presigned URL for key: {}", storageKey);
        return presignedRequest.url().toString();
    }

    public byte[] downloadFile(String storageKey) {
        log.debug("Downloading file from S3 with key: {}", storageKey);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .build();
        ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(getObjectRequest);
        log.info("Successfully downloaded file from S3 with key: {}", storageKey);
        return responseBytes.asByteArray();
    }

    public void uploadThumbnail(byte[] thumbnailBytes, String storageKey, String contentType) {
        log.debug("Uploading thumbnail to S3 with key: {}", storageKey);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(storageKey)
                .contentType(contentType)
                .contentLength((long) thumbnailBytes.length)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(thumbnailBytes));
    }
}
