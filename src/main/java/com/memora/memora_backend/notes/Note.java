package com.memora.memora_backend.notes;

import com.memora.memora_backend.multimedia.Multimedia;
import com.memora.memora_backend.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "notes")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 150, message = "Title cannot exceed 150 characters")
    @NotBlank(message = "Title cannot be empty")
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Size(max = 20000, message = "Note content cannot exceed 20,000 characters")
    @NotBlank(message = "Note content cannot be empty")
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "note", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Multimedia> multimediaList = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addMultimedia(Multimedia multimedia) {
        this.multimediaList.add(multimedia);
        multimedia.setNote(this);
    }

    public void removeMultimedia(Multimedia multimedia) {
        this.multimediaList.remove(multimedia);
        multimedia.setNote(null);
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}