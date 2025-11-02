package com.dss;

// (1) Import các tệp cần thiết
import com.dss.repository.mongo.RetailDataRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
    CommandLineRunner checkMongoConnection(RetailDataRepository retailDataRepository) {
        return args -> {

            System.out.println("=========================================");
            System.out.println("====== KIỂM TRA KẾT NỐI MONGODB ======");

            try {
                // (3) Đếm số lượng tài liệu (document) trong collection
                long count = retailDataRepository.count();

                System.out.println("[SUCCESS] Kết nối MongoDB thành công!");
                System.out.println("[INFO] Tìm thấy " + count + " bản ghi trong collection 'InvoiceDataset'.");

                if (count == 0) {
                    System.out.println("[WARNING] Kết nối thành công, nhưng không tìm thấy dữ liệu. Bạn đã import tệp CSV (Bước 3) chưa?");
                }

            } catch (Exception e) {
                System.out.println("[FAILED] KHÔNG THỂ KẾT NỐI MONGODB.");
                System.err.println(e.getMessage());
            }

            System.out.println("=========================================");
        };
    }
}