package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class compPage {

    private WebDriver driver;
    private WebDriverWait wait;
    // The Computers category page on Microcenter
    private static final String COMPUTERS_URL = "https://www.microcenter.com/site/products/computers.aspx";

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

        driver.get(COMPUTERS_URL);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================================
    // CPE_01: Compare sidebar "All Desktop Computers" link with
    //         main content "Desktop Computers" link — both should go
    //         to the same destination
    // =========================================================================
    @Test(description = "CPE_01: Verify sidebar desktop link and main content desktop link point to the same page")
    public void testDesktopLinksMatch() {
        // Find the "All Desktop Computers" link in the left sidebar
        WebElement sidebarLink = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//a[contains(text(),'All Desktop Computers')]")));
        String sidebarHref = sidebarLink.getAttribute("href");
        Assert.assertNotNull(sidebarHref,
                "Sidebar 'All Desktop Computers' link has no href.");

        // Find the "Desktop Computers" link/image in the main content area
        // This is one of the category cards with an image and text below it
        WebElement mainContentLink = driver.findElement(
                By.xpath("//a[contains(text(),'Desktop Computers') " +
                         "and not(contains(text(),'All Desktop')) " +
                         "and not(contains(text(),'Gaming'))]" +
                         " | //a[.//text()[contains(.,'Desktop Computers')] " +
                         "and not(contains(.,'All')) and not(contains(.,'Gaming'))]"));
        String mainHref = mainContentLink.getAttribute("href");
        Assert.assertNotNull(mainHref,
                "Main content 'Desktop Computers' link has no href.");

        // Compare the two URLs — they should resolve to the same destination
        // Normalize by trimming trailing slashes and converting to lowercase
        String normalizedSidebar = sidebarHref.toLowerCase().replaceAll("/$", "");
        String normalizedMain = mainHref.toLowerCase().replaceAll("/$", "");

        Assert.assertEquals(normalizedMain, normalizedSidebar,
                "Desktop links do not match.\n" +
                "Sidebar link: " + sidebarHref + "\n" +
                "Main content link: " + mainHref);
    }

    // =========================================================================
    // CPE_02: Verify YouTube embedded video for Priority Care
    // =========================================================================
    @Test(description = "CPE_02: Verify YouTube embedded video displays on computer page")
    public void testYouTubeEmbeddedVideo() {
        // Scroll down to find an embedded YouTube video (iframe)
        WebElement videoFrame = null;
        for (int scroll = 0; scroll < 15; scroll++) {
            List<WebElement> iframes = driver.findElements(
                    By.cssSelector("iframe[src*='youtube'], iframe[src*='youtu.be'], " +
                                   "iframe[data-src*='youtube'], " +
                                   "[class*='video'] iframe, [id*='video'] iframe"));
            if (!iframes.isEmpty()) {
                videoFrame = iframes.get(0);
                break;
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        Assert.assertNotNull(videoFrame,
                "No YouTube embedded video found on the computer page.");

        // Scroll the video into view
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", videoFrame);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Verify the iframe is displayed
        Assert.assertTrue(videoFrame.isDisplayed(),
                "YouTube video iframe is not visible.");

        // Verify the src contains a valid YouTube URL
        String src = videoFrame.getAttribute("src");
        if (src == null || src.isEmpty()) {
            src = videoFrame.getAttribute("data-src");
        }
        Assert.assertNotNull(src, "YouTube iframe has no src or data-src attribute.");
        Assert.assertTrue(src.contains("youtube") || src.contains("youtu.be"),
                "iframe src does not point to YouTube. src: " + src);
    }

    // =========================================================================
    // CPE_03: Verify computer side banner displays and is functional
    // =========================================================================
    @Test(description = "CPE_03: Verify computer side banner displays and responds to interaction")
    public void testComputerSideBanner() {
        // Look for a side banner (sidebar advertisement or promotional banner)
        List<WebElement> sideBanners = driver.findElements(
                By.cssSelector("[class*='side'] [class*='banner'], " +
                               "[class*='sidebar'] [class*='banner'], " +
                               "[class*='side-banner'], [class*='sideBanner'], " +
                               "[class*='sidebar'] img[src*='banner'], " +
                               "[class*='sidebar'] a[href] img"));

        if (sideBanners.isEmpty()) {
            // Broader search — look for any banner-like elements on the side
            sideBanners = driver.findElements(
                    By.cssSelector("[class*='banner'], [class*='promo'], " +
                                   "[class*='ad-'] img, [class*='advertisement']"));
        }
        Assert.assertFalse(sideBanners.isEmpty(),
                "No side banner found on the computer page.");

        WebElement banner = sideBanners.get(0);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", banner);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Verify it's displayed
        Assert.assertTrue(banner.isDisplayed(),
                "Side banner is not visible on the computer page.");

        // Check if the banner is clickable (has a parent link or is itself a link)
        String tagName = banner.getTagName().toLowerCase();
        WebElement clickableElement = banner;
        if (!tagName.equals("a")) {
            try {
                clickableElement = banner.findElement(By.xpath("./ancestor::a | ./a | ."));
            } catch (NoSuchElementException e) {
                clickableElement = banner;
            }
        }

        // Verify the banner is interactive (has an href or responds to click)
        String href = clickableElement.getAttribute("href");
        Assert.assertTrue(href != null || clickableElement.isEnabled(),
                "Side banner is not interactive — no link or clickable element.");
    }

    // =========================================================================
    // CPE_04: Verify Priority Care banner displays and is functional
    // =========================================================================
    @Test(description = "CPE_04: Verify priority care banner displays and responds to interaction")
    public void testPriorityCareBanner() {
        // Search for the Priority Care banner/section
        WebElement priorityCareBanner = null;
        for (int scroll = 0; scroll < 15; scroll++) {
            List<WebElement> elements = driver.findElements(
                    By.xpath("//*[contains(text(),'Priority Care') or " +
                             "contains(text(),'priority care') or " +
                             "contains(@alt,'Priority Care') or " +
                             "contains(@title,'Priority Care')]" +
                             " | //img[contains(@src,'priority') or contains(@src,'Priority')]" +
                             " | //a[contains(@href,'priority')]"));
            if (!elements.isEmpty()) {
                priorityCareBanner = elements.get(0);
                break;
            }
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        Assert.assertNotNull(priorityCareBanner,
                "Priority Care banner not found on the computer page.");

        // Scroll it into view
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", priorityCareBanner);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Verify it's displayed
        Assert.assertTrue(priorityCareBanner.isDisplayed(),
                "Priority Care banner is not visible.");

        // Verify it's interactive (clickable link or has associated link)
        WebElement clickable = priorityCareBanner;
        try {
            clickable = priorityCareBanner.findElement(
                    By.xpath("./ancestor::a | ./a | ."));
        } catch (NoSuchElementException ignored) {}

        String href = clickable.getAttribute("href");
        Assert.assertTrue(href != null || clickable.isEnabled(),
                "Priority Care banner is not interactive.");
    }

    // =========================================================================
    // CPE_05: Verify one of the images on the page loads correctly
    // =========================================================================
    @Test(description = "CPE_05: Verify a product/promotional image loads without errors")
    public void testImageVerification() {
        // Find product or promotional images on the page
        List<WebElement> images = driver.findElements(
                By.cssSelector("img[src*='microcenter'], img[src*='product'], " +
                               "img[class*='product'], img[class*='category'], " +
                               "img[alt]:not([src=''])"));

        // Filter to only visible, meaningful images (not tiny icons)
        WebElement targetImage = null;
        for (WebElement img : images) {
            if (img.isDisplayed()) {
                int width = img.getSize().getWidth();
                int height = img.getSize().getHeight();
                if (width > 50 && height > 50) {
                    targetImage = img;
                    break;
                }
            }
        }
        Assert.assertNotNull(targetImage,
                "No meaningful product/promotional image found on the page.");

        // Scroll the image into view
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", targetImage);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Verify the image is displayed
        Assert.assertTrue(targetImage.isDisplayed(),
                "Image is not visible on the page.");

        // Verify the image loaded correctly using JavaScript naturalWidth check
        Boolean imageLoaded = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].complete && " +
                "typeof arguments[0].naturalWidth !== 'undefined' && " +
                "arguments[0].naturalWidth > 0;", targetImage);
        Assert.assertTrue(imageLoaded,
                "Image did not load correctly (naturalWidth is 0 or image incomplete). " +
                "src: " + targetImage.getAttribute("src"));

        // Verify the image src is not a broken link
        String src = targetImage.getAttribute("src");
        Assert.assertNotNull(src, "Image has no src attribute.");
        Assert.assertFalse(src.isEmpty(), "Image src is empty.");
    }
}
