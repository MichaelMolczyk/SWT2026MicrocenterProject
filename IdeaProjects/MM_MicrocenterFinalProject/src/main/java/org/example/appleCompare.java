package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.interactions.Actions;
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
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new EdgeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        driver.get(BASE_URL);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================================
    // Helper: Get the href from the Apple dropdown for a given link text
    // =========================================================================
    private String getDropdownHref(String linkText) {
        // Hover over the "Apple" tab in the top navigation to reveal dropdown
        WebElement appleTab = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(),'Apple') or contains(text(),'APPLE')]" +
                         "[contains(@class,'nav') or ancestor::*[contains(@class,'nav') " +
                         "or contains(@class,'menu') or contains(@id,'nav')]]")));

        Actions actions = new Actions(driver);
        actions.moveToElement(appleTab).perform();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Find the specific link in the dropdown
        WebElement dropdownLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[normalize-space(text())='" + linkText + "' " +
                         "or contains(normalize-space(text()),'" + linkText + "')]")));
        String href = dropdownLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Dropdown link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Get the href from the Apple page ribbon for a given link text
    // =========================================================================
    private String getRibbonHref(String linkText) {
        // Navigate to the Apple page
        driver.get("https://www.microcenter.com/site/products/apple.aspx");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Find the link in the dark ribbon bar at the top of the Apple page
        // The ribbon contains: APPLE, MacBook, Mac Desktops, iPad, AirPod, Apple Watch, Accessories, Compare Mac Models
        WebElement ribbonLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[normalize-space(text())='" + linkText + "' " +
                         "or contains(normalize-space(text()),'" + linkText + "')]")));
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
        driver.get(BASE_URL);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

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
