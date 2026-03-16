package com.memora.memora_backend.multimedia;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MultimediaServiceImpl implements MultimediaService{

    private final MultimediaRepository multimediaRepository;

    public MultimediaServiceImpl(MultimediaRepository multimediaRepository) {
        this.multimediaRepository = multimediaRepository;
    }

    @Override
    public Multimedia save(Multimedia multimedia) {
        return multimediaRepository.save(multimedia);
    }

    @Override
    public Multimedia findById(Long id) {
        return multimediaRepository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        multimediaRepository.deleteById(id);
    }

    @Override
    public Multimedia update(Multimedia multimedia) {
        return multimediaRepository.save(multimedia);
    }

    @Override
    public List<Multimedia> findAll() {
        return multimediaRepository.findAll();
    }

}

