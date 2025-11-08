package com.dss.service;

import com.dss.model.User;
import com.dss.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * UserService là service để thao tác với bảng users trong cơ sở dữ liệu
 * (PostgreSQL).
 * Nó có các phương thức để thao tác với bảng users.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */

@Service
public class UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repo) {
        this.repo = repo;
        this.passwordEncoder = new BCryptPasswordEncoder();
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

    public Optional<User> findByUsername(String username) {
        return repo.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return repo.existsByUsername(username);
    }

    public User register(String username, String password, String role) {
        if (existsByUsername(username)) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        User user = new User();
        user.setUsername(username);
        // Hash password bằng BCrypt
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        return repo.save(user);
    }

    public User login(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Username không tồn tại!");
        }

        User user = userOpt.get();
        String storedPassword = user.getPassword();

        // Kiểm tra xem password đã được hash bằng BCrypt chưa
        // BCrypt hash luôn bắt đầu với $2a$, $2b$, hoặc $2y$
        boolean isBcryptHash = storedPassword.startsWith("$2a$") ||
                storedPassword.startsWith("$2b$") ||
                storedPassword.startsWith("$2y$");

        boolean passwordMatches;
        if (isBcryptHash) {
            // So sánh password đã hash bằng BCrypt
            passwordMatches = passwordEncoder.matches(password, storedPassword);
        } else {
            // Backward compatibility: so sánh plain text (cho các user cũ)
            passwordMatches = storedPassword.equals(password);

            // Tự động migrate password sang BCrypt sau khi đăng nhập thành công
            if (passwordMatches) {
                user.setPassword(passwordEncoder.encode(password));
                repo.save(user);
            }
        }

        if (!passwordMatches) {
            throw new RuntimeException("Mật khẩu không đúng!");
        }

        return user;
    }

    public User changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại!");
        }

        User user = userOpt.get();
        String storedPassword = user.getPassword();

        // Kiểm tra xem password đã được hash bằng BCrypt chưa
        boolean isBcryptHash = storedPassword.startsWith("$2a$") ||
                storedPassword.startsWith("$2b$") ||
                storedPassword.startsWith("$2y$");

        // Kiểm tra mật khẩu cũ
        boolean oldPasswordMatches;
        if (isBcryptHash) {
            oldPasswordMatches = passwordEncoder.matches(oldPassword, storedPassword);
        } else {
            // Backward compatibility: so sánh plain text
            oldPasswordMatches = storedPassword.equals(oldPassword);
        }

        if (!oldPasswordMatches) {
            throw new RuntimeException("Mật khẩu cũ không đúng!");
        }

        // Kiểm tra mật khẩu mới
        if (newPassword == null || newPassword.length() < 4) {
            throw new RuntimeException("Mật khẩu mới phải có ít nhất 4 ký tự!");
        }

        // Hash và cập nhật mật khẩu mới bằng BCrypt
        user.setPassword(passwordEncoder.encode(newPassword));

        return repo.save(user);
    }
}
