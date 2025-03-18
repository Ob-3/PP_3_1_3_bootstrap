package ru.kata.springsecurity.controllers;

import org.springframework.web.bind.annotation.*;
import ru.kata.springsecurity.entity.User;
import ru.kata.springsecurity.entity.Role;
import ru.kata.springsecurity.service.RoleService;
import ru.kata.springsecurity.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.HashSet;
import java.util.Optional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String redirectToUsers() {
        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", userService.findAll());
        return "users-list";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());
        return "edit-user";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user, @RequestParam(value = "roles", required = false) List<Long> roleId) {

        System.out.println("Полученные роли: " + roleId); //дебажим ошибку

        Set<Role> roles = (roleId != null) ?
                roleId.stream()
                        .map(roleService::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet())
                : new HashSet<>();

        System.out.println("Назначаемые роли: " + roles);

        user.setRoles(roles);
        userService.update(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll()); // Получаем все роли
        return "add-user";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user, @RequestParam(value = "roles", required = false) List<Long> roleId) {
        Set<Role> roles = (roleId != null) ?
                roleId.stream()
                        .map(roleService::findById)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toSet())
                : new HashSet<>();

        user.setRoles(roles);
        userService.save(user);
        return "redirect:/admin/users";
    }

}
