# 📚 Light Novel Reading \& Management Platform

[!\[Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat\&logo=openid)](https://www.oracle.com/java/)
[!\[Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg?style=flat\&logo=springboot)](https://spring.io/projects/spring-boot)
[!\[Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-blue.svg?style=flat\&logo=springsecurity)](https://spring.io/projects/spring-security)
[!\[MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg?style=flat\&logo=mysql)](https://www.mysql.com/)
[!\[License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

Nền tảng ứng dụng Web trực tuyến phục vụ việc đọc, theo dõi và quản lý tiểu thuyết (Light Novel). Hệ thống được thiết kế và phát triển theo kiến trúc MVC chuẩn mực, tối ưu hóa hiệu năng truy vấn cơ sở dữ liệu và chú trọng tích hợp các cơ chế bảo mật phân quyền người dùng.

\---

## 🚀 Tính năng nổi bật (Key Features)

### 📖 Dành cho Độc giả (User Features)

* **Đọc \& Trải nghiệm:** Giao diện đọc truyện tối ưu, hỗ trợ chuyển chương nhanh chóng.
* **Tương tác bất đồng bộ (AJAX):** Xử lý các thao tác tải dữ liệu, bình luận/đánh giá không cần tải lại trang.
* **Theo dõi \& Lịch sử:** Lưu trữ danh sách truyện yêu thích và tiến trình đọc cá nhân.

### 🔐 Bảo mật \& Phân quyền (Security \& Access Control)

* **Spring Security Authentication/Authorization:** Phân quyền chặt chẽ giữa các vai trò (Admin, Content Manager, Member).
* **An toàn dữ liệu:** Triển khai cơ chế phòng chống các lỗ hổng bảo mật phổ biến (như IDOR, CSRF) nhằm bảo vệ tài nguyên và dữ liệu người dùng.

### 🛠️ Quản trị hệ thống (Admin \& Management)

* **Quản lý danh mục \& Chương:** Cung cấp bộ công cụ CRUD hỗ trợ quản lý tiểu thuyết, danh mục, chương đọc (chapters) hiệu quả.
* **Tối ưu hóa truy vấn:** Sử dụng Spring Data JPA / Hibernate kết hợp đánh chỉ mục (indexing) để xử lý nhanh các luồng dữ liệu lớn.

\---

## 🛠️ Công nghệ sử dụng (Tech Stack)

|Thành phần|Công nghệ / Framework|
|-|-|
|**Ngôn ngữ lập trình**|Java 21|
|**Backend Framework**|Spring Boot 3.x (Spring MVC, Spring Data JPA, Spring Security)|
|**Database \& ORM**|MySQL 8.0, Hibernate|
|**Frontend \& Template**|Thymeleaf, HTML5, CSS3, JavaScript (AJAX, Fetch API)|
|**Build Tool \& Git**|Maven, Git / GitHub|

\---

## 📐 Kiến trúc hệ thống (Architecture \& Design Pattern)

Hệ thống được thiết kế tuân thủ mô hình **MVC (Model - View - Controller)** kết hợp với **Layered Architecture**:

```text
\[ Client / Browser ] 
        │
        ▼ (HTTP Requests / AJAX)
\[ Controller Layer ] ── (Handles Routing \& Validation)
        │
        ▼
\[ Service Layer ]    ── (Business Logic \& Security Rules)
        │
        ▼
\[ Repository Layer ] ── (Spring Data JPA / Hibernate)
        │
        ▼
\[ Database Layer ]   ── (MySQL Database)
```

\---

## ⚙️ Hướng dẫn cài đặt \& Chạy ứng dụng (Getting Started)

### Yêu cầu tiên quyết (Prerequisites)

* **JDK:** Java 21 trở lên
* **Database:** MySQL Server 8.0+
* **Build Tool:** Apache Maven 3.8+

### Các bước khởi chạy (Installation Steps)

1. **Clone dự án về máy:**

```bash
   git clone https://github.com/NgoNhuTuanMinh/light-novel-web-app.git
   cd light-novel-web-app
   ```

2. **Cấu hình CSDL (`application.properties`):**
Mở file `src/main/resources/application.properties` và cập nhật thông số kết nối MySQL của bạn:

```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/light\_novel\_db?useSSL=false\&serverTimezone=UTC
   spring.datasource.username=YOUR\_MYSQL\_USERNAME
   spring.datasource.password=YOUR\_MYSQL\_PASSWORD
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Biên dịch và chạy ứng dụng:**

```bash
   # Sử dụng Maven Wrapper
   ./mvnw spring-boot:run
   ```

4. **Truy cập ứng dụng:**
Mở trình duyệt và truy cập: `http://localhost:8080`

\---

## 👤 Tác giả (Author)

* **Ngô Như Tuấn Minh**
* **Trường:** Đại học Tài nguyên và Môi trường Hà Nội (HUNRE)
* **Chuyên ngành:** Công nghệ thông tin
* **Email:** ngotuanminh205@gmail.com
* **Phone:** 0967208473
* **GitHub:** [NgoNhuTuanMinh](https://github.com/NgoNhuTuanMinh)

