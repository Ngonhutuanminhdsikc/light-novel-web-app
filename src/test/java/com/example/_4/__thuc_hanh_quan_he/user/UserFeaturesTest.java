package com.example._4.__thuc_hanh_quan_he.user;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserFeaturesTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    void launchBrowser() {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(300));
    }

    @AfterAll
    void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();


        page.navigate(BASE_URL + "/login");
        page.locator("input[name='username']").fill("minh204");
        page.locator("input[name='password']").fill("minh205");
        page.locator("button:has-text('ĐĂNG NHẬP')").click();
        page.waitForURL(BASE_URL + "/");
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    @Order(1)
    @DisplayName("TC_FEAT_01: Xác thực trạng thái Đăng nhập thành viên")
    void testLoginMember() {
        assertTrue(page.locator("body").innerText().contains("Đăng truyện"), "Lỗi! Đăng nhập thành viên thất bại!");
    }

    @Test
    @Order(2)
    @DisplayName("TC_FEAT_02: Độc giả thực hiện gửi yêu cầu Đăng truyện mới (Chờ duyệt)")
    void testUserPostStory() {
        page.navigate(BASE_URL + "/post-story");

        Locator titleInput = page.locator("input[name='title']");
        titleInput.click();


        String rawTitle = "LN Tự Sáng Tác " + System.currentTimeMillis();
        titleInput.pressSequentially(rawTitle, new Locator.PressSequentiallyOptions().setDelay(20));

        page.locator("input[name='authorName']").fill("Tác Giả Playwright");
        page.locator("textarea[name='description']").fill("Mo ta noi dung truyen tu dong sinh bang kich ban kiem thu.");
        page.locator("select[name='status']").selectOption("ONGOING");


        page.setInputFiles("input[type='file']", Paths.get("C:\\Users\\PC\\Downloads\\z7537698916431_d028a973a1bf871cd8cb3b7e9941a732.jpg"));
        page.waitForTimeout(500);

        Locator tagCheckbox = page.locator("input[name='tagIds']").first();
        if (tagCheckbox.isVisible()) {
            tagCheckbox.check();
        }

        page.waitForTimeout(1000); // Đợi sinh slug hoàn chỉnh

        Locator submitButton = page.locator("button:has-text('Lưu truyện ngay')").first();
        submitButton.scrollIntoViewIfNeeded();
        submitButton.click();

        page.waitForURL(url -> !url.contains("/post-story"));
        System.out.println("LOG DEBUG: URL thực tế sau khi bấm nút Lưu là: " + page.url());

        page.navigate(BASE_URL + "/user/stories/my-stories");
        page.waitForTimeout(1000);

        boolean isStoryListed = page.locator("body").innerText().contains(rawTitle);
        assertTrue(isStoryListed, "Lỗi! Không tìm thấy bộ truyện mới đăng hiển thị trong danh sách của User!");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/feat-02-post-story-pending.png")));
    }

    @Test
    @Order(3)
    @DisplayName("TC_FEAT_03: Kiểm tra giao diện Lịch sử đọc và tính năng Xóa lịch sử")
    void testReadHistoryAndEmptyState() {
        page.navigate(BASE_URL + "/history");
        page.waitForTimeout(1000); // Chờ trang ổn định hẳn

        assertTrue(page.locator("body").innerText().contains("Lịch sử"), "Lỗi! Giao diện trang Lịch sử đọc không tải được!");

        page.onDialog(Dialog::accept);

        Locator deleteButtons = page.locator("a:has-text('Xóa khỏi lịch sử'), button:has-text('Xóa khỏi lịch sử')");
        int countBeforeDelete = deleteButtons.count();
        System.out.println("LOG DEBUG: Số lượng mục trong lịch sử TRƯỚC khi xóa: " + countBeforeDelete);

        if (countBeforeDelete > 0) {
            Locator firstDeleteBtn = deleteButtons.first();
            firstDeleteBtn.scrollIntoViewIfNeeded();

            firstDeleteBtn.click();

            page.waitForTimeout(1000);
            page.navigate(BASE_URL + "/history");
            page.waitForTimeout(1000);

            int countAfterDelete = page.locator("a:has-text('Xóa khỏi lịch sử'), button:has-text('Xóa khỏi lịch sử')").count();
            System.out.println("LOG DEBUG: Số lượng mục trong lịch sử SAU khi xóa: " + countAfterDelete);

            assertEquals(countBeforeDelete - 1, countAfterDelete, "Lỗi! Hành động xóa lịch sử không làm giảm số lượng truyện trong danh sách!");
            System.out.println("LOG SUCCESS: Robot đã xóa thành công một mục khỏi lịch sử đọc.");
        } else {
            System.out.println("LOG INFO: Danh sách lịch sử hiện tại đang trống, không có mục để tiến hành xóa.");
        }

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/feat-03-history-page.png")));
    }


    @Test
    @Order(4)
    @DisplayName("TC_USER_04: Đăng chương mới cho truyện thành công")
    void testUserCreateNewChapter() {
        // 1. Vào trang quản lý truyện
        page.navigate(BASE_URL + "/user/stories/my-stories");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        Locator manageChapterBtn = page.locator("a[href*='/user/chapters/'], a:has(.bi-list-ul)").first();
        assertTrue(manageChapterBtn.isVisible(), "Lỗi! Không tìm thấy nút Quản lý chương.");
        manageChapterBtn.click();

        Locator addChapterBtn = page.locator("a:has-text('Thêm chương'), a[href*='/chapters/create']").first();
        if (addChapterBtn.isVisible()) {
            addChapterBtn.click();
        }

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        String chapterTitle = "Chương Test: " + System.currentTimeMillis();

        page.locator("input[name='chapterNumber']").fill("1");
        page.locator("input[name='volNumber']").fill("1");
        page.locator("input[name='title']").fill(chapterTitle);

        page.evaluate("CKEDITOR.instances['editor'].setData('Nội dung test tự động.')");

        page.locator("button.btn-success").click();

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        assertTrue(page.locator("body").innerText().contains(chapterTitle), "Lỗi! Không thấy chương mới xuất hiện.");

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/user-04-chapter-success.png")));
    }

    @Test
    @Order(5)
    @DisplayName("TC_FEAT_05: Sửa thông tin truyện thành công")
    void testEditStory() {
        page.navigate(BASE_URL + "/user/stories/my-stories");
        page.locator("a[href*='/user/stories/edit/']").first().click();

        String newTitle = "Tiêu đề đã update " + System.currentTimeMillis();
        page.locator("input[name='title']").fill(newTitle);

        page.locator("button.btn-success[type='submit']").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);
        assertTrue(page.locator("body").innerText().contains(newTitle), "Lỗi! Tiêu đề không cập nhật.");
    }

    @Test
    @Order(6)
    @DisplayName("TC_USER_06: Xóa chương truyện")
    void testDeleteChapter() {
        page.navigate(BASE_URL + "/user/stories/my-stories");

        page.locator("a[href*='/user/chapters/']").first().click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        page.onDialog(dialog -> {
            System.out.println("LOG INFO: Đã bắt được hộp thoại: " + dialog.message());
            dialog.accept();
        });

        Locator deleteBtn = page.locator("a.btn-delete[href*='/user/chapters/delete/']").first();

        if (deleteBtn.count() > 0) {
            deleteBtn.scrollIntoViewIfNeeded();
            deleteBtn.click();

            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("LOG SUCCESS: Đã click nút xóa thành công.");
        } else {
            System.err.println("LOG ERROR: Không tìm thấy nút xóa. Kiểm tra lại selector!");
        }
    }

    @Test
    @Order(7)
    @DisplayName("TC_FEAT_07: Xóa bộ truyện cá nhân")
    void testDeleteStory() {
        page.navigate(BASE_URL + "/user/stories/my-stories");

        page.onDialog(dialog -> {
            System.out.println("LOG INFO: Bắt được dialog xóa truyện: " + dialog.message());
            dialog.accept();
        });

        Locator deleteBtn = page.locator("a.btn-delete[href*='/user/stories/delete/']").first();

        if (deleteBtn.isVisible()) {
            deleteBtn.scrollIntoViewIfNeeded();
            deleteBtn.click();

            page.waitForLoadState(LoadState.NETWORKIDLE);
            System.out.println("LOG SUCCESS: Đã click nút xóa truyện thành công.");
        } else {
            System.err.println("LOG ERROR: Không tìm thấy nút xóa truyện!");
        }
    }


    // test lỗi
    @Test
    @Order(8)
    @DisplayName("TC_USER_08: Kiểm tra chặn nhập dữ liệu trống")
    void testCreateChapterValidation() {
        page.navigate(BASE_URL + "/login");
        page.fill("input[name='username']", "minh204");
        page.fill("input[name='password']", "minh205");
        page.click("button[type='submit']");
        page.waitForURL(BASE_URL + "/**");

        page.navigate(BASE_URL + "/user/chapters/add/54");

        page.fill("input[name='title']", "");
        page.evaluate("CKEDITOR.instances.editor.setData('')");
        page.waitForTimeout(500);

        page.locator("form[action*='/user/chapters/save'] button[type='submit']").click();

        String errorSelector = ".validation-error-msg";
        page.waitForSelector(errorSelector, new Page.WaitForSelectorOptions().setTimeout(5000));

        int errorCount = page.locator(errorSelector).count();
        assertTrue(errorCount > 0, "Thông báo lỗi validation phải hiển thị trên trang!");
    }

    @Test
    @Order(9)
    @DisplayName("TC_SEC_01: Kiểm tra chặn truy cập trái phép (ID không thuộc sở hữu)")
    void testUnauthorizedAccess() {
        var response = page.request().get(BASE_URL + "/user/chapters/9999");

        int statusCode = response.status();

        System.out.println("Debug Status Code: " + statusCode);
        System.out.println("Response Body: " + response.text());

        assertTrue(statusCode == 403 || statusCode == 404 || statusCode == 500, "Mã: " + statusCode);
    }


    @Test
    @Order(10)
    @DisplayName("TC_FEAT_10: Kiểm tra tính năng Theo dõi truyện")
    void testFollowStory() {
        page.navigate(BASE_URL + "/");
        Locator storyLink = page.locator("a[href*='/story/']").first();
        storyLink.click();
        page.waitForURL("**/story/**");

        String storyTitle = page.locator("h1").first().innerText().trim();
        System.out.println("LOG INFO: Đang theo dõi truyện: " + storyTitle);

        Locator followBtn = page.locator("#btn-follow");
        followBtn.waitFor();

        if (!followBtn.innerText().contains("Bỏ")) {

            page.waitForRequest(req -> req.url().contains("/follows/toggle/"), () -> {
                followBtn.click();
            });

            page.waitForTimeout(1000);
        }

        page.navigate(BASE_URL + "/follows/user/tu-truyen");

        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator targetStory = page.locator(".hako-grid .position-relative")
                .filter(new Locator.FilterOptions().setHasText(storyTitle));

        try {
            targetStory.first().waitFor(new Locator.WaitForOptions().setTimeout(10000));

            assertTrue(targetStory.count() > 0, "Lỗi! Truyện '" + storyTitle + "' không hiển thị trong danh sách!");
            System.out.println("LOG SUCCESS: Truyện đã xuất hiện thành công trong Tủ truyện.");

            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/feat-10-following-list.png")));

        } catch (Exception e) {
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("target/error-TC10-follow.png")));

            int foundCount = page.locator(".hako-grid .position-relative").count();
            System.err.println("LOG DEBUG: Tìm thấy tổng cộng " + foundCount + " truyện trong Tủ truyện.");

            throw new AssertionError("Lỗi: Không tìm thấy truyện '" + storyTitle + "' trong Tủ truyện sau khi theo dõi.", e);
        }
    }


}