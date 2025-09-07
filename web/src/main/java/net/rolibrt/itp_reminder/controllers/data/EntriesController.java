package net.rolibrt.itp_reminder.controllers.data;

import com.alibaba.excel.EasyExcel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import net.rolibrt.itp_reminder.dtos.DataEntryCSV;
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
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/entries")
public class EntriesController {
    private static final Logger logger = LoggerFactory.getLogger(EntriesController.class);

    private final DataEntryService dataEntryService;
    private final WebUserService webUserService;

    @Autowired
    public EntriesController(DataEntryService dataEntryService, WebUserService webUserService) {
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
        dataEntry.setDuration(Math.min(24, Math.max(entry.getDuration(), 1)));
        dataEntry.setCreatedBy(user);
        dataEntryService.save(dataEntry);
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
        dataEntry.setDuration(Math.min(24, Math.max(entry.getDuration(), 1)));
        dataEntry.setReminded(entry.isReminded());
        dataEntryService.save(dataEntry);
        return "redirect:/entries";
    }

    @PostMapping("/delete/{id}")
    public String deleteEntry(@PathVariable Long id) {
        dataEntryService.delete(id);
        return "redirect:/entries";
    }

    @PostMapping("/export")
    public void exportEntries(@RequestParam List<Long> ids, HttpServletResponse response) throws Exception {
        List<DataEntryCSV> dataEntries = dataEntryService.findAllById(ids).stream().map(DataEntryCSV::new).toList();
        if (dataEntries.isEmpty()) return;
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"exported-selected-entries.xlsx\"");
        EasyExcel.write(response.getOutputStream(), DataEntryCSV.class)
                .sheet("Entries")
                .doWrite(dataEntries);
    }

    @PostMapping("/export-all")
    public void exportAllEntries(HttpServletResponse response) throws Exception {
        List<DataEntryCSV> dataEntries = dataEntryService.findAll().stream().map(DataEntryCSV::new).toList();
        if (dataEntries.isEmpty()) return;
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"exported-all-entries.xlsx\"");
        EasyExcel.write(response.getOutputStream(), DataEntryCSV.class)
                .sheet("Entries")
                .doWrite(dataEntries);
    }

    @PostMapping("/import")
    public String importEntries(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }
        List<DataEntryCSV> entries = EasyExcel.read(file.getInputStream())
                .head(DataEntryCSV.class)
                .sheet()
                .doReadSync();
        Map<Long, DataEntryCSV> map = entries.stream().collect(Collectors.toMap(DataEntryCSV::getId, e -> e));

        Map<Long, DataEntry> entriesPresent = dataEntryService
                .findAllById(map.keySet().stream()
                        .toList()).stream().collect(Collectors.toMap(DataEntry::getId, e -> e));
        List<DataEntry> entriesUpdate = new ArrayList<>(map.size());
        for (Map.Entry<Long, DataEntryCSV> csvEntry : map.entrySet()) {
            DataEntry entry;
            if (entriesPresent.containsKey(csvEntry.getKey())) {
                entry = entriesPresent.get(csvEntry.getKey());
                boolean creatorUpdate = entry.getCreatedBy() != null &&
                        !entry.getCreatedBy().getUsername().equals(csvEntry.getValue().getCreator());
                if (entry.importCSV(csvEntry.getValue())) {
                    if (creatorUpdate) {
                        webUserService.findByUsername(csvEntry.getValue().getCreator()).ifPresent(entry::setCreatedBy);
                    }
                }
            } else {
                entry = new DataEntry();
                if (entry.importCSV(csvEntry.getValue())) {
                    webUserService.findByUsername(csvEntry.getValue().getCreator()).ifPresent(entry::setCreatedBy);
                }
            }
            entriesUpdate.add(entry);
        }
        dataEntryService.saveAll(entriesUpdate);
        return "redirect:/entries";
    }
}