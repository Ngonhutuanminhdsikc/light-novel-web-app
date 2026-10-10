package com.example._4.__thuc_hanh_quan_he.test.dashboard;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminDashboardStatsTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
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
    @DisplayName("TC_DASHBOARD_01: Kiểm tra hiển thị các khối thẻ số liệu và biểu đồ Chart.js thành công")
    void testDashboardStatisticsRender() {

        page.navigate("http://localhost:8080/login");
        page.locator("input[name='username']").fill("admin");
        page.locator("input[name='password']").fill("1234566");
        page.locator("button:has-text('ĐĂNG NHẬP')").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        page.navigate("http://localhost:8080/admin/statistics");

        Locator mainHeading = page.locator("h1");
        assertTrue(mainHeading.isVisible(), "Trang thống kê không tải được!");
        assertTrue(mainHeading.innerText().contains("Thống kê"), "Sai tiêu đề trang!");

        assertTrue(page.locator(".card:has-text('Tổng số truyện')").isVisible(),
                "Khối thống kê 'Tổng số truyện' không hiển thị!");

        Locator chartCanvas = page.locator("#storyStatusChart");
        chartCanvas.waitFor();
        assertTrue(chartCanvas.isVisible(), "Biểu đồ không hiển thị!");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-admin-dashboard-stats.png")));
    }
}