package net.rolibrt.itp_reminder.controllers;

import net.rolibrt.itp_reminder.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private SettingsService settingsService;

    @GetMapping("/settings")
    public String listSettings(Model model) {
        model.addAttribute("settings", settingsService.getAllSettings());
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String updateSettings(@RequestParam Map<String, String> allParams) {
        settingsService.updateSettings(allParams);
        return "redirect:/admin/settings?success";
    }
}
