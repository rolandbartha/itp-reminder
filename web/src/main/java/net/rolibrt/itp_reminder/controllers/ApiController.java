package net.rolibrt.itp_reminder.controllers;

import net.rolibrt.itp_reminder.dtos.DataEntryListDto;
import net.rolibrt.itp_reminder.dtos.DataEntryRemindedDto;
import net.rolibrt.itp_reminder.services.DataEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final DataEntryService dataEntryService;

    @Autowired
    public ApiController(DataEntryService dataEntryService) {
        this.dataEntryService = dataEntryService;
    }

    @GetMapping("/api/entries")
    public List<DataEntryListDto> listEntries() {
        return dataEntryService.getDueEntries();
    }

    @PostMapping("/api/update")
    public ResponseEntity<Void> updateEntries(@RequestBody DataEntryRemindedDto entry) {
        logger.info("Received update");
        Set<Long> ids = entry.getIds();
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        int updates = dataEntryService.updateSomeFieldForEntries(ids, true);
        logger.info("Updated {} entries from REST API", updates);
        return ResponseEntity.ok().build();
    }
}
