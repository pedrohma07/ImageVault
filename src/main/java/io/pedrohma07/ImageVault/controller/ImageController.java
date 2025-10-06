package io.pedrohma07.ImageVault.controller;

import io.pedrohma07.ImageVault.dto.PaginatedResponse;
import io.pedrohma07.ImageVault.dto.image.ImageMetadataDTO;
import io.pedrohma07.ImageVault.dto.image.UpdateImageMetadataDTO;
import io.pedrohma07.ImageVault.dto.user.ResponseUserDTO;
import io.pedrohma07.ImageVault.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Endpoints para gerenciamento de imagens.")
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Faz o upload de uma nova imagem")
    public ImageMetadataDTO uploadImage(
            @RequestParam("file") MultipartFile file,
            Principal principal // injeta o usuário
    ) {
        log.info("Started uploadImage action");
        return imageService.uploadImage(file, principal.getName());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Lista as imagens do usuário autenticado de forma paginada")
    public PaginatedResponse<ImageMetadataDTO> listUserImages(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            Principal principal
    ) {
        log.info("Started listUserImages action");
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<ImageMetadataDTO> images = imageService.listUserImages(principal.getName(), pageable);
        return new PaginatedResponse<>(
                images.getContent(),
                page,
                limit,
                images.getTotalElements(),
                images.getTotalPages()
        );
    }

    @GetMapping("/{id}/metadata")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtém os metadados de uma imagem específica do usuário autenticado")
    public ImageMetadataDTO getMetadataById(@PathVariable UUID id, Principal principal) {
        log.info("Started getMetadataById action");
        return imageService.findMetadataById(id, principal.getName());
    }

    @PutMapping("/{id}/metadata")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Atualiza os metadados de uma imagem do usuário autenticado")
    public ImageMetadataDTO updateImageMetadata(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateImageMetadataDTO updateDTO,
            Principal principal
    ) {
        log.info("Started updateImageMetadata action");
        return imageService.updateImageMetadata(id, principal.getName(), updateDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta uma imagem do usuário autenticado")
    public void deleteImage(@PathVariable UUID id, Principal principal) {
        log.info("Started deleteImage action");
        imageService.deleteImage(id, principal.getName());
    }

    @GetMapping("/view/{id}")
    @Operation(summary = "Obtém uma URL temporária e redireciona para a visualização da imagem")
    public ResponseEntity<Void> viewImage(@PathVariable UUID id, Principal principal) {
        log.info("Started viewImage action");
        String imageUrl = imageService.getImageViewUrl(id, principal.getName(), false);
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(imageUrl))
                .build();
    }

    @GetMapping("/view/{id}/thumbnail")
    @Operation(summary = "Redireciona para a URL de visualização do thumbnail da imagem")
    public ResponseEntity<Void> viewImageThumbnail(@PathVariable UUID id, Principal principal) {
        log.info("Started viewImageThumbnail action");
        String imageUrl = imageService.getImageViewUrl(id, principal.getName(), true);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(imageUrl)).build();
    }
}
