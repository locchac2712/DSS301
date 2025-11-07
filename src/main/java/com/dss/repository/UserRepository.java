package com.dss.repository;

import com.dss.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * UserRepository là interface JpaRepository để thao tác với bảng users trong cơ
 * sở dữ liệu.
 * Nó kế thừa từ JpaRepository và có các phương thức để thao tác với bảng users.
 * 
 * @author DSS301
 * @version 1.0
 * @since 2025-11-07
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm kiếm user theo username
    Optional<User> findByUsername(String username);

    // Kiểm tra xem username có tồn tại không
    boolean existsByUsername(String username);
}
