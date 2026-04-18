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

public class compCompare {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";
    private static final String COMPUTERS_PAGE = "https://www.microcenter.com/site/products/computers.aspx";

    // Page link texts (what appears on the Computers category page sidebar)
    private static final String[] PAGE_NAMES = {
        "All Desktop Computers",         // CDC_01
        "All Gaming Desktops",           // CDC_02
        "Refurbished Desktops",          // CDC_03
        "Desktop Workstations",          // CDC_04
        "All Laptops/Notebooks",         // CDC_05
        "Refurbished Laptops",           // CDC_06
        "Laptop Workstations",           // CDC_07
        "All Servers"                    // CDC_08
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
            // Cloudflare may still be checking — wait and retry
            TestUtils.sleep(5000);
            TestUtils.navigateTo(driver, BASE_URL);
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("navContainer")));
        }
    }

    // =========================================================================
    // Helper: Get the href from the Computers category page for a given link text
    // =========================================================================
    private String getPageHref(String linkText) {
        TestUtils.navigateTo(driver, COMPUTERS_PAGE);

        WebElement pageLink = TestUtils.findFirst(driver, wait,
                By.xpath("//a[contains(normalize-space(),'" + linkText + "')]"));
        String href = pageLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Page link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Hover over the Computers tab and verify a dropdown link exists
    //         that matches the given href. Compares by href, not by text.
    // =========================================================================
    private void verifyDropdownHasHref(String expectedHref, String testId, String pageName) {
        TestUtils.navigateTo(driver, BASE_URL);
        waitForNav();

        // Hover over the "Computers" tab to reveal the dropdown
        WebElement computersTab = TestUtils.findNavTab(driver, wait, "Computers");
        TestUtils.hover(driver, computersTab);

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
                testId + ": No dropdown link found matching page href.\n" +
                "Expected href: " + expectedHref + "\n" +
                "Page link: '" + pageName + "'\n" +
                "Dropdown links found: " + dropdownLinks.size());
    }

    // =========================================================================
    // Helper: Get page href, then verify the dropdown contains matching link
    // =========================================================================
    private void compareLinks(int index, String testId) {
        // Step 1: Get the page link's href (this is the source of truth)
        String pageHref = getPageHref(PAGE_NAMES[index]);

        // Step 2: Go back and verify the dropdown has a link with the same href
        verifyDropdownHasHref(pageHref, testId, PAGE_NAMES[index]);
    }

    // =========================================================================
    // CDC_01 through CDC_08
    // =========================================================================
    @Test(description = "CDC_01: Compare dropdown 'All Desktop Computers' link to page link")
    public void testCDC01_AllDesktopComputers() { compareLinks(0, "CDC_01"); }

    @Test(description = "CDC_02: Compare dropdown 'All Gaming Desktops' link to page link")
    public void testCDC02_AllGamingDesktops() { compareLinks(1, "CDC_02"); }

    @Test(description = "CDC_03: Compare dropdown 'Refurbished Desktops' link to page link")
    public void testCDC03_RefurbishedDesktops() { compareLinks(2, "CDC_03"); }

    @Test(description = "CDC_04: Compare dropdown 'Desktop Workstations' link to page link")
    public void testCDC04_DesktopWorkstations() { compareLinks(3, "CDC_04"); }

    @Test(description = "CDC_05: Compare dropdown 'All Laptops/Notebooks' link to page link")
    public void testCDC05_AllLaptopsNotebooks() { compareLinks(4, "CDC_05"); }

    @Test(description = "CDC_06: Compare dropdown 'Refurbished Laptops' link to page link")
    public void testCDC06_RefurbishedLaptops() { compareLinks(5, "CDC_06"); }

    @Test(description = "CDC_07: Compare dropdown 'Laptop Workstations' link to page link")
    public void testCDC07_LaptopWorkstations() { compareLinks(6, "CDC_07"); }

    @Test(description = "CDC_08: Compare dropdown 'All Servers' link to page link")
    public void testCDC08_AllServers() { compareLinks(7, "CDC_08"); }
}
