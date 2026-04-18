package org.example;

import me.bramar.undetectedselenium.SeleniumStealthOptions;
import me.bramar.undetectedselenium.UndetectedChromeDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class appleCompare {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";
    private static final String APPLE_PAGE = "https://www.microcenter.com/site/products/apple.aspx";

    // Ribbon link texts (what appears on the Apple page ribbon bar)
    // Used to find the page-side href (source of truth)
    private static final String[] RIBBON_NAMES = {
        "MacBook",           // ADC_01
        "Mac Desktops",      // ADC_02
        "iPad",              // ADC_03
        "Apple Watch",       // ADC_04
        "AirPod",            // ADC_05 — ribbon says "AirPod" (no s)
        "Accessories"        // ADC_06
    };

    @BeforeMethod
    public void setUp() throws Exception {
        driver = UndetectedChromeDriver.builder()
                .seleniumStealth(SeleniumStealthOptions.getDefault())
                .build();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        TestUtils.navigateTo(driver, BASE_URL);
        waitForNav();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    /**
     * Wait for the nav to actually appear — handles Cloudflare delay.
     * If the nav isn't found, retries navigation once.
     */
    private void waitForNav() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("navContainer")));
        } catch (TimeoutException e) {
            TestUtils.sleep(5000);
            TestUtils.navigateTo(driver, BASE_URL);
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("navContainer")));
        }
    }

    // =========================================================================
    // Helper: Get the href from the Apple page ribbon for a given link text
    //         Navigates to apple.aspx and finds link inside the ribbon bar
    // =========================================================================
    private String getRibbonHref(String linkText) {
        TestUtils.navigateTo(driver, APPLE_PAGE);

        // Find the link in the dark ribbon bar (div#myTopnav inside div#AppleNav)
        WebElement ribbonLink = TestUtils.findFirst(driver, wait,
                By.xpath("//div[@id='myTopnav']//a[contains(normalize-space(),'" + linkText + "')]"),
                By.xpath("//div[@id='AppleNav']//a[contains(normalize-space(),'" + linkText + "')]"),
                By.xpath("//a[contains(normalize-space(),'" + linkText + "')]"));
        String href = ribbonLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Ribbon link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Hover over the Apple tab and verify a dropdown link exists
    //         that matches the given href. Compares by href, not by text.
    // =========================================================================
    private void verifyDropdownHasHref(String expectedHref, String testId, String ribbonName) {
        TestUtils.navigateTo(driver, BASE_URL);
        waitForNav();

        // Hover over the "Apple" tab to reveal the dropdown
        WebElement appleTab = TestUtils.findNavTab(driver, wait, "Apple");
        TestUtils.hover(driver, appleTab);

        // Grab all dropdown links
        List<WebElement> dropdownLinks = driver.findElements(
                By.cssSelector("li.generalLink a.upperDropNav"));

        // Also try broader selector if the first found nothing
        if (dropdownLinks.isEmpty()) {
            dropdownLinks = driver.findElements(
                    By.xpath("//nav[@id='navContainer']//ul//ul//a"));
        }

        // Search for a link whose href matches
        String matchedText = null;
        for (WebElement link : dropdownLinks) {
            String href = link.getAttribute("href");
            if (href != null) {
                String normalized = href.toLowerCase().replaceAll("/$", "");
                if (normalized.equals(expectedHref)) {
                    matchedText = link.getText().trim();
                    break;
                }
            }
        }

        Assert.assertNotNull(matchedText,
                testId + ": No dropdown link found matching ribbon href.\n" +
                "Expected href: " + expectedHref + "\n" +
                "Ribbon link: '" + ribbonName + "'\n" +
                "Dropdown links found: " + dropdownLinks.size());
    }

    // =========================================================================
    // Helper: Get ribbon href, then verify the dropdown contains matching link
    // =========================================================================
    private void compareLinks(int index, String testId) {
        // Step 1: Get the ribbon link's href (this is the source of truth)
        String ribbonHref = getRibbonHref(RIBBON_NAMES[index]);

        // Step 2: Go back and verify the dropdown has a link with the same href
        verifyDropdownHasHref(ribbonHref, testId, RIBBON_NAMES[index]);
    }

    // =========================================================================
    // ADC_01 through ADC_06
    // =========================================================================
    @Test(description = "ADC_01: Compare dropdown 'MacBook' link to ribbon 'MacBook' link")
    public void testADC01_MacBook() { compareLinks(0, "ADC_01"); }

    @Test(description = "ADC_02: Compare dropdown 'Mac Desktops' link to ribbon 'Mac Desktops' link")
    public void testADC02_MacDesktops() { compareLinks(1, "ADC_02"); }

    @Test(description = "ADC_03: Compare dropdown 'iPad' link to ribbon 'iPad' link")
    public void testADC03_iPad() { compareLinks(2, "ADC_03"); }

    @Test(description = "ADC_04: Compare dropdown 'Apple Watch' link to ribbon 'Apple Watch' link")
    public void testADC04_AppleWatch() { compareLinks(3, "ADC_04"); }

    @Test(description = "ADC_05: Compare dropdown 'AirPods' link to ribbon 'AirPod' link")
    public void testADC05_AirPods() { compareLinks(4, "ADC_05"); }

    @Test(description = "ADC_06: Compare dropdown 'Accessories' link to ribbon 'Accessories' link")
    public void testADC06_Accessories() { compareLinks(5, "ADC_06"); }
}
