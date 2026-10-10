package com.example._4.__thuc_hanh_quan_he.test.genre;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminGenreTest {
    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100));

        try { Files.createDirectories(Paths.get("target/screenshots")); } catch (Exception e) {}
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setup() { page = browser.newPage(); }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        String fileName = testInfo.getDisplayName().replaceAll("[^a-zA-Z0-9.-]", "_") + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/screenshots/" + fileName)));
        page.close();
    }

    private void loginAsAdmin() {
        page.navigate("http://localhost:8080/login");
        page.locator("input[name='username']").fill("admin");
        page.locator("input[name='password']").fill("1234566");
        page.locator("button:has-text('ĐĂNG NHẬP')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Test
    @Order(1)
    @DisplayName("testAddGenreSuccess")
    void testAddGenreSuccess() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/tagsController");
        page.locator("a:has-text('Thêm Tag mới')").click();

        page.locator("#name").fill("Isekai Trọng Sinh");
        page.locator("#slug").fill("isekai-trong-sinh");
        page.locator("button:has-text('Lưu lại')").click();

        page.waitForURL("**/tagsController");
        assertTrue(page.locator("text=Isekai Trọng Sinh").isVisible());
    }

    @Test
    @Order(2)
    @DisplayName("testEditGenreSuccess")
    void testEditGenreSuccess() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/tagsController");

        page.locator("tr:has-text('Isekai Trọng Sinh')").locator("a.btn-outline-warning").click();

        page.locator("#name").clear();
        page.locator("#name").fill("Isekai Chuyển Sinh");
        page.locator("button:has-text('Cập nhật')").click();

        page.waitForURL("**/tagsController");
        assertTrue(page.locator("text=Isekai Chuyển Sinh").isVisible());
    }

    @Test
    @Order(3)
    @DisplayName("testDuplicateGenreFail")
    void testDuplicateGenreFail() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/tagsController/add");

        page.locator("#name").fill("Isekai Chuyển Sinh");
        page.locator("#slug").fill("trung-lap");
        page.locator("button:has-text('Lưu lại')").click();

        assertTrue(page.locator(".alert-danger").isVisible());
    }

    @Test
    @Order(4)
    @DisplayName("testDeleteGenreSuccess")
    void testDeleteGenreSuccess() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/tagsController");

        page.onceDialog(dialog -> dialog.accept());
        page.locator("tr:has-text('Isekai Chuyển Sinh')").locator("a.btn-outline-danger").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        assertFalse(page.locator("text=Isekai Chuyển Sinh").isVisible());
    }

    @Test
    @Order(5)
    @DisplayName("TC_GENRE_05: Kiểm thử lỗi nhập tên quá ngắn")
    void testAddGenreFailShortName() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/tagsController/add");

        page.locator("#name").fill("A");
        page.locator("#slug").fill("a");
        page.locator("button:has-text('Lưu lại')").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        assertTrue(page.locator(".text-danger").first().isVisible());
        assertTrue(page.url().contains("/tagsController/add"));
    }
}