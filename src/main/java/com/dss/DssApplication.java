package com.dss;

// (1) Import các tệp cần thiết
import com.dss.repository.mongo.RetailDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.Set;

@SpringBootApplication
public class DssApplication {

    public static void main(String[] args) {
        SpringApplication.run(DssApplication.class, args);
    }

    /**
     * (2) Thêm đoạn mã @Bean này vào.
     * Nó sẽ tự động tiêm (inject) Mongo Repository của bạn
     * và chạy code bên trong hàm run().
     */
    @Bean
    CommandLineRunner checkMongoConnection(RetailDataRepository retailDataRepository, MongoTemplate mongoTemplate) {
        return args -> {

            System.out.println("=========================================");
            System.out.println("====== KIỂM TRA KẾT NỐI MONGODB ======");

            try {
                // Lấy tên database hiện tại
                String dbName = mongoTemplate.getDb().getName();
                System.out.println("[INFO] Database đang sử dụng: " + dbName);

                // Liệt kê tất cả collections trong database hiện tại
                Set<String> collections = mongoTemplate.getCollectionNames();
                System.out.println("[INFO] Tất cả collections trong database '" + dbName + "': " + collections);

                // (3) Đếm số lượng tài liệu (document) trong collection
                long count = retailDataRepository.count();

                System.out.println("[SUCCESS] Kết nối MongoDB thành công!");
                System.out.println("[INFO] Tìm thấy " + count + " bản ghi trong collection 'InvoiceDataset'.");

                if (count == 0) {
                    System.out.println("\n[WARNING] Kết nối thành công, nhưng không tìm thấy dữ liệu!");
                    System.out.println("[DEBUG] Kiểm tra:");
                    System.out.println("  1. Database hiện tại: " + dbName);
                    System.out.println(
                            "  2. Collection 'InvoiceDataset' có tồn tại: " + collections.contains("InvoiceDataset"));
                    System.out.println("  3. Nếu dữ liệu ở database khác, hãy sửa trong application.properties:");
                    System.out.println("     spring.data.mongodb.uri=mongodb+srv://.../<TEN_DATABASE>");
                    System.out.println(
                            "  4. Nếu collection có tên khác, hãy sửa @Document(collection = \"...\") trong RetailData.java");
                }

            } catch (Exception e) {
                System.out.println("[FAILED] KHÔNG THỂ KẾT NỐI MONGODB.");
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            System.out.println("=========================================");
        };
    }
}