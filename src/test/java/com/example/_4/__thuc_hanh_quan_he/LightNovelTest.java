package com.example._4.__thuc_hanh_quan_he;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;
import org.junit.jupiter.api.*; // Thư viện để chạy Test

import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
public class LightNovelTest {

    @Test
    void testFullFlow() {
        try (Playwright playwright = Playwright.create()) {
            // Bước 1: Mở trình duyệt (hiện lên để mình xem)
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(1000)); // Chạy chậm 1 giây mỗi bước để bạn kịp nhìn

            Page page = browser.newPage();

            // Bước 2: Đi đến trang Dashboard của bạn
            page.navigate("http://localhost:8080/identity/usersController");

            // Bước 3: Dán đoạn code "I'm the bone of my sword" bạn vừa làm vào đây
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Quản lý Truyện")).click();
            page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(" Thêm truyện mới")).click();
            page.getByPlaceholder("Nhập tên truyện...").fill("leesin");
            page.getByPlaceholder("Viết nội dung giới thiệu truy").fill("mù");

            // Các bước chọn thể loại và tác giả
            page.getByText("comedy").first().click();
            page.locator("label").filter(new Locator.FilterOptions().setHasText("yasuo")).click();
            page.locator("#author").selectOption("5");
            page.getByLabel("Đánh dấu là truyện HOT").check();
            // ... (Dán hết các bước click/fill còn lại của bạn vào) ...

            // Bước 4: Lưu xong thì máy báo thành công
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Lưu truyện ngay")).click();

            assertThat(page).hasURL("http://localhost:8080/identity/storiesController"); // Máy kiểm tra xem đã về trang danh sách chưa

            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ket-qua-test.png")));
            System.out.println("Máy đã tự động chạy xong mọi thứ!");

            // Tắt trình duyệt sau khi xong
            browser.close();
        }
    }
}