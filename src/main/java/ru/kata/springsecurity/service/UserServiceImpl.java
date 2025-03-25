package ru.kata.springsecurity.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.springsecurity.entity.User;
import ru.kata.springsecurity.entity.Role;
import ru.kata.springsecurity.repository.RoleRepository;
import ru.kata.springsecurity.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           RoleService roleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        if (!user.getPassword().startsWith("$2a$")) { // Проверяем, зашифрован ли пароль
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of(roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена"))));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void update(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        existingUser.setUsername(user.getUsername());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setAge(user.getAge());
        existingUser.setEmail(user.getEmail());

        if (user.getPassword() != null && !user.getPassword().isBlank() && !user.getPassword().startsWith("$2a$")) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Role> updatedRoles = user.getRoles().stream()
                    .map(role -> roleService.findById(role.getId())
                            .orElseThrow(() -> new RuntimeException("Роль не найдена"))).collect(Collectors.toSet());
            existingUser.setRoles(updatedRoles);
        }
        userRepository.save(existingUser);
    }

    @Transactional
    @Override
    public void registerNewUser(String username, String password, String firstName, String lastName, int age, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Пользователь уже существует!");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));  // ✅ Кодируем пароль в сервисе
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена"));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);
    }

    @Transactional
    @Override
    public void createUserWithRoles(User user, List<String> roleIds) {
        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));  // ✅ Кодируем пароль в сервисе
        }

        Set<Role> roles = (roleIds != null) ? roleIds.stream()
                .map(Long::valueOf)
                .map(roleService::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet()) : new HashSet<>();

        user.setRoles(roles);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void updateUserWithRoles(User formUser, List<Long> roleIds) {
        User user = userRepository.findById(formUser.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setUsername(formUser.getUsername());
        user.setFirstName(formUser.getFirstName());
        user.setLastName(formUser.getLastName());
        user.setAge(formUser.getAge());
        user.setEmail(formUser.getEmail());

        if (formUser.getPassword() != null && !formUser.getPassword().isBlank() && !formUser.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(formUser.getPassword()));  // ✅ Пароль шифруем в сервисе
        }

        if (roleIds != null) {
            Set<Role> updatedRoles = roleIds.stream()
                    .map(roleService::findById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toSet());
            user.setRoles(updatedRoles);
        }

        userRepository.save(user);
    }

}
