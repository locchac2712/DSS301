package com.dss.controller;

import com.dss.model.Product;
import com.dss.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.List;
import java.util.Optional;

@Controller // Sử dụng @Controller để trả về tên template (HTML)
public class WebController {

    private final ProductService productService;

    public WebController(ProductService productService) {
        this.productService = productService;
    }


    /**
     * TRANG DASHBOARD MARKETING
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // (Tạm thời, chúng ta sẽ dùng dữ liệu giả cho các bảng)
        // (Sau này, bạn sẽ cần tạo các phương thức service mới, ví dụ:
        //  productService.getTop5Selling() và productService.getLowStock())

        // Lấy 5 sản phẩm đầu tiên làm "Top Selling" (ví dụ)
        List<Product> topProducts = productService.getProducts(null, PageRequest.of(0, 5, Sort.by("id").descending())).getContent();

        // Lấy 5 sản phẩm có tồn kho thấp nhất làm "Low Stock" (ví dụ)
        List<Product> lowStockProducts = productService.getProducts(null, PageRequest.of(0, 5, Sort.by("stockQuantity").ascending())).getContent();

        model.addAttribute("topSellingProducts", topProducts);
        model.addAttribute("lowStockProducts", lowStockProducts);

        // (Dữ liệu cho các KPI và Biểu đồ sẽ được tải bằng API riêng)

        return "dashboard"; // Trả về tệp dashboard.html
    }

    /**
     * 1. READ (Hiển thị Danh sách) - ĐÃ NÂNG CẤP
     * Chấp nhận các tham số:
     * @param keyword (để lọc/tìm kiếm)
     * @param page (số trang hiện tại, bắt đầu từ 1)
     * @param size (số mục trên mỗi trang)
     */
    @GetMapping("/products/list")
    public String showProductList(
            Model model,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {

        // (1) Xử lý Pageable (Spring data page bắt đầu từ 0)
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by("id").ascending()
        );

        // (2) Lấy dữ liệu Page từ Service
        Page<Product> productPage = productService.getProducts(keyword, pageable);

        // (3) Thêm Page object vào Model
        model.addAttribute("productPage", productPage);

        // (4) Thêm từ khóa tìm kiếm vào Model (để giữ lại trên thanh search)
        model.addAttribute("keyword", keyword);

        // (5) Tạo danh sách các số trang để hiển thị trên UI
        int totalPages = productPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "product-list";
    }

    // 2. CREATE (Hiển thị Form Thêm mới) - GET /products/new
    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        // Truyền Product rỗng để form biết liên kết dữ liệu
        model.addAttribute("product", new Product());
        return "product-form";
    }

    // 3. UPDATE (Hiển thị Form Sửa) - GET /products/edit/{id}
    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable("id") String id, Model model) {
        Product product = productService.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // Truyền Product có dữ liệu cũ vào form
        model.addAttribute("product", product);
        return "product-form";
    }

    // 4. CREATE/UPDATE (Xử lý Lưu Form) - POST /products/save
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product) {
        // Nếu product.id có giá trị: UPDATE. Nếu product.id là null: CREATE
        productService.save(product);
        return "redirect:/products/list"; // Chuyển hướng về trang danh sách
    }

    // 5. DELETE (Xử lý Xóa) - POST /products/delete/{id}
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") String id) {
        productService.delete(id);
        return "redirect:/products/list"; // Chuyển hướng về trang danh sách
    }
}