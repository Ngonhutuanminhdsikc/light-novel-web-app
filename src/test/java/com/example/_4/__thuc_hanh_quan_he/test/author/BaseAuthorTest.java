package com.example._4.__thuc_hanh_quan_he.test.author;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.*;

public class BaseAuthorTest {
    protected static Playwright playwright;
    protected static Browser browser;
    protected Page page;

    @BeforeAll
    static void launch() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(100));
    }

    @AfterAll
    static void close() {
        browser.close();
        playwright.close();
    }

    protected void loginAsAdmin() {
        page.navigate("http://localhost:8080/login");
        page.locator("input[name='username']").fill("admin");
        page.locator("input[name='password']").fill("1234566");

        page.locator("button:has-text('ĐĂNG NHẬP')").click();

        page.waitForURL(url -> !url.contains("/login"));

        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    protected void assertAlertVisible(String selector) {
        page.locator(selector).waitFor(new Locator.WaitForOptions().setTimeout(3000));
    }
}