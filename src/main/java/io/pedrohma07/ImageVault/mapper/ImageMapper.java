package io.pedrohma07.ImageVault.mapper;

import io.pedrohma07.ImageVault.dto.image.ImageMetadataDTO;
import io.pedrohma07.ImageVault.model.ImageMetadata;
import org.springframework.stereotype.Component;

@Component
public class ImageMapper {
    public ImageMetadataDTO toImageMetadataDTO(ImageMetadata imageMetadata) {
        return new ImageMetadataDTO(
                imageMetadata.getId(),
                imageMetadata.getFileName(),
                imageMetadata.getContentType(),
                imageMetadata.getSize(),
                imageMetadata.getVisibility(),
                imageMetadata.getOwner().getId(),
                imageMetadata.getUploadTimestamp()
        );
    }
}