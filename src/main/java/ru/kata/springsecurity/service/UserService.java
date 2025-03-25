package ru.kata.springsecurity.service;

import jakarta.transaction.Transactional;
import ru.kata.springsecurity.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User>  findById(Long id);
    Optional<User> findByUsername(String username);
    User save(User user);
    void update(User user);
    void deleteById(Long id);

    @Transactional
    void registerNewUser(String username, String password, String firstName, String lastName, int age, String email);

    @Transactional
    void createUserWithRoles(User user, List<String> roleIds);

    @Transactional
    void updateUserWithRoles(User formUser, List<Long> roleIds);
}
