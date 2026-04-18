package org.example;

import me.bramar.undetectedselenium.SeleniumStealthOptions;
import me.bramar.undetectedselenium.UndetectedChromeDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;

import java.time.Duration;

/**
 * Quick validation test — run this FIRST to verify stealth is working.
 * Hits bot.sannysoft.com which shows what Cloudflare sees.
 * If "webdriver" shows "missing (passed)" — stealth is working.
 * If "webdriver" shows "present (failed)" — stealth is NOT working.
 */
public class StealthValidation {

    private WebDriver driver;

    @Test(description = "Validate stealth at bot.sannysoft.com")
    public void validateStealth() throws Exception {
        driver = UndetectedChromeDriver.builder()
                .seleniumStealth(SeleniumStealthOptions.getDefault())
                .build();

        System.out.println("=== STEALTH VALIDATION ===");
        System.out.println("Navigating to bot.sannysoft.com...");

        driver.get("https://bot.sannysoft.com");
        Thread.sleep(5000); // Let all JS checks run

        // Print the page title
        System.out.println("Page title: " + driver.getTitle());

        // Check key detection results
        String[] checks = {"webdriver", "chrome", "permissions", "plugins-length", "languages"};
        for (String check : checks) {
            try {
                WebElement row = driver.findElement(By.id(check));
                String result = row.getText();
                System.out.println("  " + check + ": " + result);
            } catch (NoSuchElementException e) {
                System.out.println("  " + check + ": (element not found)");
            }
        }

        // Take a screenshot for manual review
        System.out.println("\nReview the Chrome window to see all results.");
        System.out.println("GREEN rows = passed (not detected as bot)");
        System.out.println("RED rows = failed (detected as bot)");
        System.out.println("\nPress Enter in the console to close, or wait 30s...");

        Thread.sleep(30000); // Keep browser open for 30s so you can see results
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
