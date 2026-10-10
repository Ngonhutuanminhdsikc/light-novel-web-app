package com.example._4.__thuc_hanh_quan_he.test.chapter;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminChapterTest {
    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    private final String TARGET_STORY_URL = "http://localhost:8080/chaptersController/story/26";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100));
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setupPage() { page = browser.newPage(); loginAsAdmin(); }

    @AfterEach
    void tearDown() { page.close(); }

    private void loginAsAdmin() {
        page.navigate("http://localhost:8080/login");
        page.locator("input[name='username']").fill("admin");
        page.locator("input[name='password']").fill("1234566");
        page.locator("button:has-text('ĐĂNG NHẬP')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    @Test
    @Order(1)
    void testAddNewChapterSuccess() {
        page.navigate(TARGET_STORY_URL);
        page.locator("a:has-text('Thêm Chương mới')").click();

        page.locator("#chapterNumber").fill("1");
        page.locator("#volNumber").fill("1");
        page.locator("#title").fill("Chương 1: Khởi Đầu Mới");
        page.locator("textarea[name='chapterContent.contentText']").fill("Nội dung chương test.");

        page.locator("button:has-text('Lưu chương')").click();
        page.waitForURL("**/chaptersController/story/26");

        assertTrue(page.locator("text=Chương 1: Khởi Đầu Mới").isVisible());
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/chapter-add.png")));
    }

    @Test
    @Order(2)
    void testEditChapterContentSuccess() {

        page.navigate(TARGET_STORY_URL);


        page.locator("a.btn-info").first().click();


        page.waitForFunction("() => typeof CKEDITOR !== 'undefined' && CKEDITOR.instances.contentText");
        page.evaluate("CKEDITOR.instances['contentText'].setData('<p>Nội dung mới từ Playwright11.</p>')");
        page.evaluate("CKEDITOR.instances['contentText'].updateElement()");

        page.locator("button:has-text('Lưu nội dung')").click();

        page.waitForURL("**/chaptersController/story/26");
        assertTrue(page.locator(".alert-success").isVisible(), "Không thấy thông báo thành công!");
    }

    @Test
    @Order(3)
    void testAddChapterValidationFail() {
        page.navigate(TARGET_STORY_URL);
        page.locator("a:has-text('Thêm Chương mới')").click();

        page.locator("#chapterNumber").fill("2");
        page.locator("#title").fill(""); // Đảm bảo rỗng

        page.locator("button:has-text('Lưu chương')").click();

        String errorSelector = "text=Tiêu đề chương không được để trống!";

        try {
            page.waitForSelector(errorSelector, new Page.WaitForSelectorOptions().setTimeout(5000));
            assertTrue(page.locator(errorSelector).isVisible(), "Lỗi: Thông báo validation không xuất hiện!");
        } catch (Exception e) {
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/error-debug-v2.png")));
            System.out.println("Nội dung trang hiện tại: " + page.content());
            fail("Không tìm thấy thông báo lỗi. Có thể selector 'text=...' chưa khớp với HTML.");
        }
    }

    @Test
    @Order(4)
    void testDeleteChapterSuccess() {
        page.navigate(TARGET_STORY_URL);
        page.onceDialog(dialog -> dialog.accept());

        page.locator("a.btn-outline-danger").last().click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/chapter-delete.png")));
    }

    @Test
    @Order(5)
    @DisplayName("TC_CHAPTER_05: Kiểm thử lỗi khi bỏ trống tiêu đề chương")
    void testAddChapterEmptyTitleFail() {
        page.navigate(TARGET_STORY_URL);
        page.locator("a:has-text('Thêm Chương mới')").click();

        page.locator("#chapterNumber").fill("2");
        page.locator("button:has-text('Lưu chương')").click();

        boolean isStillOnPage = page.locator("h1:has-text('Soạn thảo'), .card-title:has-text('Soạn thảo')").isVisible();

        boolean hasErrorMessage = page.locator("text=Tiêu đề chương không được để trống!").isVisible();

        assertTrue(isStillOnPage || hasErrorMessage, "Hệ thống phải ở lại trang Soạn thảo chương khi có lỗi!");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/chapter-error-empty-title.png")));
    }

    @Test
    @Order(6)
    @DisplayName("TC_CHAPTER_VALIDATION: Thử nhập số chương âm")
    void testAddChapterNegativeNumberFail() {
        page.navigate(TARGET_STORY_URL);
        page.locator("a:has-text('Thêm Chương mới')").click();

        page.locator("#chapterNumber").fill("-5");
        page.locator("#title").fill("Chương lỗi âm");
        page.locator("button:has-text('Lưu chương')").click();

        assertTrue(page.locator(".text-danger").first().isVisible(), "Phải hiển thị lỗi validation!");
        assertTrue(page.content().contains("Số chương không được nhỏ hơn 0"));
    }

    @Test
    @Order(7)
    @DisplayName("TC_CHAPTER_07: Kiểm thử lỗi khi soạn nội dung chương trống")
    void testEditChapterContentEmptyFail() {
        page.navigate("http://localhost:8080/edit/136"); // Thay bằng ID chương hiện có

        page.waitForFunction("() => typeof CKEDITOR !== 'undefined' && CKEDITOR.instances.contentText");

        page.evaluate("CKEDITOR.instances['contentText'].setData('')");

        page.onDialog(dialog -> {
            assertEquals("Nội dung chương không được để trống!", dialog.message());
            dialog.accept();
        });

        page.locator("button:has-text('Lưu nội dung')").click();

        assertTrue(page.locator(".alert-danger").isVisible(), "Phải hiện thông báo lỗi!");
    }
}