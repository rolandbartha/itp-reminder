package net.rolibrt.itp_reminder.controllers;

import jakarta.validation.Valid;
import net.rolibrt.itp_reminder.dtos.WebUserDto;
import net.rolibrt.itp_reminder.dtos.WebUserListDto;
import net.rolibrt.itp_reminder.dtos.WebUserSearchDto;
import net.rolibrt.itp_reminder.models.*;
import net.rolibrt.itp_reminder.services.WebUserService;
import net.rolibrt.itp_reminder.utils.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class WebUserController {
    private static final Logger logger = LoggerFactory.getLogger(WebUserController.class);
    private final WebUserService webUserService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public WebUserController(WebUserService webUserService, BCryptPasswordEncoder passwordEncoder) {
        this.webUserService = webUserService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/search")
    public String listSearchUsers(@ModelAttribute("search") WebUserSearchDto search, Model model) {
        Page<WebUserListDto> page = webUserService.searchDataEntries(search)
                .map(WebUserListDto::new);
        model.addAttribute("page", page);
        model.addAttribute("search", search);
        return "users/list";
    }

    @GetMapping
    public String listUsers(Model model) {
        WebUserSearchDto search = new WebUserSearchDto();
        Page<WebUserListDto> page = webUserService.searchDataEntries(search)
                .map(WebUserListDto::new);
        model.addAttribute("page", page);
        model.addAttribute("search", search);
        return "users/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new WebUserDto());
        model.addAttribute("roles", List.of(Role.values()));
        return "users/add";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute("user") @Valid WebUserDto user,
                          BindingResult result, Model model, Principal principal) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            result.rejectValue("password", "error.user", "A password is required.");
        } else if (PasswordValidator.isWeakPassword(user.getPassword())) {
            result.rejectValue("password", "error.user", "Password is too weak.");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            result.rejectValue("email", "error.user", "An email is required.");
        } else if (webUserService.findByEmail(user.getEmail()).isPresent()) {
            result.rejectValue("email", "error.user", "Email already taken!");
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            if (webUserService.findByUsername(user.getUsername()).isPresent()) {
                result.rejectValue("username", "error.user", "Username already taken!");
            }
        } else {
            result.rejectValue("username", "error.user", "Username must be provided!");
        }
        model.addAttribute("roles", List.of(Role.values()));
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error ->
                    logger.warn("Add Validation error: {}", error.getDefaultMessage()));
            model.addAttribute("user", user);
            return "users/add";
        }
        WebUser admin = webUserService.findByUsername(principal.getName()).orElseThrow();
        if (!admin.getRoles().contains(Role.ADMIN)) {
            return "redirect:/users";
        }
        WebUser webUser = new WebUser();
        webUser.setUsername(user.getUsername());
        webUser.setEmail(user.getEmail());
        webUser.setPassword(passwordEncoder.encode(user.getPassword()));
        webUser.setRoles(user.getRoles());
        webUser.setClosed(user.isClosed());
        logger.info("{} created user {} with roles {}", admin.getUsername(), webUser.getUsername(), webUser.getRoleList());
        webUserService.saveUser(webUser);
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<WebUser> optionalWebUser = webUserService.findById(id);
        if (optionalWebUser.isEmpty()) {
            // User not found, redirect or show error
            return "redirect:/users?error=notfound";
        }

        WebUser webUser = optionalWebUser.get();

        // Map existing user to DTO (for form binding)
        WebUserDto user = new WebUserDto();
        user.setUsername(webUser.getUsername());
        user.setEmail(webUser.getEmail());
        // Password left empty for security reasons; user can enter new password to update
        user.setRoles(webUser.getRoles());
        user.setLocked(webUser.isLocked());
        user.setClosed(webUser.isClosed());

        // Add DTO and roles list to model for the form
        model.addAttribute("user", user);
        model.addAttribute("userId", id);
        model.addAttribute("roles", List.of(Role.values()));

        // Return the Thymeleaf template name for edit user form
        return "users/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute("user") @Valid WebUserDto user,
                             BindingResult result, Model model, Principal principal) {
        if (user.getPassword() != null && !user.getPassword().isBlank() && PasswordValidator.isWeakPassword(user.getPassword())) {
            result.rejectValue("password", "error.user.password", "Password is too weak.");
        }
        model.addAttribute("roles", List.of(Role.values()));
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error ->
                    logger.warn("Edit Validation error: {}", error.getDefaultMessage()));
            model.addAttribute("user", user);
            model.addAttribute("userId", id);
            return "users/edit";
        }
        WebUser admin = webUserService.findByUsername(principal.getName()).orElseThrow();
        if (!admin.getRoles().contains(Role.ADMIN)) {
            return "redirect:/users";
        }
        Optional<WebUser> webUserOptional = webUserService.findById(id);
        if (webUserOptional.isEmpty()) {
            return "redirect:/users?error=notfound";
        }
        WebUser webUser = webUserOptional.get();
        if ((Objects.equals(admin.getId(), id) || webUser.getRoles().contains(Role.ADMIN)) && !user.getRoles().contains(Role.ADMIN)) {
            return "redirect:/users";
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            if (webUser.getUsername() == null || !webUser.getUsername().equals(user.getUsername())) {
                if (webUserService.findByUsername(user.getUsername()).isPresent()) {
                    result.rejectValue("username", "error.user.username", "Username already taken!");
                    model.addAttribute("user", user);
                    model.addAttribute("userId", id);
                    return "users/edit";
                }
            }
            webUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (webUser.getEmail() == null || !webUser.getEmail().equals(user.getEmail())) {
                if (webUserService.findByEmail(user.getEmail()).isPresent()) {
                    result.rejectValue("email", "error.user.email", "Email already taken!");
                    model.addAttribute("user", user);
                    model.addAttribute("userId", id);
                    return "users/edit";
                }
                webUser.setEmail(user.getEmail());
            }
        }
        if (user.getPassword() != null && !user.getPassword().isBlank())
            webUser.setPassword(passwordEncoder.encode(user.getPassword()));
        webUser.setRoles(user.getRoles());
        if (webUser.isLocked() && !user.isLocked()) {
            webUser.setFailedLoginAttempts(0);
            webUser.setFailed2FAAttempts(0);
            webUser.setLockTime(null);
        }
        webUser.setLocked(user.isLocked());
        webUser.setClosed(user.isClosed());
        logger.info("{} updated user {} with roles {}", admin.getUsername(), webUser.getUsername(), webUser.getRoleList());
        webUserService.saveUser(webUser);
        return "redirect:/users";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        webUserService.deleteUser(id);
        return "redirect:/users";
    }
}