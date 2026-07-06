package com.memora.memora_backend.note;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByUpdatedAtAscIdAsc(Long userId, PageRequest pageable);

    @Query("""
        SELECT n FROM Note n
        WHERE n.user.id = :userId
        AND (n.updatedAt > :updatedAt OR
            (n.updatedAt = :updatedAt AND n.id > :id))
        ORDER BY n.updatedAt ASC, n.id ASC
        """)
    List<Note> findNextPage(
            @Param("userId") Long userId,
            @Param("updatedAt") Instant updatedAt,
            @Param("id") Long id,
            Pageable pageable
    );

}
