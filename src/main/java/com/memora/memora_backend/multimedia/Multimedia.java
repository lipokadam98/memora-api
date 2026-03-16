package com.memora.memora_backend.multimedia;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Multimedia {

    /*
    TODO refactor the Multimedia object structure to include these fields
    id
    original_file_name
    content_type
    size
    bucket_name
    object_key
    created_at
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "type", nullable = false)
    private MultimediaType type;

    @Column(name = "description", nullable = false)
    private String description;
}
