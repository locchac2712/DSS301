package com.dss.controller;

import com.dss.model.User;
import com.dss.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // Nếu đã đăng nhập, redirect về dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(HttpSession session) {
        // Nếu đã đăng nhập, redirect về dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.login(username, password);
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String role,
            RedirectAttributes redirectAttributes) {
        try {
            userService.register(username, password, role);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage(HttpSession session) {
        // Nếu đã đăng nhập, redirect về dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:/dashboard";
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String username,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra username có tồn tại không
            if (!userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Tên đăng nhập không tồn tại!");
                return "redirect:/forgot-password";
            }

            // TODO: Trong production, nên gửi email chứa mật khẩu mới hoặc link reset
            // Hiện tại chỉ hiển thị thông báo
            redirectAttributes.addFlashAttribute("success",
                    "Yêu cầu đã được gửi! Vui lòng liên hệ quản trị viên để lấy lại mật khẩu.");
            return "redirect:/forgot-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session) {
        // Kiểm tra đăng nhập
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra đăng nhập
            if (session.getAttribute("user") == null) {
                return "redirect:/login";
            }

            String username = (String) session.getAttribute("username");

            // Kiểm tra mật khẩu mới và xác nhận
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận không khớp!");
                return "redirect:/change-password";
            }

            // Kiểm tra độ dài mật khẩu mới
            if (newPassword.length() < 4) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 4 ký tự!");
                return "redirect:/change-password";
            }

            // Đổi mật khẩu
            userService.changePassword(username, oldPassword, newPassword);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/change-password";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/change-password";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đăng xuất thành công!");
        return "redirect:/login";
    }

    // User Profile feature removed as requested
}
