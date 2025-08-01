package net.rolibrt.itp_reminder.controllers;

import jakarta.validation.Valid;
import net.rolibrt.itp_reminder.dtos.DataEntryDto;
import net.rolibrt.itp_reminder.dtos.DataEntryListDto;
import net.rolibrt.itp_reminder.dtos.DataEntrySearchDto;
import net.rolibrt.itp_reminder.models.*;
import net.rolibrt.itp_reminder.services.DataEntryService;
import net.rolibrt.itp_reminder.services.WebUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/entries")
public class DataEntryController {
    private static final Logger logger = LoggerFactory.getLogger(DataEntryController.class);

    private final DataEntryService dataEntryService;
    private final WebUserService webUserService;

    @Autowired
    public DataEntryController(DataEntryService dataEntryService, WebUserService webUserService) {
        this.dataEntryService = dataEntryService;
        this.webUserService = webUserService;
    }

    @GetMapping("/search")
    public String listSearchEntries(@ModelAttribute("search") DataEntrySearchDto search, Model model) {
        int reminderDays = dataEntryService.getReminderDays();
        Page<DataEntryListDto> page = dataEntryService.searchDataEntries(search)
                .map(entry -> new DataEntryListDto(entry, reminderDays));
        model.addAttribute("page", page);
        model.addAttribute("search", search);
        return "entries/list";
    }

    @GetMapping
    public String listEntries(Model model) {
        int reminderDays = dataEntryService.getReminderDays();
        DataEntrySearchDto search = new DataEntrySearchDto();
        Page<DataEntryListDto> page = dataEntryService.searchDataEntries(search)
                .map(entry -> new DataEntryListDto(entry, reminderDays));
        model.addAttribute("page", page);
        model.addAttribute("search", search);
        return "entries/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("entry", new DataEntryDto());
        return "entries/add"; // templates/entries/add.html
    }

    @PostMapping("/add")
    public String addEntry(@ModelAttribute("entry") @Valid DataEntryDto entry,
                           BindingResult result, Model model, Principal principal) {
        if (entry.getPhone() == null || entry.getPhone().isBlank()) {
            result.rejectValue("phone", "error.entry.phone", "A phone number is required!");
        }
        if (entry.getTag() == null || entry.getTag().isBlank()) {
            result.rejectValue("tag", "error.entry.tag1", "A tag is required!");
        } else if (dataEntryService.findByTag(entry.getTag()).isPresent()) {
            result.rejectValue("tag", "error.entry.tag2", "Tag already registered!");
        }
        if (entry.getDate() == null) {
            result.rejectValue("date", "error.entry.date", "A date is required!");
        }
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error ->
                    logger.warn("Add Validation error: {}", error.getDefaultMessage()));
            model.addAttribute("entry", entry);
            return "entries/add";
        }
        WebUser user = webUserService.findByUsername(principal.getName()).orElseThrow();
        DataEntry dataEntry = new DataEntry();
        dataEntry.setPhone(entry.getPhone());
        dataEntry.setTag(entry.getTag());
        dataEntry.setDate(entry.getDate());
        dataEntry.setDuration(Math.min(36, Math.max(entry.getDuration(), 1)));
        dataEntry.setCreatedBy(user);
        dataEntryService.saveEntry(dataEntry);
        return "redirect:/entries";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<DataEntry> optionalEntry = dataEntryService.getEntryById(id);
        if (optionalEntry.isEmpty()) {
            return "redirect:/entries?error=notfound";
        }
        DataEntry entry = optionalEntry.get();
        DataEntryDto dataEntryDto = new DataEntryDto(entry);
        model.addAttribute("entry", dataEntryDto);
        model.addAttribute("entryId", id);
        return "entries/edit"; // templates/entries/edit.html
    }

    @PostMapping("/edit/{id}")
    public String updateEntry(@PathVariable Long id, @ModelAttribute("entry") @Valid DataEntryDto entry,
                              BindingResult result, Model model) {
        if (entry.getPhone() == null || entry.getPhone().isBlank()) {
            result.rejectValue("phone", "error.entry.phone", "A phone number is required!");
        }
        if (entry.getTag() == null || entry.getTag().isBlank()) {
            result.rejectValue("tag", "error.entry.tag", "A tag is required!");
        }
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error ->
                    logger.warn("Edit Validation error: {}", error.getDefaultMessage()));
            model.addAttribute("entry", entry);
            model.addAttribute("entryId", id);
            return "entries/edit";
        }
        Optional<DataEntry> optionalEntry = dataEntryService.getEntryById(id);
        if (optionalEntry.isEmpty()) {
            return "redirect:/entries?error=notfound";
        }
        DataEntry dataEntry = optionalEntry.get();
        dataEntry.setPhone(entry.getPhone());
        if (!dataEntry.getTag().equals(entry.getTag())) {
            if (dataEntryService.findByTag(entry.getTag()).isPresent()) {
                result.rejectValue("tag", "error.entry.tag", "Tag already registered!");
                return "entries/edit";
            }
            dataEntry.setTag(entry.getTag());
        }
        if (entry.getDate() != null) {
            dataEntry.setDate(entry.getDate());
        }
        dataEntry.setDuration(Math.min(36, Math.max(entry.getDuration(), 1)));
        dataEntry.setReminded(entry.isReminded());
        dataEntryService.saveEntry(dataEntry);
        return "redirect:/entries";
    }

    @PostMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id) {
        dataEntryService.deleteEntry(id);
        return "redirect:/entries";
    }
}