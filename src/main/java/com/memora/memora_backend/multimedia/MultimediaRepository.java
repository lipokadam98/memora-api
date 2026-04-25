package com.memora.memora_backend.multimedia;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MultimediaRepository extends JpaRepository<Multimedia, Long> {
    @Query("""
        SELECT m FROM Multimedia m
        WHERE m.user.id = :userId
        AND (m.uploadDate > :uploadDate OR
            (m.uploadDate = :uploadDate AND m.id > :id))
        ORDER BY m.uploadDate ASC, m.id ASC
        """)
    List<Multimedia> findNextPage(
            @Param("userId") Long userId,
            @Param("uploadDate") Instant uploadDate,
            @Param("id") Long id,
            Pageable pageable
    );

    List<Multimedia> findByUserIdOrderByUploadDateAscIdAsc(Long userId, Pageable pageable);
}
