package ru.kata.springsecurity.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.kata.springsecurity.entity.User;
import ru.kata.springsecurity.security.CustomUserDetails;
import ru.kata.springsecurity.service.RoleService;
import ru.kata.springsecurity.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.List;

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
    public String userList(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("newUser", new User()); // Новый пользователь для формы
        model.addAttribute("roles", roleService.findAll()); // Добавляем список ролей
        model.addAttribute("currentUser", userDetails.getUser()); // Передаем текущего пользователя
        return "admin-panel";
    }


    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user, @RequestParam(value = "roles", required = false) List<String> roleIds) {
        userService.createUserWithRoles(user, roleIds);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user, @RequestParam(value = "roles", required = false) List<Long> roleIds) {
        userService.updateUserWithRoles(user, roleIds);
        return "redirect:/admin/users";
    }


    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }
}
