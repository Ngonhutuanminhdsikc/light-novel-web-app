package com.example._4.__thuc_hanh_quan_he.user;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserReadingTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private final String BASE_URL = "http://localhost:8080";


    private void capture(String testName) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String path = "target/screenshots/" + testName + "_" + timestamp + ".png";
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(path)));
        System.out.println("LOG: Đã chụp ảnh màn hình tại -> " + path);
    }

    @BeforeAll
    void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(150));

        context = browser.newContext();
        page = context.newPage();
    }

    @AfterAll
    void closeBrowser() {
        if (context != null) context.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }


    private void ensureLoggedIn(Page page) {
        if (page.url() == null || page.url().equals("about:blank")) {
            page.navigate(BASE_URL + "/");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        }

        boolean isNeedLogin = page.locator("a[href*='login'], a:has-text('Đăng nhập')").count() > 0;

        if (isNeedLogin || page.url().contains("/login")) {
            System.out.println("LOG INFO: Phát hiện chưa có session đăng nhập, khởi động luồng login tự động...");

            page.navigate(BASE_URL + "/login");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            page.locator("input[name='username']").fill("minh204");
            page.locator("input[name='password']").fill("minh205");

            page.locator("button:has-text('ĐĂNG NHẬP'), button[type='submit']").first().click();

            page.waitForURL(Pattern.compile("http://localhost:8080/?"));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForTimeout(500); // Khoảng nghỉ nhỏ cho cookie ăn khớp
        } else {
            System.out.println("LOG INFO: Đã nhận diện được Session đăng nhập hợp lệ.");
        }
    }

    @Test
    @Order(1)
    @DisplayName("TC_USER_01: Vào trang chủ -> Đi tới Đăng nhập -> Xác thực Đăng nhập thành công")
    void testHomepageAndLoginFlow() {
        page.navigate(BASE_URL + "/");
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        Locator loginLink = page.locator("a[href*='login'], a:has-text('Đăng nhập'), .bi-person-fill").first();
        try {
            if (loginLink.isVisible()) {
                loginLink.click();
            } else {
                page.navigate(BASE_URL + "/login");
            }
        } catch (Exception e) {
            page.navigate(BASE_URL + "/login");
        }
        page.waitForURL("**/login");

        page.locator("input[name='username']").fill("minh204");
        page.locator("input[name='password']").fill("minh205");
        page.locator("button:has-text('ĐĂNG NHẬP'), button[type='submit']").first().click();

        page.waitForURL(Pattern.compile("http://localhost:8080/?"));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        boolean isLoggedIn = page.locator("body").innerText().contains("Đăng truyện");
        assertTrue(isLoggedIn, "Lỗi! Đăng nhập không thành công!");

        capture("TC_01_LoginSuccess");
    }

    @Test
    @Order(2)
    @DisplayName("TC_USER_02: Từ trang chủ click vào 1 truyện bất kỳ -> Kiểm tra Theo dõi & Bình luận")
    void testUserFollowAndComment() {

        ensureLoggedIn(page);

        if (!page.url().equals(BASE_URL + "/") && !page.url().equals(BASE_URL)) {
            page.navigate(BASE_URL + "/");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        }

        Locator targetStoryCard = page.locator(".hako-grid .hako-card, .card, a[href*='/story/']").first();
        targetStoryCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
        targetStoryCard.scrollIntoViewIfNeeded();
        targetStoryCard.click();

        try {
            page.waitForURL("**/story/**", new Page.WaitForURLOptions().setTimeout(5000));
        } catch (Exception e) {
            System.out.println("LOG WARNING: Bị chặn quyền hoặc đá văng về trang login, tiến hành ép login lại...");
            if (page.url().contains("/login")) {
                page.locator("input[name='username']").fill("minh204");
                page.locator("input[name='password']").fill("minh205");
                page.locator("button:has-text('ĐĂNG NHẬP'), button[type='submit']").first().click();
                page.waitForURL(Pattern.compile("http://localhost:8080/?"));

                targetStoryCard = page.locator(".hako-grid .hako-card, .card, a[href*='/story/']").first();
                targetStoryCard.click();
                page.waitForURL("**/story/**");
            }
        }
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        Locator followBtn = page.locator("#btn-follow");
        if (followBtn.isVisible()) {
            String initialText = followBtn.locator("#follow-text").innerText().trim().toUpperCase();
            followBtn.click();

            page.waitForCondition(() -> {
                String currentText = followBtn.locator("#follow-text").innerText().trim().toUpperCase();
                return !currentText.equals(initialText) || currentText.contains("THEO DÕI");
            });
        }

        Locator commentInput = page.locator("#mainCommentContent");
        if (commentInput.isVisible()) {
            commentInput.scrollIntoViewIfNeeded();
            String testComment = "Bình luận tự động sinh từ robot1 Playwright: " + System.currentTimeMillis();
            commentInput.fill(testComment);

            Locator submitCommentBtn = page.locator("button[onclick*='sendComment'], button:has-text('GỬI')").first();

            try {
                page.waitForResponse(response -> response.url().contains("/comments/add") || response.status() == 200, () -> {
                    submitCommentBtn.click();
                });
            } catch (Exception ex) {
                System.out.println("LOG WARNING: Kích hoạt click dự phòng cho nút Gửi...");
                submitCommentBtn.click();
            }

            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            page.waitForTimeout(1000);

            boolean isCommentPosted = page.locator("#commentSection").innerText().contains(testComment);
            assertTrue(isCommentPosted, "Lỗi! Không tìm thấy bình luận kiểm thử trong vùng #commentSection!");
            System.out.println("LOG SUCCESS: Đã kiểm tra luồng bình luận thành công.");
        }

        capture("TC_02_CommentPosted");
    }

    @Test
    @Order(3)
    @DisplayName("TC_USER_03: Vào trang đọc chương -> Kiểm tra Toolbar & Tùy chỉnh cỡ chữ Settings")
    void testReadingInterfaceAndToolbar() {
        ensureLoggedIn(page);

        if (!page.url().contains("/story/")) {
            page.navigate(BASE_URL + "/");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            Locator targetStoryCard = page.locator(".hako-grid .hako-card, .card, a[href*='/story/']").first();
            targetStoryCard.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            targetStoryCard.click();

            try {
                page.waitForURL("**/story/**", new Page.WaitForURLOptions().setTimeout(5000));
            } catch (Exception e) {
                if (page.url().contains("/login")) {
                    page.locator("input[name='username']").fill("minh204");
                    page.locator("input[name='password']").fill("minh205");
                    page.locator("button:has-text('ĐĂNG NHẬP'), button[type='submit']").first().click();
                    page.waitForURL(Pattern.compile("http://localhost:8080/?"));
                    page.navigate(BASE_URL + "/");
                    page.locator(".hako-grid .hako-card, .card, a[href*='/story/']").first().click();
                    page.waitForURL("**/story/**");
                }
            }
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        }

        Locator firstChapterLink = page.locator(".chapter-list-item, a[href*='/reading/chapter/']").first();

        if (firstChapterLink.isVisible()) {
            firstChapterLink.click();
            page.waitForURL("**/reading/chapter/**");
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);

            boolean isContentRendered = page.locator(".chapter-content").isVisible();
            assertTrue(isContentRendered, "Lỗi! Không load được nội dung chương truyện!");

            Locator gearButton = page.locator("button:has(.bi-gear-fill), button[onclick*='toggleSettings']").first();
            gearButton.click();

            page.locator("#settingsPanel").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));

            Locator btnA = page.locator("button:has-text('A+')").first();
            btnA.click();

            page.locator("#fontSizeDisplay").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            String fontSizeText = page.locator("#fontSizeDisplay").innerText().trim();
            assertEquals("24px", fontSizeText, "Lỗi! Tính năng tăng kích thước chữ không lên 24px!");

            page.locator("#settingsPanel .btn-close, button[onclick*='toggleSettings']").first().click();

            capture("TC_03_SettingsChanged");

            Locator nextChapterBtn = page.locator(".bottom-toolbar a:has-text('Next'), .bottom-toolbar a:has(.bi-arrow-right-square-fill)");
            if (nextChapterBtn.isVisible() && !nextChapterBtn.getAttribute("class").contains("disabled")) {
                nextChapterBtn.click();
                page.waitForURL("**/reading/chapter/**");
                System.out.println("LOG SUCCESS: Đã chuyển sang chương tiếp theo thành công.");
            }
        } else {
            System.out.println("LOG WARNING: Truyện ko có chương để test.");
        }

    }


    @Test
    @Order(4)
    @DisplayName("TC_LOGIN_02: Kiểm thử lỗi khi nhập sai định dạng Tên đăng nhập")
    void testLoginValidationFail() {
        page.navigate(BASE_URL + "/login");

        page.locator("input[name='username']").fill("abc");
        page.locator("input[name='password']").fill("password@123");

        page.locator("button:has-text('ĐĂNG NHẬP')").click();

        String errorSelector = "text=Tên đăng nhập từ 5 ký tự, chỉ gồm chữ và số!";

        assertTrue(page.locator(errorSelector).isVisible(), "Lỗi: Hệ thống không hiển thị thông báo validation cho Tên đăng nhập!");

        capture("TC_LOGIN_Validation_Fail");
    }

    @Test
    @Order(5)
    @DisplayName("TC_LOGIN_03: Kiểm thử lỗi khi nhập mật khẩu sai định dạng")
    void testLoginPasswordValidationFail() {
        page.navigate(BASE_URL + "/login");

        page.locator("input[name='username']").fill("minh204");
        page.locator("input[name='password']").fill("123");

        page.locator("button:has-text('ĐĂNG NHẬP')").click();

        String errorSelector = "text=Mật khẩu từ 6 ký tự, chỉ gồm chữ, số và @!";

        assertTrue(page.locator(errorSelector).isVisible(), "Lỗi: Hệ thống không hiển thị thông báo validation cho Mật khẩu!");

        capture("TC_LOGIN_Password_Validation_Fail");
    }

    @Test
    @Order(6)
    @DisplayName("TC_POST_03: Chặn đăng truyện khi tiêu đề trống (Validation)")
    void testPostStoryEmptyTitle() {
        ensureLoggedIn(page);
        page.navigate(BASE_URL + "/user/stories/create-new-story");

        page.locator("input[name='title']").fill("");
        page.locator("textarea[name='description']").fill("Mô tả nội dung truyện hợp lệ, đủ độ dài.");

        page.locator("button:has-text('Lưu truyện ngay')").click();

        assertTrue(page.locator(".invalid-feedback").isVisible(), "Lỗi! Hệ thống không chặn được việc để trống Tiêu đề!");
        capture("TC_POST_Title_Empty");
    }

    @Test
    @Order(7)
    @DisplayName("TC_POST_04: Chặn lưu truyện khi không chọn Thể loại (Tag)")
    void testPostStoryNoTags() {
        ensureLoggedIn(page);
        page.navigate(BASE_URL + "/user/stories/create-new-story");

        page.locator("input[name='title']").fill("Truyện không tag " + System.currentTimeMillis());
        page.locator("textarea[name='description']").fill("Mô tả nội dung đầy đủ để không bị chặn bởi mô tả...");

        page.locator("button:has-text('Lưu truyện ngay')").click();

        assertTrue(page.locator(".invalid-feedback.text-danger:visible").count() > 0
                        || page.locator("text=Vui lòng chọn ít nhất một thể loại").isVisible(),
                "Lỗi! Hệ thống không hiển thị thông báo lỗi khi không chọn tag!");
        capture("TC_POST_No_Tags");
    }

    @Test
    @Order(8)
    @DisplayName("TC_EDIT_01: Kiểm tra chặn lưu khi sửa tiêu đề thành trống")
    void testEditStoryValidation() {
        ensureLoggedIn(page);
        page.navigate(BASE_URL + "/user/stories/my-stories");

        page.locator("a[href*='/user/stories/edit/']").first().click();
        page.waitForURL("**/user/stories/edit/**");

        page.locator("input[name='title']").fill("");

        page.locator("button:has-text('Lưu thay đổi')").click();

        page.waitForTimeout(1000);

        String currentUrl = page.url();
        System.out.println("LOG DEBUG: URL sau khi nhấn lưu: " + currentUrl);

        boolean hasError = page.locator(".text-danger").count() > 0;

        if (!hasError) {
            capture("DEBUG_EDIT_ERROR");
            System.err.println("LOG DEBUG: Không tìm thấy class .text-danger nào trên trang!");
        }

        assertTrue(hasError, "Lỗi! Hệ thống không hiển thị thông báo lỗi khi để trống tiêu đề!");
    }


    @Test
    @Order(9)
    @DisplayName("TC_CHAPTER_02: Chặn lưu chương khi để trống tiêu đề")
    void testSaveChapterWithEmptyTitle() {
        ensureLoggedIn(page);
        page.navigate(BASE_URL + "/user/chapters/write/30");

        page.locator("input[name='chapterNumber']").fill("1");
        page.locator("input[name='volNumber']").fill("1");
        page.locator("input[name='title']").fill("");

        page.evaluate("CKEDITOR.instances['editor'].setData('Nội dung thử nghiệm')");
        page.evaluate("CKEDITOR.instances['editor'].updateElement()");

        page.locator("form:has(input[name='title']) button[type='submit']").click();

        Locator titleError = page.locator("input[name='title'] + .text-danger");
        titleError.waitFor(new Locator.WaitForOptions().setTimeout(5000));

        assertTrue(titleError.isVisible(), "Lỗi: Hệ thống không hiển thị thông báo lỗi khi để trống tiêu đề!");
        assertEquals("Tiêu đề chương không được để trống!", titleError.textContent().trim());    }
}