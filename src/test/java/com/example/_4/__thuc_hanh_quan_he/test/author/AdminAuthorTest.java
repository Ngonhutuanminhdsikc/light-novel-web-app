package com.example._4.__thuc_hanh_quan_he.test.author;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminAuthorTest extends BaseAuthorTest {

    @BeforeAll
    static void initScreenshotDir() throws Exception {
        Files.createDirectories(Paths.get("target/screenshots"));
    }

    @BeforeEach
    void setupPage() { page = browser.newPage(); }

    @AfterEach
    void tearDown(TestInfo testInfo) {

        String displayName = testInfo.getDisplayName();

        String fileName = (displayName == null || displayName.isEmpty())
                ? testInfo.getTestMethod().get().getName()
                : displayName;

        String screenshotPath = "target/screenshots/" + fileName.replaceAll("[^a-zA-Z0-9.-]", "_") + ".png";

        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(screenshotPath)));
        page.close();
    }

    @Test
    @Order(1)
    void testAddNewAuthorSuccess() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/authors/add");

        page.locator("#name").fill("Chidrag");
        page.locator("#description").fill("Mô tả hợp lệ.");

        page.locator("button:has-text('Lưu tác giả')").click();
        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator newAuthor = page.locator("table").locator("text=Chidrag");
        assertTrue(newAuthor.isVisible(), "Lỗi! Tác giả không hiển thị trên bảng sau khi lưu!");
    }

    @Test
    @Order(2)
    void testEditAuthorSuccess() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/authors");

        String authorToEdit = "Chidrag";
        String newName = "Chidrag Updated";

        Locator targetRow = page.locator("table tbody tr").filter(new Locator.FilterOptions().setHasText(authorToEdit));

        targetRow.locator("a.btn-outline-warning").click();

        page.waitForURL("**/authors/edit/**");

        page.locator("#name").fill(newName);
        page.locator("button:has-text('Lưu thay đổi')").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator updatedAuthor = page.locator("table tbody tr").filter(new Locator.FilterOptions().setHasText(newName));
        assertTrue(updatedAuthor.isVisible(), "Lỗi! Không tìm thấy tác giả với tên đã cập nhật!");
    }

    @Test
    @Order(3)
    void testDeleteAuthorSuccess() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/authors");

        String authorToDelete = "Chidrag Updated";

        Locator targetRow = page.locator("table tbody tr").filter(new Locator.FilterOptions().setHasText(authorToDelete));

        page.onceDialog(dialog -> {
            assertEquals("confirm", dialog.type());
            dialog.accept();
        });

        targetRow.locator("a.btn-outline-danger").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        assertFalse(targetRow.isVisible(), "Lỗi! Tác giả vẫn còn tồn tại trong bảng sau khi xóa!");
    }

    @Test
    @Order(4)
    @DisplayName("testAddNewAuthorFailEmptyName")
    void testAddNewAuthorFailEmptyName() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/authors/add");

        page.locator("#name").fill("");
        page.locator("button:has-text('Lưu tác giả')").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator nameError = page.locator(".text-danger:has-text('...')");
        assertTrue(page.locator(".text-danger").first().isVisible(), "Lỗi validate tên không xuất hiện!");

        assertTrue(page.url().contains("/authors/add"));
    }

    @Test
    @Order(5)
    @DisplayName("testAddNewAuthorFailShortName")
    void testAddNewAuthorFailShortName() {
        loginAsAdmin();
        page.navigate("http://localhost:8080/authors/add");

        page.locator("#name").fill("A");
        page.locator("#description").fill("Mô tả hợp lệ cho tác giả.");

        page.locator("button:has-text('Lưu tác giả')").click();

        page.waitForLoadState(LoadState.NETWORKIDLE);

        Locator nameError = page.locator("#name + .text-danger");

        assertTrue(nameError.isVisible(), "Lỗi! Hệ thống không hiển thị thông báo lỗi ngay dưới ô Tên!");

        assertTrue(page.url().contains("/authors/add"), "Hệ thống đã chuyển hướng sai khi có lỗi validate!");

    }
}