package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;

public class appleCompare {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";

    // Dropdown link texts (what appears when hovering over the Apple tab)
    private static final String[] DROPDOWN_OPTIONS = {
        "MacBook",           // ADC_01
        "Mac Desktops",      // ADC_02
        "iPad",              // ADC_03
        "Apple Watch",       // ADC_04
        "AirPods",           // ADC_05
        "Accessories"        // ADC_06
    };

    // Corresponding ribbon link texts (what appears on the Apple page ribbon bar)
    private static final String[] RIBBON_NAMES = {
        "MacBook",           // ADC_01
        "Mac Desktops",      // ADC_02
        "iPad",              // ADC_03
        "Apple Watch",       // ADC_04
        "AirPod",            // ADC_05 — note: ribbon says "AirPod" (no s)
        "Accessories"        // ADC_06
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
    // Helper: Get the href from the Apple dropdown for a given link text
    //         Uses TestUtils.findDropdownLink to get the dropdown link after hovering
    // =========================================================================
    private String getDropdownHref(String linkText) {
        WebElement dropdownLink = TestUtils.findDropdownLink(driver, wait, "Apple", linkText);
        String href = dropdownLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Dropdown link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Get the href from the Apple page ribbon for a given link text
    //         Navigates to apple.aspx and finds link inside the ribbon bar (div#myTopnav)
    // =========================================================================
    private String getRibbonHref(String linkText) {
        // Navigate to the Apple page
        TestUtils.navigateTo(driver, "https://www.microcenter.com/site/products/apple.aspx");

        // Find the link in the dark ribbon bar at the top of the Apple page
        // The ribbon is inside div#myTopnav which is inside div#AppleNav
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
    // Helper: Compare dropdown URL to ribbon URL for a given index
    // =========================================================================
    private void compareLinks(int index, String testId) {
        String dropdownHref = getDropdownHref(DROPDOWN_OPTIONS[index]);

        // Navigate back to home to then go to the Apple page
        TestUtils.navigateTo(driver, BASE_URL);
        TestUtils.sleep(500);

        String ribbonHref = getRibbonHref(RIBBON_NAMES[index]);

        Assert.assertEquals(ribbonHref, dropdownHref,
                testId + ": URLs do not match.\n" +
                "Dropdown '" + DROPDOWN_OPTIONS[index] + "': " + dropdownHref + "\n" +
                "Ribbon '" + RIBBON_NAMES[index] + "': " + ribbonHref);
    }

    // =========================================================================
    // ADC_01: MacBook → MacBook
    // =========================================================================
    @Test(description = "ADC_01: Compare dropdown 'MacBook' link to ribbon 'MacBook' link")
    public void testADC01_MacBook() {
        compareLinks(0, "ADC_01");
    }

    // =========================================================================
    // ADC_02: Mac Desktops → Mac Desktops
    // =========================================================================
    @Test(description = "ADC_02: Compare dropdown 'Mac Desktops' link to ribbon 'Mac Desktops' link")
    public void testADC02_MacDesktops() {
        compareLinks(1, "ADC_02");
    }

    // =========================================================================
    // ADC_03: iPad → iPad
    // =========================================================================
    @Test(description = "ADC_03: Compare dropdown 'iPad' link to ribbon 'iPad' link")
    public void testADC03_iPad() {
        compareLinks(2, "ADC_03");
    }

    // =========================================================================
    // ADC_04: Apple Watch → Apple Watch
    // =========================================================================
    @Test(description = "ADC_04: Compare dropdown 'Apple Watch' link to ribbon 'Apple Watch' link")
    public void testADC04_AppleWatch() {
        compareLinks(3, "ADC_04");
    }

    // =========================================================================
    // ADC_05: AirPods → AirPod (dropdown has "AirPods", ribbon has "AirPod")
    // =========================================================================
    @Test(description = "ADC_05: Compare dropdown 'AirPods' link to ribbon 'AirPod' link")
    public void testADC05_AirPods() {
        compareLinks(4, "ADC_05");
    }

    // =========================================================================
    // ADC_06: Accessories → Accessories
    // =========================================================================
    @Test(description = "ADC_06: Compare dropdown 'Accessories' link to ribbon 'Accessories' link")
    public void testADC06_Accessories() {
        compareLinks(5, "ADC_06");
    }
}
