package net.rolibrt.itp_reminder.services;

import net.rolibrt.itp_reminder.models.DataEntry;
import net.rolibrt.itp_reminder.dtos.DataEntryListDto;
import net.rolibrt.itp_reminder.dtos.DataEntrySearchDto;
import net.rolibrt.itp_reminder.models.WebUser;
import net.rolibrt.itp_reminder.repositories.DataEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DataEntryService {

    private final DataEntryRepository dataEntryRepository;
    private final SettingsService settingsService;

    @Autowired
    public DataEntryService(DataEntryRepository dataEntryRepository, SettingsService settingsService) {
        this.dataEntryRepository = dataEntryRepository;
        this.settingsService = settingsService;
    }

    public int getReminderDays() {
        return settingsService.getInt("reminder_days", 10);
    }

    public Page<DataEntry> searchDataEntries(DataEntrySearchDto search) {
        int safePage = Math.max(0, search.getPage() - 1);
        int safeSize = Math.min(Math.max(search.getSize(), 1), 100);
        Sort sort = search.getDirection() != null
                && search.getDirection().equalsIgnoreCase("asc")
                ? Sort.by(search.getSortBy()).ascending()
                : Sort.by(search.getSortBy()).descending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        return dataEntryRepository.findAll(DataEntrySearchDto.filter(search), pageable);
    }

    public List<DataEntryListDto> getDueEntries() {
        int reminderDays = getReminderDays() + 1;
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.minusYears(1)
                .plusDays(reminderDays); // today == (createdAt + 1y - 10d)
        return dataEntryRepository.findDue(targetDate).stream()
                .map(entry -> new DataEntryListDto(entry, reminderDays))
                .collect(Collectors.toList());
    }

    @Transactional
    public int updateSomeFieldForEntries(Set<Long> ids, boolean newValue) {
        return dataEntryRepository.updateRemindedByIds(newValue, ids);
    }

    public List<DataEntry> getEntriesByUser(WebUser user) {
        return dataEntryRepository.findByCreatedBy(user);
    }

    public List<DataEntry> findByPhone(String phone) {
        return dataEntryRepository.findByPhone(phone);
    }

    public Optional<DataEntry> findByTag(String tag) {
        return dataEntryRepository.findByTag(tag);
    }

    public Optional<DataEntry> getEntryById(Long id) {
        return dataEntryRepository.findById(id);
    }

    public DataEntry saveEntry(DataEntry entry) {
        return dataEntryRepository.save(entry);
    }

    public void deleteEntry(Long id) {
        dataEntryRepository.deleteById(id);
    }
}
