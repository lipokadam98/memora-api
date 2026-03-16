package com.memora.memora_backend.multimedia;

import java.util.List;

public interface MultimediaService {
    Multimedia save(Multimedia multimedia);
    Multimedia findById(Long id);
    void delete(Long id);
    Multimedia update(Multimedia multimedia);
    List<Multimedia> findAll();
}
