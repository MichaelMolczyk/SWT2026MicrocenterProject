package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class pcCompare {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";

    // Dropdown link texts (what appears in the PC Parts dropdown menu)
    private static final String[] DROPDOWN_OPTIONS = {
        "AMD Bundles",              // PCDC_01
        "Intel Bundles",            // PCDC_02
        "ASUS Build Bundles",       // PCDC_03
        "Corsair Build Bundles",    // PCDC_04
        "Processors/CPUs",          // PCDC_05
        "Motherboards",             // PCDC_06
        "Graphics Cards",           // PCDC_07
        "Computer Memory",          // PCDC_08
        "Drives & Storage",         // PCDC_09
        "Computer Cases",           // PCDC_10
        "Power Supplies",           // PCDC_11
        "Air & Water Cooling"       // PCDC_12
    };

    // Corresponding page link texts (what appears on the PC Parts category page)
    private static final String[] PAGE_NAMES = {
        "AMD Bundles",                    // PCDC_01
        "Intel Bundles",                  // PCDC_02
        "ASUS Build Bundles",             // PCDC_03
        "Corsair Build Bundles",          // PCDC_04
        "All Processors",                 // PCDC_05
        "All Motherboards",               // PCDC_06
        "All Graphics Cards",             // PCDC_07
        "All Computer Memory / RAM",       // PCDC_08
        "All Hard Drives & SSDs",          // PCDC_09
        "All Computer Cases",             // PCDC_10
        "All Power Supplies",             // PCDC_11
        "All Computer Fans & Cooling"     // PCDC_12
    };

    @BeforeMethod
    public void setUp() {
        EdgeOptions options = new EdgeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");

        driver = new EdgeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        TestUtils.navigateTo(driver, BASE_URL);
    }

    @AfterMethod
    public void tearDown() {
        driver = null;
    }

    // =========================================================================
    // Helper: Get the href from the PC Parts dropdown for a given link text
    //         Uses TestUtils.findDropdownLink to get the dropdown link after hovering
    // =========================================================================
    private String getDropdownHref(String linkText) {
        // Find "Parts" tab and get the dropdown link within it
        WebElement dropdownLink = TestUtils.findDropdownLink(driver, wait, "Parts", linkText);
        String href = dropdownLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Dropdown link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Get the href from the PC Parts category page for a given link text
    // =========================================================================
    private String getPageHref(String linkText) {
        // Navigate to the PC Parts category page
        TestUtils.navigateTo(driver, "https://www.microcenter.com/site/products/parts.aspx");

        // Find the link in the sidebar or main content
        WebElement pageLink = TestUtils.findFirst(driver, wait,
                By.xpath("//a[contains(normalize-space(),'" + linkText + "')]"));
        String href = pageLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Page link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Compare dropdown URL to page URL for a given index
    // =========================================================================
    private void compareLinks(int index, String testId) {
        String dropdownHref = getDropdownHref(DROPDOWN_OPTIONS[index]);

        // Navigate back to the home page to then go to the PC Parts page
        TestUtils.navigateTo(driver, BASE_URL);
        TestUtils.sleep(500);

        String pageHref = getPageHref(PAGE_NAMES[index]);

        Assert.assertEquals(pageHref, dropdownHref,
                testId + ": URLs do not match.\n" +
                "Dropdown '" + DROPDOWN_OPTIONS[index] + "': " + dropdownHref + "\n" +
                "Page '" + PAGE_NAMES[index] + "': " + pageHref);
    }

    // =========================================================================
    // PCDC_01: AMD Bundles
    // =========================================================================
    @Test(description = "PCDC_01: Compare dropdown 'AMD Bundles' link to page link")
    public void testPCDC01_AMDBundles() {
        compareLinks(0, "PCDC_01");
    }

    // =========================================================================
    // PCDC_02: Intel Bundles
    // =========================================================================
    @Test(description = "PCDC_02: Compare dropdown 'Intel Bundles' link to page link")
    public void testPCDC02_IntelBundles() {
        compareLinks(1, "PCDC_02");
    }

    // =========================================================================
    // PCDC_03: ASUS Build Bundles
    // =========================================================================
    @Test(description = "PCDC_03: Compare dropdown 'ASUS Build Bundles' link to page link")
    public void testPCDC03_ASUSBuildBundles() {
        compareLinks(2, "PCDC_03");
    }

    // =========================================================================
    // PCDC_04: Corsair Build Bundles
    // =========================================================================
    @Test(description = "PCDC_04: Compare dropdown 'Corsair Build Bundles' link to page link")
    public void testPCDC04_CorsairBuildBundles() {
        compareLinks(3, "PCDC_04");
    }

    // =========================================================================
    // PCDC_05: Processors/CPUs → All Processors
    // =========================================================================
    @Test(description = "PCDC_05: Compare dropdown 'Processors/CPUs' link to page 'All Processors' link")
    public void testPCDC05_Processors() {
        compareLinks(4, "PCDC_05");
    }

    // =========================================================================
    // PCDC_06: Motherboards → All Motherboards
    // =========================================================================
    @Test(description = "PCDC_06: Compare dropdown 'Motherboards' link to page 'All Motherboards' link")
    public void testPCDC06_Motherboards() {
        compareLinks(5, "PCDC_06");
    }

    // =========================================================================
    // PCDC_07: Graphics Cards → All Graphics Cards
    // =========================================================================
    @Test(description = "PCDC_07: Compare dropdown 'Graphics Cards' link to page 'All Graphics Cards' link")
    public void testPCDC07_GraphicsCards() {
        compareLinks(6, "PCDC_07");
    }

    // =========================================================================
    // PCDC_08: Computer Memory → All Computer Memory/RAM
    // =========================================================================
    @Test(description = "PCDC_08: Compare dropdown 'Computer Memory' link to page 'All Computer Memory/RAM' link")
    public void testPCDC08_ComputerMemory() {
        compareLinks(7, "PCDC_08");
    }

    // =========================================================================
    // PCDC_09: Drives & Storage → All Hard Drives & SSD's
    // =========================================================================
    @Test(description = "PCDC_09: Compare dropdown 'Drives & Storage' link to page 'All Hard Drives & SSD's' link")
    public void testPCDC09_DrivesStorage() {
        compareLinks(8, "PCDC_09");
    }

    // =========================================================================
    // PCDC_10: Computer Cases → All Computer Cases
    // =========================================================================
    @Test(description = "PCDC_10: Compare dropdown 'Computer Cases' link to page 'All Computer Cases' link")
    public void testPCDC10_ComputerCases() {
        compareLinks(9, "PCDC_10");
    }

    // =========================================================================
    // PCDC_11: Power Supplies → All Power Supplies
    // =========================================================================
    @Test(description = "PCDC_11: Compare dropdown 'Power Supplies' link to page 'All Power Supplies' link")
    public void testPCDC11_PowerSupplies() {
        compareLinks(10, "PCDC_11");
    }

    // =========================================================================
    // PCDC_12: Air & Water Cooling → All Computer Fans & Cooling
    // =========================================================================
    @Test(description = "PCDC_12: Compare dropdown 'Air & Water Cooling' link to page 'All Computer Fans & Cooling' link")
    public void testPCDC12_AirWaterCooling() {
        compareLinks(11, "PCDC_12");
    }
}
