package com.dss.service;

import com.dss.model.User;
import com.dss.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAll() {
        return repo.findAll();
    }

    /**
     * Lấy một TRANG (Page) người dùng với phân trang
     */
    public Page<User> getUsers(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /**
     * Lấy người dùng theo ID
     */
    public Optional<User> getById(Long id) {
        return repo.findById(id);
    }
}
