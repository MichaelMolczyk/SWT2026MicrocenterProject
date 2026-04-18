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

public class pcCompare {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";
    private static final String PARTS_PAGE = "https://www.microcenter.com/site/products/parts.aspx";

    // Page link texts (what appears on the PC Parts category page sidebar)
    private static final String[] PAGE_NAMES = {
        "AMD Bundles",                    // PCDC_01
        "Intel Bundles",                  // PCDC_02
        "ASUS Build Bundles",             // PCDC_03
        "Corsair Build Bundles",          // PCDC_04
        "All Processors",                 // PCDC_05
        "All Motherboards",               // PCDC_06
        "All Graphics Cards",             // PCDC_07
        "All Computer Memory / RAM",      // PCDC_08
        "All Hard Drives & SSDs",         // PCDC_09
        "All Computer Cases",             // PCDC_10
        "All Power Supplies",             // PCDC_11
        "All Computer Fans & Cooling"     // PCDC_12
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
    // Helper: Get the href from the PC Parts category page for a given link text
    // =========================================================================
    private String getPageHref(String linkText) {
        TestUtils.navigateTo(driver, PARTS_PAGE);

        WebElement pageLink = TestUtils.findFirst(driver, wait,
                By.xpath("//a[contains(normalize-space(),'" + linkText + "')]"));
        String href = pageLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Page link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Hover over the PC Parts tab and verify a dropdown link exists
    //         that matches the given href. Compares by href, not by text.
    // =========================================================================
    private void verifyDropdownHasHref(String expectedHref, String testId, String pageName) {
        TestUtils.navigateTo(driver, BASE_URL);
        waitForNav();

        // Hover over the "Parts" tab (matches "PC Parts") to reveal the dropdown
        WebElement partsTab = TestUtils.findNavTab(driver, wait, "Parts");
        TestUtils.hover(driver, partsTab);

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
    // PCDC_01 through PCDC_12
    // =========================================================================
    @Test(description = "PCDC_01: Compare dropdown 'AMD Bundles' link to page link")
    public void testPCDC01_AMDBundles() { compareLinks(0, "PCDC_01"); }

    @Test(description = "PCDC_02: Compare dropdown 'Intel Bundles' link to page link")
    public void testPCDC02_IntelBundles() { compareLinks(1, "PCDC_02"); }

    @Test(description = "PCDC_03: Compare dropdown 'ASUS Build Bundles' link to page link")
    public void testPCDC03_ASUSBuildBundles() { compareLinks(2, "PCDC_03"); }

    @Test(description = "PCDC_04: Compare dropdown 'Corsair Build Bundles' link to page link")
    public void testPCDC04_CorsairBuildBundles() { compareLinks(3, "PCDC_04"); }

    @Test(description = "PCDC_05: Compare dropdown 'Processors/CPUs' link to page 'All Processors' link")
    public void testPCDC05_Processors() { compareLinks(4, "PCDC_05"); }

    @Test(description = "PCDC_06: Compare dropdown 'Motherboards' link to page 'All Motherboards' link")
    public void testPCDC06_Motherboards() { compareLinks(5, "PCDC_06"); }

    @Test(description = "PCDC_07: Compare dropdown 'Graphics Cards' link to page 'All Graphics Cards' link")
    public void testPCDC07_GraphicsCards() { compareLinks(6, "PCDC_07"); }

    @Test(description = "PCDC_08: Compare dropdown 'Computer Memory' link to page 'All Computer Memory/RAM' link")
    public void testPCDC08_ComputerMemory() { compareLinks(7, "PCDC_08"); }

    @Test(description = "PCDC_09: Compare dropdown 'Drives & Storage' link to page 'All Hard Drives & SSD's' link")
    public void testPCDC09_DrivesStorage() { compareLinks(8, "PCDC_09"); }

    @Test(description = "PCDC_10: Compare dropdown 'Computer Cases' link to page 'All Computer Cases' link")
    public void testPCDC10_ComputerCases() { compareLinks(9, "PCDC_10"); }

    @Test(description = "PCDC_11: Compare dropdown 'Power Supplies' link to page 'All Power Supplies' link")
    public void testPCDC11_PowerSupplies() { compareLinks(10, "PCDC_11"); }

    @Test(description = "PCDC_12: Compare dropdown 'Air & Water Cooling' link to page 'All Computer Fans & Cooling' link")
    public void testPCDC12_AirWaterCooling() { compareLinks(11, "PCDC_12"); }
}
