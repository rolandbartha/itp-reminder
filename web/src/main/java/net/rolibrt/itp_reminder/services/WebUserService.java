package net.rolibrt.itp_reminder.services;

import net.rolibrt.itp_reminder.dtos.WebUserSearchDto;
import net.rolibrt.itp_reminder.models.*;
import net.rolibrt.itp_reminder.repositories.WebUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WebUserService {
    private final WebUserRepository webUserRepository;

    @Autowired
    public WebUserService(WebUserRepository webUserRepository) {
        this.webUserRepository = webUserRepository;
    }


    public Page<WebUser> searchDataEntries(WebUserSearchDto search) {
        int safePage = Math.max(0, search.getPage() - 1);
        int safeSize = Math.min(Math.max(search.getSize(), 1), 100);
        Sort sort = search.getDirection() != null
                && search.getDirection().equalsIgnoreCase("asc")
                ? Sort.by(search.getSortBy()).ascending()
                : Sort.by(search.getSortBy()).descending();
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        return webUserRepository.findAll(WebUserSearchDto.filter(search), pageable);
    }

    public Optional<WebUser> findById(Long id) {
        return webUserRepository.findById(id);
    }

    public WebUser saveUser(WebUser user) {
        return webUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        webUserRepository.deleteById(id);
    }

    public Optional<WebUser> findByUsername(String username) {
        return webUserRepository.findByUsername(username);
    }

    public Optional<WebUser> findByEmail(String email) {
        return webUserRepository.findByEmail(email);
    }
}