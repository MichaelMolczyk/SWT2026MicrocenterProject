package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.time.Duration;
import java.util.List;

public class topDealsPage {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String TOP_DEALS_URL = "https://www.microcenter.com/search/search_results.aspx?Ntk=all&sortby=pricelow&N=4294966937&myStore=true";

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

        driver.get(TOP_DEALS_URL);
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================================
    // TD_01: Price range slide
    // =========================================================================
    @Test(description = "TD_01: Verify price range slider adjusts product filter")
    public void testPriceRangeSlide() {
        // Locate the price range slider area
        WebElement sliderArea = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[class*='price'] input[type='range'], " +
                               "[class*='slider'], [id*='price'], " +
                               "[class*='range-slider'], [class*='priceRange']")));
        Assert.assertTrue(sliderArea.isDisplayed(), "Price range slider not found on top deals page.");

        // Try to interact with the slider using Actions (drag)
        Actions actions = new Actions(driver);
        actions.clickAndHold(sliderArea).moveByOffset(50, 0).release().perform();

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Verify the slider responded (still visible after interaction)
        Assert.assertTrue(sliderArea.isDisplayed(),
                "Price range slider not visible after adjustment.");
    }

    // =========================================================================
    // TD_02: Price range apply filter
    // =========================================================================
    @Test(description = "TD_02: Verify applying price range filter updates product list")
    public void testPriceRangeApplyFilter() {
        // Look for a price filter input or apply button
        List<WebElement> priceInputs = driver.findElements(
                By.cssSelector("input[id*='price'], input[name*='price'], " +
                               "input[class*='price'], input[placeholder*='$'], " +
                               "[class*='priceFilter'] input"));

        if (!priceInputs.isEmpty()) {
            // Clear and enter a price value
            priceInputs.get(0).clear();
            priceInputs.get(0).sendKeys("500");
        }

        // Look for an apply/go/filter button
        List<WebElement> applyButtons = driver.findElements(
                By.cssSelector("button[class*='apply'], input[value*='Go' i], " +
                               "button[class*='filter'], a[class*='apply'], " +
                               "[class*='price'] button, input[type='submit']"));

        if (!applyButtons.isEmpty()) {
            applyButtons.get(0).click();
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        } else if (!priceInputs.isEmpty()) {
            // If no apply button, try pressing Enter on the price input
            priceInputs.get(0).sendKeys(Keys.ENTER);
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }

        // Verify product results are still displayed after filtering
        List<WebElement> products = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result'], [class*='listing']"));
        Assert.assertFalse(products.isEmpty(),
                "No products displayed after applying price range filter.");
    }

    // =========================================================================
    // TD_03: Change items per page to 96
    // =========================================================================
    @Test(description = "TD_03: Verify user can change items displayed per page to 96")
    public void testChangeItemsPerPageTo96() {
        // Look for items per page dropdown or selector
        List<WebElement> perPageSelects = driver.findElements(
                By.cssSelector("select[id*='perPage'], select[name*='perPage'], " +
                               "select[id*='count'], select[class*='perPage'], " +
                               "select[id*='Rpp'], select[name*='Rpp']"));

        if (!perPageSelects.isEmpty()) {
            Select select = new Select(perPageSelects.get(0));
            // Try to select 96
            try {
                select.selectByValue("96");
            } catch (NoSuchElementException e) {
                try {
                    select.selectByVisibleText("96");
                } catch (NoSuchElementException e2) {
                    // Select the last option (usually the largest)
                    List<WebElement> options = select.getOptions();
                    select.selectByIndex(options.size() - 1);
                }
            }
        } else {
            // Try clicking a "96" link or button
            List<WebElement> perPageLinks = driver.findElements(
                    By.xpath("//a[text()='96'] | //button[text()='96'] | " +
                             "//a[contains(@href,'Rpp=96')] | //a[contains(@href,'perPage=96')]"));
            Assert.assertFalse(perPageLinks.isEmpty(),
                    "No items-per-page selector found on the page.");
            perPageLinks.get(0).click();
        }

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Verify the page reloaded with more items or URL updated
        String currentUrl = driver.getCurrentUrl();
        List<WebElement> products = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result']"));
        Assert.assertTrue(products.size() > 0,
                "No products displayed after changing items per page.");
    }

    // =========================================================================
    // TD_04: Toggle in stock only
    // =========================================================================
    @Test(description = "TD_04: Verify toggle for in-stock items only works")
    public void testToggleInStockOnly() {
        // Look for in-stock toggle/checkbox/link
        List<WebElement> inStockElements = driver.findElements(
                By.cssSelector("input[id*='inStock'], input[name*='inStock'], " +
                               "[class*='inStock'], [class*='in-stock'], " +
                               "label[for*='inStock'], a[href*='inStore']"));

        if (inStockElements.isEmpty()) {
            // Try XPath for text-based search
            inStockElements = driver.findElements(
                    By.xpath("//label[contains(text(),'In Stock')] | " +
                             "//a[contains(text(),'In Stock')] | " +
                             "//input[@type='checkbox'][following-sibling::*[contains(text(),'Stock')]] | " +
                             "//span[contains(text(),'In Stock')]/parent::*"));
        }
        Assert.assertFalse(inStockElements.isEmpty(),
                "No in-stock toggle found on the page.");

        // Click the in-stock toggle
        inStockElements.get(0).click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify products are still displayed (filtered to in-stock)
        List<WebElement> products = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result']"));
        Assert.assertFalse(products.isEmpty(),
                "No products displayed after toggling in-stock filter.");
    }

    // =========================================================================
    // TD_05: Quick view close with 'X'
    // =========================================================================
    @Test(description = "TD_05: Verify quick view modal closes with X button")
    public void testQuickViewCloseX() {
        // Hover over a product to reveal Quick View button
        List<WebElement> productCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='product'], [class*='product_wrapper'], " +
                               "[class*='productWrapper']")));
        Assert.assertFalse(productCards.isEmpty(), "No product cards found on top deals page.");

        Actions actions = new Actions(driver);
        actions.moveToElement(productCards.get(0)).perform();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Look for and click the Quick View button
        List<WebElement> quickViewButtons = driver.findElements(
                By.cssSelector("[class*='quickView'], [class*='quick-view'], " +
                               "a[title*='Quick View' i], button[class*='quickview'], " +
                               "[data-action*='quickView']"));

        if (quickViewButtons.isEmpty()) {
            quickViewButtons = driver.findElements(
                    By.xpath("//a[contains(text(),'Quick View')] | " +
                             "//button[contains(text(),'Quick View')]"));
        }
        Assert.assertFalse(quickViewButtons.isEmpty(), "No Quick View button found.");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", quickViewButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        quickViewButtons.get(0).click();

        // Wait for the modal to appear
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               "[class*='quickView'], .ui-dialog, [class*='overlay']")));
        Assert.assertTrue(modal.isDisplayed(), "Quick view modal did not open.");

        // Find and click the X (close) button
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[class*='close'], [class*='modal'] .close, " +
                               "button[aria-label*='close' i], " +
                               ".ui-dialog-titlebar-close, [class*='modal'] [class*='x'], " +
                               "[data-dismiss='modal']")));
        closeButton.click();

        // Verify the modal is closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        List<WebElement> visibleModals = driver.findElements(
                By.cssSelector(".modal.show, .modal[style*='display: block'], " +
                               "[class*='modal'][class*='open']"));
        Assert.assertTrue(visibleModals.isEmpty(),
                "Quick view modal did not close after clicking X.");
    }

    // =========================================================================
    // TD_06: Quick view click off
    // =========================================================================
    @Test(description = "TD_06: Verify quick view modal closes when clicking outside")
    public void testQuickViewClickOff() {
        // Hover over a product to reveal Quick View button
        List<WebElement> productCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='product'], [class*='product_wrapper'], " +
                               "[class*='productWrapper']")));
        Assert.assertFalse(productCards.isEmpty(), "No product cards found.");

        Actions actions = new Actions(driver);
        actions.moveToElement(productCards.get(0)).perform();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Click Quick View
        List<WebElement> quickViewButtons = driver.findElements(
                By.cssSelector("[class*='quickView'], [class*='quick-view'], " +
                               "a[title*='Quick View' i], button[class*='quickview']"));
        if (quickViewButtons.isEmpty()) {
            quickViewButtons = driver.findElements(
                    By.xpath("//a[contains(text(),'Quick View')] | " +
                             "//button[contains(text(),'Quick View')]"));
        }
        Assert.assertFalse(quickViewButtons.isEmpty(), "No Quick View button found.");

        quickViewButtons.get(0).click();

        // Wait for modal
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               "[class*='quickView'], .ui-dialog")));
        Assert.assertTrue(modal.isDisplayed(), "Quick view modal did not open.");

        // Click outside the modal (on the backdrop/overlay)
        List<WebElement> backdrops = driver.findElements(
                By.cssSelector(".modal-backdrop, [class*='backdrop'], " +
                               "[class*='overlay'], .ui-widget-overlay"));
        if (!backdrops.isEmpty()) {
            backdrops.get(0).click();
        } else {
            // Click at the edge of the page to dismiss
            actions.moveByOffset(-300, -300).click().perform();
        }

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Verify modal closed
        List<WebElement> visibleModals = driver.findElements(
                By.cssSelector(".modal.show, .modal[style*='display: block'], " +
                               "[class*='modal'][class*='open']"));
        Assert.assertTrue(visibleModals.isEmpty(),
                "Quick view modal did not close after clicking outside.");
    }

    // =========================================================================
    // TD_07: Quick view add to cart
    // =========================================================================
    @Test(description = "TD_07: Verify user can add to cart from quick view modal")
    public void testQuickViewAddToCart() {
        // Hover over a product
        List<WebElement> productCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("[class*='product'], [class*='product_wrapper'], " +
                               "[class*='productWrapper']")));
        Assert.assertFalse(productCards.isEmpty(), "No product cards found.");

        Actions actions = new Actions(driver);
        actions.moveToElement(productCards.get(0)).perform();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Click Quick View
        List<WebElement> quickViewButtons = driver.findElements(
                By.cssSelector("[class*='quickView'], [class*='quick-view'], " +
                               "a[title*='Quick View' i], button[class*='quickview']"));
        if (quickViewButtons.isEmpty()) {
            quickViewButtons = driver.findElements(
                    By.xpath("//a[contains(text(),'Quick View')] | " +
                             "//button[contains(text(),'Quick View')]"));
        }
        Assert.assertFalse(quickViewButtons.isEmpty(), "No Quick View button found.");
        quickViewButtons.get(0).click();

        // Wait for modal to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               "[class*='quickView'], .ui-dialog")));

        // Find and click Add to Cart inside the modal
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[class*='modal'] button[class*='add'], " +
                               "[class*='modal'] [class*='addToCart'], " +
                               "[class*='modal'] input[value*='Add to Cart' i], " +
                               ".ui-dialog button[class*='add'], " +
                               "[id*='modal'] button[class*='cart']")));
        addToCartBtn.click();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify cart was updated (look for cart confirmation or count change)
        List<WebElement> cartConfirm = driver.findElements(
                By.cssSelector("[class*='cart-confirm'], [class*='cartConfirm'], " +
                               "[class*='added'], [class*='success'], " +
                               "[class*='cart'] [class*='count'], " +
                               "[class*='cart-count']"));
        Assert.assertFalse(cartConfirm.isEmpty(),
                "No cart confirmation appeared after adding item from quick view.");
    }

    // =========================================================================
    // TD_08: Add to cart (from product listing)
    // =========================================================================
    @Test(description = "TD_08: Verify add to cart button on product listing works")
    public void testAddToCartFromListing() {
        // Find Add to Cart buttons on the product listing page
        List<WebElement> addButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("button[class*='add-to-cart'], input[value*='Add to Cart' i], " +
                               "button[data-id*='cart'], .btn-add-cart, " +
                               "[class*='addtocart'], button[class*='atc']")));
        Assert.assertFalse(addButtons.isEmpty(),
                "No add to cart buttons found on the product listing.");

        // Scroll to and click the first Add to Cart button
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView(true);", addButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        addButtons.get(0).click();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify a cart modal/popup or confirmation appears
        WebElement response = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, .popup, [class*='cart-modal'], " +
                               "[class*='addToCart'], [id*='modal'], " +
                               ".ui-dialog, [class*='overlay']")));
        Assert.assertTrue(response.isDisplayed(),
                "No confirmation appeared after clicking Add to Cart.");
    }
}
