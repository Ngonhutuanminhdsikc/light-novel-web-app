package com.example._4.__thuc_hanh_quan_he;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;

// Quan trọng: Phải import hàm static này để dùng được lệnh assertTrue
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoryFormValidationTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        // SỬA LỖI: Cú pháp chuẩn là new BrowserType.LaunchOptions() chứ không phải cấu trúc lồng .LaunchOptions() cũ
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    @DisplayName("TC_STORY_02: Kiểm tra bắt lỗi khi để trống thông tin Đăng truyện mới")
    void testEmptyStoryFormValidation() {
        // 1. Điều hướng đến trang Đăng truyện mới
        page.navigate("http://localhost:8080/user/stories/add");

        // 2. Cố tình để trống trường "Tên truyện" và "Slug" (Giả sử id của các input trong HTML là id="title" và id="slug")
        page.locator("#title").fill("");
        page.locator("#slug").fill("");

        // 3. Click vào nút "Đăng truyện" bằng cách tìm văn bản hiển thị trên button
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Đăng truyện")).click();

        // 4. Định vị phần tử lỗi chữ đỏ (invalid-feedback) khớp với nội dung thông báo trong Entity của bạn
        // Lưu ý: Sửa lại nội dung chuỗi ký tự khớp chính xác 100% với `@NotBlank(message = "...")` bạn đã viết trong file Story.java
        Locator titleError = page.locator(".invalid-feedback:has-text('Tên bộ truyện không được để trống!')");
        Locator slugError = page.locator(".invalid-feedback:has-text('Slug của truyện không được để trống!')");

        // 5. Khẳng định kết quả: Bắt buộc các câu thông báo lỗi phải hiển thị trên màn hình
        assertTrue(titleError.isVisible(), "Thông báo lỗi trống Tên truyện chưa hiển thị!");
        assertTrue(slugError.isVisible(), "Thông báo lỗi trống Slug chưa hiển thị!");

        // 6. Tự động chụp ảnh giao diện lỗi lưu vào thư mục target của dự án để lấy làm minh chứng (Evidence) cho báo cáo
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-empty-story-error.png")));
    }
}