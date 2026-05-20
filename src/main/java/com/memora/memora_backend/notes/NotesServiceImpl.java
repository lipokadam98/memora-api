package com.memora.memora_backend.notes;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NotesServiceImpl implements NotesService{

    private final NotesRepository notesRepository;

    @Override
    public Notes save(Notes notes) {
        return notesRepository.save(notes);
    }

    @Override
    public Notes findById(Long id) {
        return notesRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        notesRepository.deleteById(id);
    }

    @Override
    public void deleteAll(List<Long> ids) {
        //To be implemented
    }

    @Override
    public List<Notes> findAll(Long userId) {
        return notesRepository.findAllByUserId(userId);
    }

    @Override
    public Notes update(Notes notes) {
        //To be implemented
        return null;
    }
}
