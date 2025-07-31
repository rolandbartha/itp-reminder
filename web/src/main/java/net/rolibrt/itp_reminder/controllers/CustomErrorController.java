package net.rolibrt.itp_reminder.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class CustomErrorController implements ErrorController {

    @GetMapping("/account-closed")
    public String accountClosed() {
        return "error/account-closed";
    }

    @GetMapping("/account-locked")
    public String accountLocked() {
        return "error/account-locked";
    }

    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }

    @PostMapping("/403")
    public String errorPost403() {
        return "redirect:/error/403";
    }
}