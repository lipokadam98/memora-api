package com.memora.memora_backend.user;

public interface UserService {
    User findByEmail(String email);
    User save(User user);
    User update(User user);
    void delete(Long id);
}
