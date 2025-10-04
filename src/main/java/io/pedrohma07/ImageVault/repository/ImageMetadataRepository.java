package io.pedrohma07.ImageVault.repository;

import io.pedrohma07.ImageVault.model.ImageMetadata;
import io.pedrohma07.ImageVault.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, UUID>{
    Page<ImageMetadata> findByOwner(User owner, Pageable pageable);
}