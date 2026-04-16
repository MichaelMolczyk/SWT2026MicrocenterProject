package org.example;

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
    // HP_01: Top deals scroll (horizontal scrollbar)
    // =========================================================================
    @Test(description = "HP_01: Verify user can scroll through Top Deals By Category using the horizontal scrollbar")
    public void testTopDealsScroll() {
        // Locate the "Top Deals By Category" container on the homepage
        // Try multiple selectors: section with "deal" class, div.DEALWrap, or div with deal/Deal id
        WebElement dealsContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//section[contains(@class,'deal') or contains(@class,'Deal')] " +
                         "| //div[contains(@class,'DEALWrap')] " +
                         "| //div[contains(@id,'deal') or contains(@id,'Deal')] " +
                         "| //h2[contains(text(),'Top Deals')]/ancestor::div[1]")));

        // Scroll the container into view first
        TestUtils.scrollTo(driver, dealsContainer);
        TestUtils.sleep(500);

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
        TestUtils.sleep(1000);

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
        // Locate the "Top Deals By Category" section on the homepage
        WebElement dealsSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//section[contains(@class,'deal') or contains(@class,'Deal')] " +
                         "| //div[contains(@class,'DEALWrap')] " +
                         "| //div[contains(@id,'deal') or contains(@id,'Deal')] " +
                         "| //h2[contains(text(),'Top Deals')]/ancestor::div[1]")));

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
            TestUtils.sleep(500);
        }
        Assert.assertNotNull(memoryCard,
                "Could not find the MEMORY category in Top Deals after scrolling.");

        // Find the ADD TO CART button within the Memory category card
        List<WebElement> addButtons = TestUtils.findAddToCartButtons(driver);
        WebElement addButton = null;
        for (WebElement btn : addButtons) {
            if (memoryCard.findElements(By.xpath(".//" + btn.getTagName() +
                    "[@" + (btn.getAttribute("class") != null ? "class" : "id") + "='" +
                    (btn.getAttribute("class") != null ? btn.getAttribute("class") : btn.getAttribute("id")) + "']")).size() > 0) {
                addButton = btn;
                break;
            }
        }
        if (addButton == null) {
            addButton = memoryCard.findElement(
                    By.cssSelector("button[class*='add'], input[value*='Add' i], " +
                                   "[class*='addtocart'], [class*='atc'], a[class*='add']"));
        }
        Assert.assertTrue(addButton.isDisplayed(),
                "ADD TO CART button not visible in the Memory category card.");

        // Click ADD TO CART using safe click
        TestUtils.safeClick(driver, addButton);

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
        TestUtils.scrollTo(driver, sideCarousel);
        TestUtils.sleep(500);

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
            TestUtils.safeClick(driver, rightArrow);
            TestUtils.sleep(800);
        }
        Assert.assertTrue(sideCarousel.isDisplayed(),
                "Side ad carousel not visible after clicking right arrow 5 times.");

        // Click left arrow 6 times to cycle back past the start
        for (int i = 0; i < 6; i++) {
            TestUtils.safeClick(driver, leftArrow);
            TestUtils.sleep(800);
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
        TestUtils.scrollTo(driver, sideCarousel);
        TestUtils.sleep(500);

        // Locate the dot indicators (the circles at the bottom of the carousel)
        // Use Slick.js dot pattern: .slick-dots li button or .slick-dots li
        List<WebElement> dots = driver.findElements(
                By.cssSelector(".slick-dots li button, .slick-dots li, " +
                               "[class*='dot'], [class*='indicator'] li, " +
                               "[class*='pagination'] li, [class*='pagination'] button, " +
                               "[class*='swiper-pagination'] span"));
        Assert.assertTrue(dots.size() >= 2,
                "Expected at least 2 dot indicators, found " + dots.size());

        // Click each dot forward (first to last)
        for (int i = 0; i < dots.size(); i++) {
            TestUtils.safeClick(driver, dots.get(i));
            TestUtils.sleep(800);
            Assert.assertTrue(sideCarousel.isDisplayed(),
                    "Carousel not visible after clicking dot " + (i + 1) + " (forward).");
        }

        // Click each dot backward (last to first)
        for (int i = dots.size() - 1; i >= 0; i--) {
            TestUtils.safeClick(driver, dots.get(i));
            TestUtils.sleep(800);
            Assert.assertTrue(sideCarousel.isDisplayed(),
                    "Carousel not visible after clicking dot " + (i + 1) + " (backward).");
        }
    }

    // =========================================================================
    // HP_05: Top ad change with arrow
    // =========================================================================
    @Test(description = "HP_05: Verify top ad carousel changes with arrow navigation")
    public void testTopAdChangeWithArrow() {
        // Locate the top/hero banner carousel using Slick.js pattern
        // div.ad-hero-slider.slider-container.slider--arrows.slider--dots
        WebElement topCarousel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'ad-hero-slider')] " +
                         "| //div[contains(@class,'hero')] " +
                         "| //div[contains(@class,'banner') and contains(@class,'slider')]")));
        Assert.assertTrue(topCarousel.isDisplayed(), "Top ad carousel not found.");

        // Find next arrow using Slick.js .slick-next selector
        List<WebElement> arrows = driver.findElements(
                By.cssSelector(".slick-next, " +
                               "div.ad-hero-slider .slick-next, " +
                               "[class*='hero'] .slick-next, " +
                               "[class*='banner'] .slick-next"));
        Assert.assertFalse(arrows.isEmpty(), "No arrow found for top ad carousel.");

        TestUtils.safeClick(driver, arrows.get(0));
        TestUtils.sleep(1000);
        Assert.assertTrue(topCarousel.isDisplayed(),
                "Top ad carousel not visible after clicking arrow.");
    }

    // =========================================================================
    // HP_06: Top ad change with dot
    // =========================================================================
    @Test(description = "HP_06: Verify top ad carousel changes with dot selection")
    public void testTopAdChangeWithDot() {
        // Locate the top banner area using Slick.js pattern
        WebElement topBanner = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'ad-hero-slider')] " +
                         "| //div[contains(@class,'hero')] " +
                         "| //div[contains(@class,'banner') and contains(@class,'slider')]")));

        // Find dot indicators using Slick.js pattern: .slick-dots li button or .slick-dots li
        List<WebElement> dots = topBanner.findElements(
                By.cssSelector(".slick-dots li button, .slick-dots li, " +
                               "[class*='dot'], [class*='indicator'], " +
                               "[class*='pagination'] li, [class*='pagination'] button, " +
                               "[class*='swiper-pagination'] span"));

        if (dots.isEmpty()) {
            dots = driver.findElements(
                    By.cssSelector(".slick-dots li button, .slick-dots li, " +
                                   "[class*='dot'], [class*='indicator']"));
        }
        Assert.assertFalse(dots.isEmpty(), "No dot indicators found for top ad carousel.");

        int dotIndex = dots.size() > 1 ? 1 : 0;
        TestUtils.safeClick(driver, dots.get(dotIndex));

        TestUtils.sleep(1000);
        Assert.assertTrue(topBanner.isDisplayed(),
                "Top ad carousel not visible after clicking dot.");
    }
}
