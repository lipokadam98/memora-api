package com.memora.memora_backend.user;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    User update(User user);
    void delete(Long id);
}
