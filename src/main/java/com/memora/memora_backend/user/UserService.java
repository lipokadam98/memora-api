package com.memora.memora_backend.user;

import com.memora.memora_backend.user.models.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    User save(User user);
    User update(User user);
    void delete(Long id);
}
