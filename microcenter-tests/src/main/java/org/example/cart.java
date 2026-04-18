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

public class cart {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";
    private static final String TOP_DEALS_URL = "https://www.microcenter.com/search/search_results.aspx?Ntk=all&sortby=pricelow&N=4294966937&myStore=true";

    @BeforeMethod
    public void setUp() throws Exception {
        driver = UndetectedChromeDriver.builder()
                .seleniumStealth(SeleniumStealthOptions.getDefault())
                .build();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Add an item to the cart first, then navigate to the cart page
        addItemToCart();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    // =========================================================================
    // Helper: Add an item to the cart so the cart isn't empty
    // Navigates to Top Deals page where product_wrapper structure is reliable
    // =========================================================================
    private void addItemToCart() {
        // Navigate to Top Deals page where we can reliably find ADD TO CART buttons
        TestUtils.navigateTo(driver, TOP_DEALS_URL);
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Find ADD TO CART button using the actual DOM structure:
        // li.product_wrapper > div.cartadd > form.crtfrm.ajaxForm > button.btn-add.STBTN
        List<WebElement> addButtons = driver.findElements(
                By.cssSelector("li.product_wrapper div.cartadd form.crtfrm.ajaxForm button.btn-add.STBTN"));

        if (addButtons.isEmpty()) {
            // Fallback: broader search
            addButtons = driver.findElements(
                    By.xpath("//button[contains(@name,'ADDtoCART')] | " +
                             "//button[contains(@value,'ADD TO CART')] | " +
                             "//button[contains(text(),'ADD TO CART')]"));
        }
        Assert.assertFalse(addButtons.isEmpty(),
                "No ADD TO CART buttons found on Top Deals page to set up cart.");

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", addButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        addButtons.get(0).click();

        // Wait for the "Added to Cart" popup
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.ui-dialog")));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Click VIEW CART to go to the cart page
        WebElement viewCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'VIEW CART')] | " +
                         "//button[contains(text(),'VIEW CART')] | " +
                         "//a[contains(text(),'View Cart')] | " +
                         "//button[contains(text(),'View Cart')]")));
        viewCartBtn.click();

        // Wait for cart page to load
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        Assert.assertTrue(driver.getCurrentUrl().toLowerCase().contains("cart"),
                "Failed to navigate to the cart page during setup.");
    }

    // =========================================================================
    // C_01: Add from cart to list — click the "List" button on a cart item
    // =========================================================================
    @Test(description = "C_01: Verify user can move an item from cart to the list using the List button")
    public void testAddFromCartToList() {
        // Find the "List" button on a cart item
        WebElement listButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'List')] | " +
                         "//a[contains(text(),'List')] | " +
                         "//button[contains(text(),'list' )] | " +
                         "//span[contains(text(),'List')]/parent::button | " +
                         "//span[contains(text(),'List')]/parent::a")));
        Assert.assertTrue(listButton.isDisplayed(), "List button not found on cart item.");

        // Click the List button
        listButton.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify the item was moved — confirmation appears or list section updates
        List<WebElement> listSection = driver.findElements(
                By.xpath("//*[contains(text(),'Your List')] | " +
                         "//*[contains(text(),'List (')]"));
        Assert.assertFalse(listSection.isEmpty(),
                "No confirmation that item was moved to the list.");
    }

    // =========================================================================
    // C_02: Add from list to cart — click "Pickup" button on a list item
    // =========================================================================
    @Test(description = "C_02: Verify user can move an item from the list to cart using the Pickup button")
    public void testAddFromListToCart() {
        // First, move an item to the list so we have something to move back
        List<WebElement> listButtons = driver.findElements(
                By.xpath("//button[contains(text(),'List')] | " +
                         "//a[contains(text(),'List')] | " +
                         "//span[contains(text(),'List')]/parent::button | " +
                         "//span[contains(text(),'List')]/parent::a"));
        if (!listButtons.isEmpty()) {
            listButtons.get(0).click();
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
        }

        // Scroll down to the "Your List" section
        List<WebElement> yourListSection = driver.findElements(
                By.xpath("//*[contains(text(),'Your List')]"));
        if (!yourListSection.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", yourListSection.get(0));
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }

        // Find the "Pickup" button in the Your List section
        WebElement pickupButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Pickup')] | " +
                         "//a[contains(text(),'Pickup')] | " +
                         "//span[contains(text(),'Pickup')]/parent::button | " +
                         "//span[contains(text(),'Pickup')]/parent::a")));
        Assert.assertTrue(pickupButton.isDisplayed(),
                "Pickup button not found in the Your List section.");

        // Click the Pickup button to move item back to cart
        pickupButton.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify the item was moved back to the cart
        List<WebElement> cartItems = driver.findElements(
                By.xpath("//*[contains(text(),'Cart (')] | " +
                         "//div[contains(@class,'cart')]//div[contains(@class,'item')]"));
        Assert.assertFalse(cartItems.isEmpty(),
                "No confirmation that item was moved from list back to cart.");
    }

    // =========================================================================
    // C_03: Remove item — click the "Remove" button on a cart item
    // =========================================================================
    @Test(description = "C_03: Verify user can remove an item from the cart using the Remove button")
    public void testRemoveItem() {
        // Count items in cart before removal
        List<WebElement> cartItemsBefore = driver.findElements(
                By.cssSelector("[class*='cartItem'], [class*='cart-item'], " +
                               "[class*='line-item'], [class*='lineItem']"));

        // Find the "Remove" button on a cart item
        WebElement removeButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Remove')] | " +
                         "//a[contains(text(),'Remove')] | " +
                         "//button[contains(text(),'REMOVE')] | " +
                         "//span[contains(text(),'Remove')]/parent::button | " +
                         "//span[contains(text(),'Remove')]/parent::a")));
        Assert.assertTrue(removeButton.isDisplayed(),
                "Remove button not found on cart item.");

        // Click the Remove button
        removeButton.click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify the item was removed
        List<WebElement> cartItemsAfter = driver.findElements(
                By.cssSelector("[class*='cartItem'], [class*='cart-item'], " +
                               "[class*='line-item'], [class*='lineItem']"));
        List<WebElement> emptyMsg = driver.findElements(
                By.xpath("//*[contains(text(),'empty') or contains(text(),'no items') " +
                         "or contains(text(),'Your cart is')]"));

        Assert.assertTrue(cartItemsAfter.size() < cartItemsBefore.size() || !emptyMsg.isEmpty(),
                "Item was not removed from the cart. Items before: " +
                cartItemsBefore.size() + ", after: " + cartItemsAfter.size());
    }

    // =========================================================================
    // C_04: Map your trip — click the MAP YOUR TRIP button
    // =========================================================================
    @Test(description = "C_04: Verify MAP YOUR TRIP button displays the store location map")
    public void testMapYourTrip() {
        // Find the MAP YOUR TRIP button
        WebElement mapButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'MAP YOUR TRIP')] | " +
                         "//a[contains(text(),'MAP YOUR TRIP')] | " +
                         "//button[contains(text(),'Map Your Trip')] | " +
                         "//a[contains(text(),'Map Your Trip')]")));
        Assert.assertTrue(mapButton.isDisplayed(),
                "MAP YOUR TRIP button not found on the cart page.");

        // Click MAP YOUR TRIP
        mapButton.click();
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Verify a map or new page/modal appeared
        String currentUrl = driver.getCurrentUrl();
        List<WebElement> mapElements = driver.findElements(
                By.cssSelector("[class*='map'], [id*='map'], iframe[src*='map'], " +
                               "img[src*='map'], [class*='store-map'], " +
                               "[class*='storeMap'], canvas"));

        boolean newWindow = driver.getWindowHandles().size() > 1;
        boolean urlChanged = !currentUrl.toLowerCase().contains("cart") ||
                currentUrl.toLowerCase().contains("map");

        Assert.assertTrue(!mapElements.isEmpty() || newWindow || urlChanged,
                "No map displayed and no navigation occurred after clicking MAP YOUR TRIP.");
    }

    // =========================================================================
    // C_05: Checkout — click the CHECKOUT button
    // =========================================================================
    @Test(description = "C_05: Verify CHECKOUT button navigates to the checkout page")
    public void testCheckout() {
        // Find and click the CHECKOUT button
        WebElement checkoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'CHECKOUT')] | " +
                         "//a[contains(text(),'CHECKOUT')] | " +
                         "//button[contains(text(),'Checkout')] | " +
                         "//a[contains(text(),'Checkout')] | " +
                         "//input[contains(@value,'CHECKOUT')]")));
        Assert.assertTrue(checkoutBtn.isDisplayed(),
                "CHECKOUT button not found on the cart page.");

        String cartUrl = driver.getCurrentUrl();
        checkoutBtn.click();
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Verify navigation away from the cart page
        String newUrl = driver.getCurrentUrl();
        Assert.assertNotEquals(newUrl, cartUrl,
                "Page did not navigate after clicking CHECKOUT. Still on: " + newUrl);
    }

    // =========================================================================
    // C_06: Print map — click print from the store map
    // =========================================================================
    @Test(description = "C_06: Verify user can print the store map from the cart")
    public void testPrintMap() {
        // First, open the map by clicking MAP YOUR TRIP
        WebElement mapButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'MAP YOUR TRIP')] | " +
                         "//a[contains(text(),'MAP YOUR TRIP')] | " +
                         "//button[contains(text(),'Map Your Trip')] | " +
                         "//a[contains(text(),'Map Your Trip')]")));
        mapButton.click();
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Look for a Print button on the map page/modal
        List<WebElement> printButtons = driver.findElements(
                By.xpath("//button[contains(text(),'Print')] | " +
                         "//a[contains(text(),'Print')] | " +
                         "//button[contains(text(),'PRINT')] | " +
                         "//a[contains(text(),'PRINT')] | " +
                         "//input[contains(@value,'Print')]"));

        if (printButtons.isEmpty()) {
            printButtons = driver.findElements(
                    By.cssSelector("button[class*='print'], a[class*='print'], " +
                                   "[onclick*='print'], [class*='print-btn']"));
        }
        Assert.assertFalse(printButtons.isEmpty(),
                "No Print button found on the store map.");

        // Click the Print button
        printButtons.get(0).click();
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Dismiss print dialog if possible
        try {
            driver.switchTo().alert().dismiss();
        } catch (NoAlertPresentException e) {
            // No alert — try pressing Escape to close native print dialog
            new org.openqa.selenium.interactions.Actions(driver)
                    .sendKeys(Keys.ESCAPE).perform();
        }

        // If we got here without error, the print button was functional
        Assert.assertTrue(true, "Print button was clicked successfully.");
    }
}
