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
        User owner = findUserByEmail(userEmail);

        // Salva arquivo original
        ImageMetadata metadata = storageService.uploadFile(file, owner);
        imageMetadataRepository.save(metadata);

        try {
            // Gera thumbnail
            byte[] originalBytes = storageService.downloadFile(metadata.getStorageKey());
            byte[] thumbnailBytes = createThumbnailBytes(originalBytes, metadata.getContentType());

            String thumbnailKey = buildThumbnailKey(metadata.getStorageKey());
            storageService.uploadThumbnail(thumbnailBytes, thumbnailKey, metadata.getContentType());

            metadata.setThumbnailStorageKey(thumbnailKey);
            imageMetadataRepository.save(metadata);

            log.info("Thumbnail gerado com sucesso para a imagem ID: {}", metadata.getId());

        } catch (Exception e) {
            log.error("Falha ao gerar thumbnail para a imagem ID: {}", metadata.getId(), e);
            throw new RuntimeException("Falha ao gerar thumbnail.");
        }

        return imageMapper.toImageMetadataDTO(metadata);
    }

    public String getImageViewUrl(UUID imageId, String userEmail, boolean isThumbnail) {
        ImageMetadata metadata = findImageMetadataById(imageId);

        User currentUser = findUserByEmail(userEmail);

        if (!canViewImage(metadata, currentUser)) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta imagem.");
        }

        String keyToUse = isThumbnail ? metadata.getThumbnailStorageKey() : metadata.getStorageKey();
        if (keyToUse == null) {
            throw new ResourceNotFoundException("Recurso de imagem não disponível no momento.");
        }

        return storageService.generatePresignedUrl(keyToUse);
    }

    public Page<ImageMetadataDTO> listUserImages(String userEmail, Pageable pageable) {
        User owner = findUserByEmail(userEmail);

        return imageMetadataRepository.findByOwner(owner, pageable)
                .map(imageMapper::toImageMetadataDTO);
    }

    public ImageMetadataDTO findMetadataById(UUID imageId, String userEmail) {
        User owner = findUserByEmail(userEmail);

        ImageMetadata metadata = findImageMetadataById(imageId);

        checkImageOwner(metadata, owner);

        return imageMapper.toImageMetadataDTO(metadata);
    }

    public ImageMetadataDTO updateImageMetadata(UUID imageId, String userEmail, UpdateImageMetadataDTO updateDTO) {
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

        return imageMapper.toImageMetadataDTO(updatedMetadata);
    }

    public void deleteImage(UUID imageId, String userEmail) {
        ImageMetadata metadata = findImageMetadataById(imageId);

        User currentUser = findUserByEmail(userEmail);

        checkImageOwner(metadata, currentUser);

        storageService.deleteFile(metadata.getStorageKey());

        // Se o thumbnail existir
        if (metadata.getThumbnailStorageKey() != null) {
            storageService.deleteFile(metadata.getThumbnailStorageKey());
        }

        imageMetadataRepository.delete(metadata);
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com o e-mail: " + email));
    }

    private ImageMetadata findImageMetadataById(UUID imageId) {
        return imageMetadataRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Imagem não encontrada com o ID: " + imageId));
    }

    private void checkImageOwner(ImageMetadata metadata, User user) {
        if (!metadata.getOwner().equals(user)) {
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
