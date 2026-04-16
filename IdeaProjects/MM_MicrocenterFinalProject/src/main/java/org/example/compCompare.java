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

public class compCompare {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";

    // Dropdown category headers (the parent item you hover over in the Computers dropdown)
    private static final String[] DROPDOWN_OPTIONS = {
        "Desktops",              // CDC_01
        "Desktops",              // CDC_02
        "Desktops",              // CDC_03
        "Desktops",              // CDC_04
        "Laptops/Notebooks",     // CDC_05
        "Laptops/Notebooks",     // CDC_06
        "Laptops/Notebooks",     // CDC_07
        "Servers"                // CDC_08
    };

    // Corresponding page link texts (what appears on the Computers category page sidebar)
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
    // Helper: Get the href from the Computers dropdown for a given category
    //         Hovers over Computers tab, then finds the category link
    // =========================================================================
    private String getDropdownHref(String categoryName) {
        // Hover over the "Computers" tab in the top navigation to reveal dropdown
        WebElement computersTab = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(),'Computers') or contains(text(),'COMPUTERS')]" +
                         "[contains(@class,'nav') or ancestor::*[contains(@class,'nav') " +
                         "or contains(@class,'menu') or contains(@id,'nav')]]")));

        Actions actions = new Actions(driver);
        actions.moveToElement(computersTab).perform();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Find the category link in the dropdown (e.g. "Desktops", "Laptops/Notebooks", "Servers")
        WebElement dropdownLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[normalize-space(text())='" + categoryName + "' " +
                         "or contains(normalize-space(text()),'" + categoryName + "')]")));
        String href = dropdownLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Dropdown category link '" + categoryName + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Get the href from the Computers category page for a given link text
    // =========================================================================
    private String getPageHref(String linkText) {
        // Navigate to the Computers category page
        driver.get("https://www.microcenter.com/site/products/computers.aspx");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Find the link in the sidebar or main content
        WebElement pageLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(normalize-space(text()),'" + linkText + "')]")));
        String href = pageLink.getAttribute("href");
        Assert.assertNotNull(href,
                "Page link '" + linkText + "' has no href attribute.");
        return href.toLowerCase().replaceAll("/$", "");
    }

    // =========================================================================
    // Helper: Compare dropdown category URL to page link URL for a given index
    // =========================================================================
    private void compareLinks(int index, String testId) {
        String dropdownHref = getDropdownHref(DROPDOWN_OPTIONS[index]);

        // Navigate back to the home page to then go to the computers page
        driver.get(BASE_URL);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        String pageHref = getPageHref(PAGE_NAMES[index]);

        Assert.assertEquals(pageHref, dropdownHref,
                testId + ": URLs do not match.\n" +
                "Dropdown category '" + DROPDOWN_OPTIONS[index] + "': " + dropdownHref + "\n" +
                "Page link '" + PAGE_NAMES[index] + "': " + pageHref);
    }

    // =========================================================================
    // CDC_01: Desktops → All Desktop Computers
    // =========================================================================
    @Test(description = "CDC_01: Compare dropdown 'All Desktop Computers' link to page link")
    public void testCDC01_AllDesktopComputers() {
        compareLinks(0, "CDC_01");
    }

    // =========================================================================
    // CDC_02: Desktops → All Gaming Desktops
    // =========================================================================
    @Test(description = "CDC_02: Compare dropdown 'All Gaming Desktops' link to page link")
    public void testCDC02_AllGamingDesktops() {
        compareLinks(1, "CDC_02");
    }

    // =========================================================================
    // CDC_03: Desktops → Refurbished Desktops
    // =========================================================================
    @Test(description = "CDC_03: Compare dropdown 'Refurbished Desktops' link to page link")
    public void testCDC03_RefurbishedDesktops() {
        compareLinks(2, "CDC_03");
    }

    // =========================================================================
    // CDC_04: Desktops → Desktop Workstations
    // =========================================================================
    @Test(description = "CDC_04: Compare dropdown 'Desktop Workstations' link to page link")
    public void testCDC04_DesktopWorkstations() {
        compareLinks(3, "CDC_04");
    }

    // =========================================================================
    // CDC_05: Laptops/Notebooks → All Laptops / Notebooks
    // =========================================================================
    @Test(description = "CDC_05: Compare dropdown 'All Laptops / Notebooks' link to page link")
    public void testCDC05_AllLaptopsNotebooks() {
        compareLinks(4, "CDC_05");
    }

    // =========================================================================
    // CDC_06: Laptops/Notebooks → Refurbished Laptops
    // =========================================================================
    @Test(description = "CDC_06: Compare dropdown 'Refurbished Laptops' link to page link")
    public void testCDC06_RefurbishedLaptops() {
        compareLinks(5, "CDC_06");
    }

    // =========================================================================
    // CDC_07: Laptops/Notebooks → Laptop Workstations
    // =========================================================================
    @Test(description = "CDC_07: Compare dropdown 'Laptop Workstations' link to page link")
    public void testCDC07_LaptopWorkstations() {
        compareLinks(6, "CDC_07");
    }

    // =========================================================================
    // CDC_08: Servers → All Servers
    // =========================================================================
    @Test(description = "CDC_08: Compare dropdown 'All Servers' link to page link")
    public void testCDC08_AllServers() {
        compareLinks(7, "CDC_08");
    }
}
