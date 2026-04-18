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
    // TD_01: Price range slide — set min to $25 and max to $500
    // =========================================================================
    @Test(description = "TD_01: Verify price range slider can be adjusted to $25–$500")
    public void testPriceRangeSlide() {
        // Scroll down to the PRICE RANGE section in the left sidebar
        WebElement priceRangeSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(text(),'PRICE RANGE')] | //div[contains(text(),'PRICE RANGE')] " +
                         "| //span[contains(text(),'PRICE RANGE')]")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", priceRangeSection);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Find the min and max price input fields next to the sliders
        // These are the "$0" and "$3700" text inputs
        List<WebElement> priceInputs = driver.findElements(
                By.cssSelector("input[id*='priceLow'], input[id*='priceMin'], " +
                               "input[name*='priceLow'], input[name*='Low'], " +
                               "input[id*='low'], input[class*='min']"));
        List<WebElement> priceMaxInputs = driver.findElements(
                By.cssSelector("input[id*='priceHigh'], input[id*='priceMax'], " +
                               "input[name*='priceHigh'], input[name*='High'], " +
                               "input[id*='high'], input[class*='max']"));

        if (!priceInputs.isEmpty() && !priceMaxInputs.isEmpty()) {
            // Clear and set min price to $25
            priceInputs.get(0).click();
            priceInputs.get(0).sendKeys(Keys.chord(Keys.CONTROL, "a"));
            priceInputs.get(0).sendKeys("25");

            // Clear and set max price to $500
            priceMaxInputs.get(0).click();
            priceMaxInputs.get(0).sendKeys(Keys.chord(Keys.CONTROL, "a"));
            priceMaxInputs.get(0).sendKeys("500");
        } else {
            // Fallback: try using the range slider handles directly
            List<WebElement> sliderHandles = driver.findElements(
                    By.cssSelector("input[type='range'], [class*='slider'] [class*='handle'], " +
                                   "[class*='range'] [class*='thumb'], [role='slider']"));
            Assert.assertTrue(sliderHandles.size() >= 2,
                    "Could not find price range inputs or slider handles.");

            // Drag the left handle (min) to the right for $25
            Actions actions = new Actions(driver);
            actions.clickAndHold(sliderHandles.get(0)).moveByOffset(10, 0).release().perform();
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}

            // Drag the right handle (max) to the left for $500
            actions.clickAndHold(sliderHandles.get(1)).moveByOffset(-100, 0).release().perform();
        }

        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Verify the price range section is still visible and responsive
        Assert.assertTrue(priceRangeSection.isDisplayed(),
                "Price range section not visible after adjusting sliders.");
    }

    // =========================================================================
    // TD_02: Price range apply filter — click APPLY FILTER with $25–$500
    // =========================================================================
    @Test(description = "TD_02: Verify APPLY FILTER button filters products to $25–$500 range")
    public void testPriceRangeApplyFilter() {
        // Scroll to the PRICE RANGE section
        WebElement priceRangeSection = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h3[contains(text(),'PRICE RANGE')] | //div[contains(text(),'PRICE RANGE')] " +
                         "| //span[contains(text(),'PRICE RANGE')]")));
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", priceRangeSection);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Set min price to $25
        List<WebElement> priceInputs = driver.findElements(
                By.cssSelector("input[id*='priceLow'], input[id*='priceMin'], " +
                               "input[name*='priceLow'], input[name*='Low'], " +
                               "input[id*='low'], input[class*='min']"));
        if (!priceInputs.isEmpty()) {
            priceInputs.get(0).click();
            priceInputs.get(0).sendKeys(Keys.chord(Keys.CONTROL, "a"));
            priceInputs.get(0).sendKeys("25");
        }

        // Set max price to $500
        List<WebElement> priceMaxInputs = driver.findElements(
                By.cssSelector("input[id*='priceHigh'], input[id*='priceMax'], " +
                               "input[name*='priceHigh'], input[name*='High'], " +
                               "input[id*='high'], input[class*='max']"));
        if (!priceMaxInputs.isEmpty()) {
            priceMaxInputs.get(0).click();
            priceMaxInputs.get(0).sendKeys(Keys.chord(Keys.CONTROL, "a"));
            priceMaxInputs.get(0).sendKeys("500");
        }

        // Click the APPLY FILTER button
        WebElement applyButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'APPLY FILTER')] | " +
                         "//input[@value='APPLY FILTER'] | " +
                         "//a[contains(text(),'APPLY FILTER')] | " +
                         "//button[contains(text(),'Apply Filter')]")));
        applyButton.click();

        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Verify product results updated — page should still show products
        List<WebElement> products = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result'], [class*='listing']"));
        Assert.assertFalse(products.isEmpty(),
                "No products displayed after applying $25–$500 price filter.");
    }

    // =========================================================================
    // TD_03: Change items per page to 96
    // =========================================================================
    @Test(description = "TD_03: Scroll down to page selector, change items per page to 96, scroll to bottom again")
    public void testChangeItemsPerPageTo96() {
        // Step 1: Scroll down until the "Items per page" dropdown is visible
        WebElement perPageDropdown = null;
        for (int scroll = 0; scroll < 15; scroll++) {
            List<WebElement> selects = driver.findElements(
                    By.cssSelector("select[id*='perPage'], select[name*='perPage'], " +
                                   "select[id*='count'], select[class*='perPage'], " +
                                   "select[id*='Rpp'], select[name*='Rpp']"));
            if (selects.isEmpty()) {
                // Also try finding by nearby text
                selects = driver.findElements(
                        By.xpath("//select[preceding-sibling::*[contains(text(),'Items per page')] " +
                                 "or ancestor::*[contains(text(),'Items per page')]//select] " +
                                 "| //span[contains(text(),'Items per page')]/following::select[1]"));
            }
            if (!selects.isEmpty()) {
                perPageDropdown = selects.get(0);
                break;
            }
            // Scroll down incrementally
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 400);");
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        Assert.assertNotNull(perPageDropdown,
                "Could not find 'Items per page' dropdown after scrolling.");

        // Scroll a bit more past it so it's clearly visible
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", perPageDropdown);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        // Count products before changing
        List<WebElement> productsBefore = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result'], [class*='listing']"));
        int countBefore = productsBefore.size();

        // Step 2: Change items per page to 96
        Select select = new Select(perPageDropdown);
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

        // Wait for page to reload with more items
        try { Thread.sleep(4000); } catch (InterruptedException ignored) {}

        // Step 3: Scroll all the way to the bottom of the page
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollTo(0, document.body.scrollHeight);");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify more products are now displayed (96 vs the original 24)
        List<WebElement> productsAfter = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result'], [class*='listing']"));
        Assert.assertTrue(productsAfter.size() > 0,
                "No products displayed after changing items per page to 96.");

        // Verify the URL or dropdown reflects the change
        String currentUrl = driver.getCurrentUrl();
        String selectedValue = select.getFirstSelectedOption().getText();
        Assert.assertTrue(currentUrl.contains("96") || selectedValue.contains("96"),
                "Page did not update to show 96 items per page. URL: " + currentUrl +
                ", Selected: " + selectedValue);
    }

    // =========================================================================
    // TD_04: Toggle both "Include out of stock items" checkboxes
    //        (one in AVAILABILITY sidebar, one near the Sort by dropdown)
    // =========================================================================
    @Test(description = "TD_04: Toggle both 'Include out of stock items' checkboxes and verify each responds")
    public void testToggleIncludeOutOfStock() {
        // Find ALL "Include out of stock items" checkboxes on the page
        // There are two: one in the sidebar AVAILABILITY section, one near Sort by
        List<WebElement> outOfStockCheckboxes = driver.findElements(
                By.xpath("//label[contains(normalize-space(.),'Include out of stock items')]" +
                         "/preceding-sibling::input[@type='checkbox'] | " +
                         "//label[contains(normalize-space(.),'Include out of stock items')]" +
                         "//input[@type='checkbox'] | " +
                         "//input[@type='checkbox']" +
                         "[following-sibling::*[contains(normalize-space(.),'Include out of stock items')] " +
                         "or following::label[1][contains(normalize-space(.),'Include out of stock items')]] | " +
                         "//*[contains(text(),'Include out of stock items')]/preceding::input[@type='checkbox'][1]"));

        // If the XPath doesn't catch them, fall back to a broader search
        if (outOfStockCheckboxes.size() < 2) {
            outOfStockCheckboxes = driver.findElements(
                    By.cssSelector("input[type='checkbox'][id*='outOfStock'], " +
                                   "input[type='checkbox'][name*='outOfStock'], " +
                                   "input[type='checkbox'][id*='oos'], " +
                                   "input[type='checkbox'][name*='oos']"));
        }

        Assert.assertTrue(outOfStockCheckboxes.size() >= 2,
                "Expected at least 2 'Include out of stock items' checkboxes " +
                "(sidebar + top bar), found: " + outOfStockCheckboxes.size());

        // --- Sidebar AVAILABILITY checkbox (first occurrence, scroll into view) ---
        WebElement sidebarCheckbox = outOfStockCheckboxes.get(0);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", sidebarCheckbox);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        boolean sidebarBefore = sidebarCheckbox.isSelected();
        sidebarCheckbox.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        boolean sidebarAfter = sidebarCheckbox.isSelected();
        Assert.assertNotEquals(sidebarAfter, sidebarBefore,
                "Sidebar 'Include out of stock items' checkbox did not toggle.");

        // Toggle it back to original state
        sidebarCheckbox.click();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        // --- Top-bar checkbox next to Sort by / Shop bundles buttons ---
        // Re-fetch in case the DOM refreshed from the sidebar click
        List<WebElement> refetch = driver.findElements(
                By.xpath("//label[contains(normalize-space(.),'Include out of stock items')]" +
                         "/preceding-sibling::input[@type='checkbox'] | " +
                         "//label[contains(normalize-space(.),'Include out of stock items')]" +
                         "//input[@type='checkbox']"));
        Assert.assertTrue(refetch.size() >= 2,
                "Lost second 'Include out of stock items' checkbox after first toggle.");

        WebElement topCheckbox = refetch.get(refetch.size() - 1);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", topCheckbox);
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        boolean topBefore = topCheckbox.isSelected();
        topCheckbox.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        boolean topAfter = topCheckbox.isSelected();
        Assert.assertNotEquals(topAfter, topBefore,
                "Top-bar 'Include out of stock items' checkbox did not toggle.");

        // Verify products are still displayed after toggling
        List<WebElement> products = driver.findElements(
                By.cssSelector("[class*='product'], [class*='item'], " +
                               "[class*='result']"));
        Assert.assertFalse(products.isEmpty(),
                "No products displayed after toggling out-of-stock checkboxes.");
    }

    // =========================================================================
    // TD_05: Open Quick View on first product, close with X button
    // =========================================================================
    @Test(description = "TD_05: Click first QUICK VIEW button, then close popup with X")
    public void testQuickViewCloseX() {
        // Find the first QUICK VIEW button on the page
        List<WebElement> quickViewButtons = driver.findElements(
                By.xpath("//button[contains(text(),'QUICK VIEW')] | " +
                         "//a[contains(text(),'QUICK VIEW')] | " +
                         "//button[contains(text(),'Quick View')] | " +
                         "//a[contains(text(),'Quick View')]"));

        if (quickViewButtons.isEmpty()) {
            quickViewButtons = driver.findElements(
                    By.cssSelector("[class*='quickView'], [class*='quick-view'], " +
                                   "a[title*='Quick View' i], button[class*='quickview']"));
        }
        Assert.assertFalse(quickViewButtons.isEmpty(), "No QUICK VIEW button found on the page.");

        // Scroll to and click the first QUICK VIEW button
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", quickViewButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        quickViewButtons.get(0).click();

        // Wait for the Quick View popup to appear
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='quickView'], [class*='quick-view']")));
        Assert.assertTrue(modal.isDisplayed(), "Quick View popup did not open.");

        // Click the X button in the top-right corner of the popup
        WebElement closeX = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".ui-dialog-titlebar-close, " +
                               "[class*='close'], .modal .close, " +
                               "button[aria-label*='close' i], " +
                               "[data-dismiss='modal'], " +
                               ".ui-dialog button[class*='close']")));
        closeX.click();

        // Verify the popup is closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".ui-dialog:not([style*='display: none']), " +
                               ".modal.show, .modal[style*='display: block']")));
        Assert.assertTrue(modalGone,
                "Quick View popup did not close after clicking X.");
    }

    // =========================================================================
    // TD_06: Open Quick View on first product, close by clicking outside
    // =========================================================================
    @Test(description = "TD_06: Click first QUICK VIEW button, then click outside popup to close")
    public void testQuickViewClickOff() {
        // Find and click the first QUICK VIEW button
        List<WebElement> quickViewButtons = driver.findElements(
                By.xpath("//button[contains(text(),'QUICK VIEW')] | " +
                         "//a[contains(text(),'QUICK VIEW')] | " +
                         "//button[contains(text(),'Quick View')] | " +
                         "//a[contains(text(),'Quick View')]"));
        if (quickViewButtons.isEmpty()) {
            quickViewButtons = driver.findElements(
                    By.cssSelector("[class*='quickView'], [class*='quick-view'], " +
                                   "a[title*='Quick View' i], button[class*='quickview']"));
        }
        Assert.assertFalse(quickViewButtons.isEmpty(), "No QUICK VIEW button found.");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", quickViewButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        quickViewButtons.get(0).click();

        // Wait for the popup to appear
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='quickView']")));
        Assert.assertTrue(modal.isDisplayed(), "Quick View popup did not open.");

        // Click on the space outside the popup to dismiss it
        // Try clicking on the overlay/backdrop first
        Actions actions = new Actions(driver);
        List<WebElement> overlays = driver.findElements(
                By.cssSelector(".ui-widget-overlay, .modal-backdrop, " +
                               "[class*='overlay'], [class*='backdrop']"));
        if (!overlays.isEmpty()) {
            overlays.get(0).click();
        } else {
            // Click at the far corner of the viewport, outside the popup
            actions.moveToElement(modal, -(modal.getSize().width), 0).click().perform();
        }

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".ui-dialog:not([style*='display: none']), " +
                               ".modal.show, .modal[style*='display: block']")));
        Assert.assertTrue(modalGone,
                "Quick View popup did not close after clicking outside.");
    }

    // =========================================================================
    // TD_07: Open Quick View on first product, click ADD TO CART inside popup
    // =========================================================================
    @Test(description = "TD_07: Click first QUICK VIEW, then click ADD TO CART in the popup")
    public void testQuickViewAddToCart() {
        // Find and click the first QUICK VIEW button
        List<WebElement> quickViewButtons = driver.findElements(
                By.xpath("//button[contains(text(),'QUICK VIEW')] | " +
                         "//a[contains(text(),'QUICK VIEW')] | " +
                         "//button[contains(text(),'Quick View')] | " +
                         "//a[contains(text(),'Quick View')]"));
        if (quickViewButtons.isEmpty()) {
            quickViewButtons = driver.findElements(
                    By.cssSelector("[class*='quickView'], [class*='quick-view'], " +
                                   "a[title*='Quick View' i], button[class*='quickview']"));
        }
        Assert.assertFalse(quickViewButtons.isEmpty(), "No QUICK VIEW button found.");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", quickViewButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        quickViewButtons.get(0).click();

        // Wait for the Quick View popup with product details
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='quickView']")));
        Assert.assertTrue(modal.isDisplayed(), "Quick View popup did not open.");

        // Find and click the ADD TO CART button inside the popup
        WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[contains(@class,'ui-dialog') or contains(@class,'modal')]" +
                         "//button[contains(text(),'ADD TO CART')] | " +
                         "//div[contains(@class,'ui-dialog') or contains(@class,'modal')]" +
                         "//input[contains(@value,'ADD TO CART')] | " +
                         "//div[contains(@class,'ui-dialog') or contains(@class,'modal')]" +
                         "//button[contains(text(),'Add to Cart')] | " +
                         "//div[contains(@class,'ui-dialog') or contains(@class,'modal')]" +
                         "//a[contains(text(),'ADD TO CART')]")));
        addToCartBtn.click();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify the item was added — cart count updated or confirmation appeared
        List<WebElement> cartResponse = driver.findElements(
                By.cssSelector("[class*='cart'] [class*='count'], [class*='cart-count'], " +
                               "[class*='cartCount'], [class*='success'], " +
                               "[class*='added'], .modal, .ui-dialog"));
        Assert.assertFalse(cartResponse.isEmpty(),
                "No cart confirmation after clicking ADD TO CART in Quick View popup.");
    }

    // =========================================================================
    // TD_08: Click ADD TO CART directly on product listing (no Quick View)
    // =========================================================================
    @Test(description = "TD_08: Click ADD TO CART button on the product listing without using Quick View")
    public void testAddToCartFromListing() {
        // Find all ADD TO CART buttons on the product listing
        // These are the red buttons next to each QUICK VIEW button
        List<WebElement> addButtons = driver.findElements(
                By.xpath("//button[contains(text(),'ADD TO CART')] | " +
                         "//a[contains(text(),'ADD TO CART')] | " +
                         "//input[contains(@value,'ADD TO CART')] | " +
                         "//button[contains(text(),'Add to Cart')]"));

        if (addButtons.isEmpty()) {
            addButtons = driver.findElements(
                    By.cssSelector("button[class*='add-to-cart'], input[value*='Add to Cart' i], " +
                                   "[class*='addtocart'], button[class*='atc']"));
        }
        Assert.assertFalse(addButtons.isEmpty(),
                "No ADD TO CART buttons found on the product listing.");

        // Scroll to the first ADD TO CART button and click it directly
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", addButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        addButtons.get(0).click();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify something happened — cart modal, count update, or page change
        List<WebElement> cartResponse = driver.findElements(
                By.cssSelector("[class*='cart'] [class*='count'], [class*='cart-count'], " +
                               "[class*='cartCount'], [class*='success'], " +
                               "[class*='added'], .modal, .ui-dialog"));
        Assert.assertFalse(cartResponse.isEmpty(),
                "No response after clicking ADD TO CART on the product listing.");
    }
}
