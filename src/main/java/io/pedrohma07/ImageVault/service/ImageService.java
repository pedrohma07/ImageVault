package io.pedrohma07.ImageVault.service;

import io.pedrohma07.ImageVault.dto.image.ImageMetadataDTO;
import io.pedrohma07.ImageVault.dto.image.UpdateImageMetadataDTO;
import io.pedrohma07.ImageVault.exception.ResourceNotFoundException;
import io.pedrohma07.ImageVault.mapper.ImageMapper;
import io.pedrohma07.ImageVault.model.ImageMetadata;
import io.pedrohma07.ImageVault.model.User;
import io.pedrohma07.ImageVault.repository.ImageMetadataRepository;
import io.pedrohma07.ImageVault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final StorageService storageService;
    private final ImageMetadataRepository imageMetadataRepository;
    private final UserRepository userRepository;
    private final ImageMapper imageMapper;

    private static final List<String> VALID_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");

    public ImageMetadataDTO uploadImage(MultipartFile file, String userEmail) {
        log.debug("Starting image upload process for user: {}", userEmail);
        User owner = findUserByEmail(userEmail);

        // Salva arquivo original
        ImageMetadata metadata = storageService.uploadFile(file, owner);
        imageMetadataRepository.save(metadata);
        log.info("Successfully saved original image metadata with ID: {}", metadata.getId());

        try {
            log.debug("Starting thumbnail generation for image ID: {}", metadata.getId());
            // Gera thumbnail
            byte[] originalBytes = storageService.downloadFile(metadata.getStorageKey());
            byte[] thumbnailBytes = createThumbnailBytes(originalBytes, metadata.getContentType());

            String thumbnailKey = buildThumbnailKey(metadata.getStorageKey());
            storageService.uploadThumbnail(thumbnailBytes, thumbnailKey, metadata.getContentType());

            metadata.setThumbnailStorageKey(thumbnailKey);
            imageMetadataRepository.save(metadata);

            log.info("Successfully generated and saved thumbnail for image ID: {}", metadata.getId());

        } catch (Exception e) {
            log.error("Failed to generate thumbnail for image ID: {}", metadata.getId(), e);
            // Mantém a exceção original para o usuário
            throw new RuntimeException("Falha ao gerar thumbnail.");
        }

        log.info("Image upload process completed successfully for image ID: {}", metadata.getId());
        return imageMapper.toImageMetadataDTO(metadata);
    }

    public String getImageViewUrl(UUID imageId, String userEmail, boolean isThumbnail) {
        log.debug("Requesting view URL for image ID: {} by user: {}. Is thumbnail: {}", imageId, userEmail, isThumbnail);
        ImageMetadata metadata = findImageMetadataById(imageId);
        User currentUser = findUserByEmail(userEmail);

        if (!canViewImage(metadata, currentUser)) {
            log.warn("Access denied for user {} attempting to view image ID {}", userEmail, imageId);
            throw new AccessDeniedException("Você não tem permissão para acessar esta imagem.");
        }

        String keyToUse = isThumbnail ? metadata.getThumbnailStorageKey() : metadata.getStorageKey();
        if (keyToUse == null) {
            log.warn("Image resource key is null for image ID: {}. Thumbnail request: {}", imageId, isThumbnail);
            throw new ResourceNotFoundException("Recurso de imagem não disponível no momento.");
        }

        log.info("Successfully generated presigned URL for image ID: {}", imageId);
        return storageService.generatePresignedUrl(keyToUse);
    }

    public Page<ImageMetadataDTO> listUserImages(String userEmail, Pageable pageable) {
        log.debug("Fetching image list for user: {} with pageable: {}", userEmail, pageable);
        User owner = findUserByEmail(userEmail);

        Page<ImageMetadataDTO> images = imageMetadataRepository.findByOwner(owner, pageable)
                .map(imageMapper::toImageMetadataDTO);

        log.info("Found {} images on page {} for user {}", images.getNumberOfElements(), pageable.getPageNumber(), userEmail);
        return images;
    }

    public ImageMetadataDTO findMetadataById(UUID imageId, String userEmail) {
        log.debug("Fetching metadata for image ID: {} by user: {}", imageId, userEmail);
        User owner = findUserByEmail(userEmail);
        ImageMetadata metadata = findImageMetadataById(imageId);

        checkImageOwner(metadata, owner);

        log.info("Successfully fetched metadata for image ID: {}", imageId);
        return imageMapper.toImageMetadataDTO(metadata);
    }

    public ImageMetadataDTO updateImageMetadata(UUID imageId, String userEmail, UpdateImageMetadataDTO updateDTO) {
        log.debug("Attempting to update metadata for image ID: {} by user: {}", imageId, userEmail);
        User currentUser = findUserByEmail(userEmail);
        ImageMetadata metadata = findImageMetadataById(imageId);

        checkImageOwner(metadata, currentUser);

        String newFileName = updateDTO.fileName();
        String finalNewFileName = newFileName;

        boolean hasExtension = VALID_EXTENSIONS.stream()
                .anyMatch(ext -> finalNewFileName.toLowerCase().endsWith(ext));

        if (!hasExtension) {
            newFileName += mapContentTypeToExtension(metadata.getContentType());
        }

        metadata.setVisibility(updateDTO.visibility());
        metadata.setFileName(newFileName);

        ImageMetadata updatedMetadata = imageMetadataRepository.save(metadata);
        log.info("Successfully updated metadata for image ID: {}", imageId);

        return imageMapper.toImageMetadataDTO(updatedMetadata);
    }

    public void deleteImage(UUID imageId, String userEmail) {
        log.debug("Deletion requested for image ID: {} by user: {}", imageId, userEmail);
        ImageMetadata metadata = findImageMetadataById(imageId);
        User currentUser = findUserByEmail(userEmail);

        checkImageOwner(metadata, currentUser);

        storageService.deleteFile(metadata.getStorageKey());
        log.info("Deleted main storage object for image ID: {}", imageId);

        if (metadata.getThumbnailStorageKey() != null) {
            storageService.deleteFile(metadata.getThumbnailStorageKey());
            log.info("Deleted thumbnail storage object for image ID: {}", imageId);
        }

        imageMetadataRepository.delete(metadata);
        log.info("Successfully deleted image metadata and files for ID: {}", imageId);
    }


    // ** Metodos auxiliares **

    private boolean canViewImage(ImageMetadata metadata, User user) {
        return metadata.getVisibility() == io.pedrohma07.ImageVault.model.enums.ImageVisibility.PUBLIC
                || metadata.getOwner().equals(user);
    }

    private byte[] createThumbnailBytes(byte[] originalBytes, String contentType) throws Exception {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalBytes));
        BufferedImage thumbnail = Scalr.resize(original, Scalr.Method.AUTOMATIC, 300, 300, Scalr.OP_ANTIALIAS);

        String formatName = extractFormat(contentType);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, formatName, baos);
        return baos.toByteArray();
    }

    private String extractFormat(String contentType) {
        String format = contentType.replace("image/", "");
        return switch (format) {
            case "jpeg", "png", "gif" -> format;
            default -> "png";
        };
    }

    private String buildThumbnailKey(String originalKey) {
        return originalKey.replaceFirst("(\\.[^.]+)$", "_thumb$1");
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", email);
                    return new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + email);
                });
    }

    private ImageMetadata findImageMetadataById(UUID imageId) {
        return imageMetadataRepository.findById(imageId)
                .orElseThrow(() -> {
                    log.warn("Image metadata not found with ID: {}", imageId);
                    return new ResourceNotFoundException("Imagem não encontrada com o ID: " + imageId);
                });
    }

    private void checkImageOwner(ImageMetadata metadata, User user) {
        if (!metadata.getOwner().equals(user)) {
            log.warn("Ownership check failed. User {} attempted to access a resource owned by user {}", user.getId(), metadata.getOwner().getId());
            throw new AccessDeniedException("Permissão negada. O recurso não pertence ao usuário.");
        }
    }

    private String mapContentTypeToExtension(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpeg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}