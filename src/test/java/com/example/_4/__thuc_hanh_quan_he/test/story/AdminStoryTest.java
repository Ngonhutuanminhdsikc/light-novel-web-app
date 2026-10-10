package com.example._4.__thuc_hanh_quan_he.test.story;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.SelectOption;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminStoryTest {
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

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
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();

        loginAdmin();
    }

    private void loginAdmin() {
        page.navigate("http://localhost:8080/login");
        page.locator("input[name='username']").fill("admin");
        page.locator("input[name='password']").fill("1234566");
        page.locator("button:has-text('ĐĂNG NHẬP')").click();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    @Order(1)
    @DisplayName("TC_STORY_01: Thêm mới Truyện thành công")
    void testAddNewStorySuccess() {
        page.navigate("http://localhost:8080/admin/stories/add");
        String storyTitle = "Hành Trình Thử Nghiệm " + System.currentTimeMillis();

        page.locator("#title").pressSequentially(storyTitle);
        page.locator("textarea[placeholder*='Viết nội dung giới thiệu']").fill("Mô tả test tự động.");
        page.locator("select").first().selectOption(new SelectOption().setLabel("Lilith"));
        page.locator("select").nth(1).selectOption(new SelectOption().setLabel("ONGOING"));
        page.locator("label:has-text('isekai')").click();
        page.locator("#isHot").check();
        page.locator("input[type='file']").setInputFiles(Paths.get("C:\\Users\\PC\\Downloads\\z7537698916431_d028a973a1bf871cd8cb3b7e9941a732.jpg"));

        page.locator("button:has-text('Lưu truyện ngay')").click();
        page.waitForURL("**/admin/stories");

        assertTrue(page.locator("table.table").locator("text=" + storyTitle).isVisible());
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-add-story.png")));
    }

    @Test
    @Order(2)
    @DisplayName("TC_STORY_02: Chỉnh sửa thông tin Truyện thành công")
    void testEditStorySuccess() {
        page.navigate("http://localhost:8080/admin/stories");
        Locator targetRow = page.locator("table.table tbody tr").first(); // Lấy hàng đầu tiên

        targetRow.locator("a.btn-outline-warning").click();
        page.waitForURL("**/admin/stories/edit/**");

        page.locator("input[name='title']").fill("Tiêu đề đã cập nhật");
        page.locator("button:has-text('Lưu thay đổi')").click();

        page.waitForURL("**/admin/stories");
        assertTrue(page.locator("text=Tiêu đề đã cập nhật").isVisible());
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-edit-story.png")));
    }

    @Test
    @Order(3)
    @DisplayName("TC_STORY_03: Xóa truyện (Confirm Dialog)")
    void testDeleteStorySuccess() {
        page.navigate("http://localhost:8080/admin/stories");
        page.onceDialog(dialog -> dialog.accept());

        Locator deleteBtn = page.locator("a.btn-outline-danger").first();
        if (deleteBtn.isVisible()) {
            deleteBtn.click();
            page.waitForLoadState();
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-delete-story.png")));
        }
    }

    @Test
    @Order(4)
    @DisplayName("TC_STORY_04: Kiểm thử lỗi khi bỏ trống tiêu đề truyện")
    void testAddStoryValidationFail() {
        page.navigate("http://localhost:8080/admin/stories/add");

        page.locator("textarea[placeholder*='Viết nội dung giới thiệu']").fill("Mô tả hợp lệ cho bộ truyện.");

        page.locator("button:has-text('Lưu truyện ngay')").click();

        Locator errorAlert = page.locator(".alert-danger");
        assertTrue(errorAlert.isVisible(), "Lỗi! Hệ thống không hiển thị thông báo lỗi khi để trống tiêu đề!");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-error-empty-title.png")));
    }

    @Test
    @Order(5)
    @DisplayName("TC_STORY_05: Kiểm thử lỗi khi nhập tiêu đề quá ngắn")
    void testAddStoryShortTitleFail() {
        page.navigate("http://localhost:8080/admin/stories/add");

        page.locator("#title").fill("A");

        page.locator("button:has-text('Lưu truyện ngay')").click();

        Locator errorAlert = page.locator(".alert-danger");
        assertTrue(errorAlert.isVisible(), "Lỗi! Hệ thống không hiển thị thông báo lỗi!");
        assertTrue(errorAlert.innerText().contains("Tên bộ truyện phải từ 2 đến 200 ký tự"),
                "Lỗi! Nội dung thông báo lỗi không chính xác!");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-error-short-title.png")));
    }

    @Test
    @Order(6)
    @DisplayName("TC_STORY_06: Kiểm thử lỗi khi không chọn Tác giả")
    void testAddStoryNoAuthorFail() {
        page.navigate("http://localhost:8080/admin/stories/add");

        page.locator("#title").fill("Truyện mới của Admin");

        page.locator("button:has-text('Lưu truyện ngay')").click();

        assertTrue(page.locator(".alert-danger").isVisible());
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-error-no-author.png")));
    }

    @Test
    @Order(7)
    @DisplayName("TC_STORY_07: Kiểm thử lỗi khi không chọn bất kỳ Tag nào")
    void testAddStoryNoTagsFail() {
        page.navigate("http://localhost:8080/admin/stories/add");

        page.locator("#title").fill("Truyện không có Tag");
        page.locator("textarea[placeholder*='Viết nội dung giới thiệu']").fill("Nội dung giới thiệu.");
        page.locator("select").first().selectOption(new SelectOption().setLabel("Lilith"));


        page.locator("button:has-text('Lưu truyện ngay')").click();

        Locator errorAlert = page.locator(".alert-danger");
        assertTrue(errorAlert.isVisible(), "Lỗi! Hệ thống phải báo lỗi khi chưa chọn Tag!");
        assertTrue(errorAlert.innerText().contains("ít nhất một nhãn"),
                "Lỗi! Thông báo lỗi không nhắc đến việc chọn nhãn/tag!");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/evidence-error-no-tags.png")));
    }
}