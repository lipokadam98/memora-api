package com.memora.memora_backend.notes;

import java.util.List;

public interface NotesService {
    Notes save(Notes notes);
    Notes findById(Long id);
    void delete(Long id);
    void deleteAll(List<Long> ids);
    List<Notes> findAll(Long userId);
    Notes update(Notes notes);
}
