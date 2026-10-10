package com.example._4.__thuc_hanh_quan_he;

import com.microsoft.playwright.*;

public class PlaywrightTest {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(500));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            try {
                page.navigate("http://localhost:8080/identity/usersController");
                // Khi lệnh này chạy, bạn cứ thao tác thoải mái.
                // Khi xong việc, hãy nhấn nút "Resume" (hình tam giác) trong Inspector
                // thay vì tắt trình duyệt đột ngột.
                page.pause();
            } catch (PlaywrightException e) {
                System.out.println("Trình duyệt đã đóng bởi người dùng.");
            }
        }

    }
}