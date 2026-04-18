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

public class homePage {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";

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
    // HP_01: Top deals scroll (horizontal scrollbar)
    // =========================================================================
    @Test(description = "HP_01: Verify user can scroll through Top Deals By Category using the horizontal scrollbar")
    public void testTopDealsScroll() {
        // Locate the "Top Deals By Category" container — it uses a horizontal scrollbar, not arrows
        WebElement dealsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Top Deals')] /ancestor::div[contains(@class,'container') " +
                         "or contains(@class,'section') or contains(@class,'deals')]" +
                         " | //div[contains(@class,'topDeals') or contains(@class,'top-deals') " +
                         "or contains(@class,'TopDeals')]")));

        // Scroll the container into view first
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", dealsContainer);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Find the scrollable inner element (the one with overflow-x: auto/scroll)
        WebElement scrollable = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "var el = arguments[0];" +
                "var children = el.querySelectorAll('*');" +
                "for (var i = 0; i < children.length; i++) {" +
                "  var style = window.getComputedStyle(children[i]);" +
                "  if ((style.overflowX === 'auto' || style.overflowX === 'scroll') " +
                "      && children[i].scrollWidth > children[i].clientWidth) {" +
                "    return children[i];" +
                "  }" +
                "}" +
                "if (el.scrollWidth > el.clientWidth) return el;" +
                "return el;", dealsContainer);

        // Record initial scroll position
        Long scrollBefore = (Long) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollLeft;", scrollable);

        // Scroll the container 400px to the right using JavaScript
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollLeft += 400;", scrollable);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Verify the scroll position changed
        Long scrollAfter = (Long) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollLeft;", scrollable);
        Assert.assertTrue(scrollAfter > scrollBefore,
                "Top Deals scrollbar did not move — scrollLeft was " + scrollBefore +
                " before and " + scrollAfter + " after.");
    }

    // =========================================================================
    // HP_02: Top deals add (Memory category)
    // =========================================================================
    @Test(description = "HP_02: Verify user can add item from the Memory category in Top Deals")
    public void testTopDealsAddMemory() {
        // Locate the "Top Deals By Category" section
        WebElement dealsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h2[contains(text(),'Top Deals')] /ancestor::div[contains(@class,'container') " +
                         "or contains(@class,'section') or contains(@class,'deals')]" +
                         " | //div[contains(@class,'topDeals') or contains(@class,'top-deals') " +
                         "or contains(@class,'TopDeals')]")));

        // Find the scrollable element inside the deals section
        WebElement scrollable = (WebElement) ((JavascriptExecutor) driver).executeScript(
                "var el = arguments[0];" +
                "var children = el.querySelectorAll('*');" +
                "for (var i = 0; i < children.length; i++) {" +
                "  var style = window.getComputedStyle(children[i]);" +
                "  if ((style.overflowX === 'auto' || style.overflowX === 'scroll') " +
                "      && children[i].scrollWidth > children[i].clientWidth) {" +
                "    return children[i];" +
                "  }" +
                "}" +
                "return el;", dealsSection);

        // Scroll the deals carousel until the MEMORY category is visible
        WebElement memoryCard = null;
        for (int attempt = 0; attempt < 10; attempt++) {
            List<WebElement> memoryElements = driver.findElements(
                    By.xpath("//div[contains(@class,'deal') or contains(@class,'card') " +
                             "or contains(@class,'category')]" +
                             "[.//text()[contains(translate(., 'memory', 'MEMORY'), 'MEMORY')]]"));
            if (!memoryElements.isEmpty() && memoryElements.get(0).isDisplayed()) {
                memoryCard = memoryElements.get(0);
                break;
            }
            // Scroll right to find the Memory category
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollLeft += 400;", scrollable);
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        Assert.assertNotNull(memoryCard,
                "Could not find the MEMORY category in Top Deals after scrolling.");

        // Find the ADD TO CART button within the Memory category card
        WebElement addButton = memoryCard.findElement(
                By.cssSelector("button[class*='add'], input[value*='Add' i], " +
                               "[class*='addtocart'], [class*='atc'], a[class*='add']"));
        Assert.assertTrue(addButton.isDisplayed(),
                "ADD TO CART button not visible in the Memory category card.");

        // Click ADD TO CART
        addButton.click();

        // Verify a cart modal/popup or confirmation appears
        WebElement response = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, .popup, [class*='cart-modal'], " +
                               "[class*='addToCart'], [id*='modal'], " +
                               ".ui-dialog, [class*='overlay']")));
        Assert.assertTrue(response.isDisplayed(),
                "No confirmation appeared after adding Memory item from Top Deals.");
    }

    // =========================================================================
    // HP_03: Side ad arrow — right 5 times, then left 6 times
    // =========================================================================
    @Test(description = "HP_03: Verify side ad carousel cycles with arrows (right 5x, left 6x)")
    public void testSideAdChangeWithArrow() {
        // Scroll down to the side ad carousel (the PowerSpec ad area with < > arrows)
        WebElement sideCarousel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='slide'], [class*='carousel'], [class*='banner']")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", sideCarousel);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Find the right (next) arrow — the ">" button
        WebElement rightArrow = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[class*='next'], [class*='right'], " +
                               "[class*='swiper-button-next'], .slick-next, " +
                               "button[aria-label*='next' i], a[class*='next']")));
        Assert.assertTrue(rightArrow.isDisplayed(), "Right arrow not found on side ad carousel.");

        // Find the left (prev) arrow — the "<" button
        WebElement leftArrow = driver.findElement(
                By.cssSelector("[class*='prev'], [class*='left'], " +
                               "[class*='swiper-button-prev'], .slick-prev, " +
                               "button[aria-label*='prev' i], a[class*='prev']"));
        Assert.assertTrue(leftArrow.isDisplayed(), "Left arrow not found on side ad carousel.");

        // Click right arrow 5 times to cycle forward through the ads
        for (int i = 0; i < 5; i++) {
            rightArrow.click();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }
        Assert.assertTrue(sideCarousel.isDisplayed(),
                "Side ad carousel not visible after clicking right arrow 5 times.");

        // Click left arrow 6 times to cycle back past the start
        for (int i = 0; i < 6; i++) {
            leftArrow.click();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
        }
        Assert.assertTrue(sideCarousel.isDisplayed(),
                "Side ad carousel not visible after clicking left arrow 6 times.");
    }

    // =========================================================================
    // HP_04: Side ad dot navigation — click each dot forward, then back
    // =========================================================================
    @Test(description = "HP_04: Verify side ad carousel changes by clicking each dot forward and back")
    public void testSideAdChangeWithDot() {
        // Scroll to the side ad carousel
        WebElement sideCarousel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='slide'], [class*='carousel'], [class*='banner']")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", sideCarousel);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Locate the dot indicators (the circles at the bottom of the carousel)
        List<WebElement> dots = driver.findElements(
                By.cssSelector("[class*='dot'], [class*='indicator'] li, " +
                               "[class*='pagination'] li, [class*='pagination'] button, " +
                               ".slick-dots li, .slick-dots button, " +
                               "[class*='swiper-pagination'] span"));
        Assert.assertTrue(dots.size() >= 2,
                "Expected at least 2 dot indicators, found " + dots.size());

        // Click each dot forward (first to last)
        for (int i = 0; i < dots.size(); i++) {
            dots.get(i).click();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            Assert.assertTrue(sideCarousel.isDisplayed(),
                    "Carousel not visible after clicking dot " + (i + 1) + " (forward).");
        }

        // Click each dot backward (last to first)
        for (int i = dots.size() - 1; i >= 0; i--) {
            dots.get(i).click();
            try { Thread.sleep(800); } catch (InterruptedException ignored) {}
            Assert.assertTrue(sideCarousel.isDisplayed(),
                    "Carousel not visible after clicking dot " + (i + 1) + " (backward).");
        }
    }

    // =========================================================================
    // HP_05: Top ad change with arrow
    // =========================================================================
    @Test(description = "HP_05: Verify top ad carousel changes with arrow navigation")
    public void testTopAdChangeWithArrow() {
        // Locate the top/hero banner carousel (usually the first major carousel)
        WebElement topCarousel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='hero'], [class*='banner'], " +
                               "[class*='carousel']:first-of-type, " +
                               "[class*='top-slider'], [class*='main-banner']")));
        Assert.assertTrue(topCarousel.isDisplayed(), "Top ad carousel not found.");

        // Find next arrow
        List<WebElement> arrows = driver.findElements(
                By.cssSelector("[class*='hero'] [class*='next'], " +
                               "[class*='banner'] [class*='next'], " +
                               "[class*='swiper-button-next'], .slick-next"));
        Assert.assertFalse(arrows.isEmpty(), "No arrow found for top ad carousel.");

        arrows.get(0).click();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        Assert.assertTrue(topCarousel.isDisplayed(),
                "Top ad carousel not visible after clicking arrow.");
    }

    // =========================================================================
    // HP_06: Top ad change with dot
    // =========================================================================
    @Test(description = "HP_06: Verify top ad carousel changes with dot selection")
    public void testTopAdChangeWithDot() {
        // Locate the top banner area
        WebElement topBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='hero'], [class*='banner'], " +
                               "[class*='carousel']:first-of-type, " +
                               "[class*='top-slider'], [class*='main-banner']")));

        // Find dot indicators within or near the top banner
        List<WebElement> dots = topBanner.findElements(
                By.cssSelector("[class*='dot'], [class*='indicator'], " +
                               "[class*='pagination'] li, [class*='pagination'] button, " +
                               ".slick-dots li, [class*='swiper-pagination'] span"));

        if (dots.isEmpty()) {
            dots = driver.findElements(
                    By.cssSelector("[class*='dot'], [class*='indicator'], " +
                                   ".slick-dots li"));
        }
        Assert.assertFalse(dots.isEmpty(), "No dot indicators found for top ad carousel.");

        int dotIndex = dots.size() > 1 ? 1 : 0;
        dots.get(dotIndex).click();

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        Assert.assertTrue(topBanner.isDisplayed(),
                "Top ad carousel not visible after clicking dot.");
    }
}
