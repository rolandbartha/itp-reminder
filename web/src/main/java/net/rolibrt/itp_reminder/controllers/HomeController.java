package net.rolibrt.itp_reminder.controllers;

import net.rolibrt.itp_reminder.services.DataEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final DataEntryService dataEntryService;

    @Autowired
    public HomeController(DataEntryService dataEntryService) {
        this.dataEntryService = dataEntryService;
    }

    @GetMapping
    public String listEntries(Model model) {
        model.addAttribute("entries", dataEntryService.getDueEntries());
        return "home";
    }
}
