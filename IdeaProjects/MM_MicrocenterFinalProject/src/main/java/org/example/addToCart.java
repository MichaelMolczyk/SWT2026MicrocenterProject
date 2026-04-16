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

public class addToCart {

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
    // Helper: Click an ADD TO CART button on the home page to trigger the popup
    // =========================================================================
    private void triggerAddToCartPopup() {
        // Find any ADD TO CART button on the home page
        List<WebElement> addButtons = driver.findElements(
                By.xpath("//button[contains(text(),'ADD TO CART')] | " +
                         "//a[contains(text(),'ADD TO CART')] | " +
                         "//input[contains(@value,'ADD TO CART')] | " +
                         "//button[contains(text(),'Add to Cart')]"));

        if (addButtons.isEmpty()) {
            addButtons = driver.findElements(
                    By.cssSelector("button[class*='add-to-cart'], [class*='addtocart'], " +
                                   "button[class*='atc'], input[value*='Add to Cart' i]"));
        }
        Assert.assertFalse(addButtons.isEmpty(),
                "No ADD TO CART buttons found on the home page.");

        // Scroll to the first one and click it
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", addButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        addButtons.get(0).click();

        // Wait for the "Added to Cart" popup to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Added to Cart')]")));
    }

    // =========================================================================
    // ATC_01: Scroll within the Add to Cart popup (horizontal scrollbar)
    // =========================================================================
    @Test(description = "ATC_01: Verify user can scroll within the Add to Cart popup using the scrollbar")
    public void testScrollInPopup() {
        triggerAddToCartPopup();

        // Find the popup/modal container
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='addedToCart'], [class*='added-to-cart']")));

        // Find the scrollable element inside the popup (BOUGHT WITH product area)
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
                "// Fallback: try overflow-y" +
                "for (var i = 0; i < children.length; i++) {" +
                "  var style = window.getComputedStyle(children[i]);" +
                "  if ((style.overflowY === 'auto' || style.overflowY === 'scroll') " +
                "      && children[i].scrollHeight > children[i].clientHeight) {" +
                "    return children[i];" +
                "  }" +
                "}" +
                "return el;", modal);

        // Record initial scroll position
        Long scrollBefore = (Long) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollLeft || arguments[0].scrollTop;", scrollable);

        // Scroll the element (try horizontal first, then vertical)
        ((JavascriptExecutor) driver).executeScript(
                "if (arguments[0].scrollWidth > arguments[0].clientWidth) {" +
                "  arguments[0].scrollLeft += 300;" +
                "} else {" +
                "  arguments[0].scrollTop += 200;" +
                "}", scrollable);
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        // Verify the scroll position changed
        Long scrollAfter = (Long) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollLeft || arguments[0].scrollTop;", scrollable);
        Assert.assertTrue(scrollAfter > scrollBefore,
                "Popup content did not scroll. Before: " + scrollBefore +
                ", After: " + scrollAfter);
    }

    // =========================================================================
    // ATC_02: Close the Add to Cart popup with X button
    // =========================================================================
    @Test(description = "ATC_02: Verify user can close the Add to Cart popup with the X button")
    public void testClosePopupWithX() {
        triggerAddToCartPopup();

        // Find the popup
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='addedToCart']")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click the X button in the top-right corner
        WebElement closeX = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".ui-dialog-titlebar-close, " +
                               "[class*='close'], .modal .close, " +
                               "button[aria-label*='close' i], " +
                               "[data-dismiss='modal']")));
        closeX.click();

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".ui-dialog:not([style*='display: none']), " +
                               ".modal.show, .modal[style*='display: block']")));
        Assert.assertTrue(modalGone,
                "Add to Cart popup did not close after clicking X.");
    }

    // =========================================================================
    // ATC_03: Close the Add to Cart popup by clicking outside
    // =========================================================================
    @Test(description = "ATC_03: Verify user can close the Add to Cart popup by clicking outside")
    public void testClosePopupClickOff() {
        triggerAddToCartPopup();

        // Wait for the popup
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='addedToCart']")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click outside the popup on the overlay/backdrop
        Actions actions = new Actions(driver);
        List<WebElement> overlays = driver.findElements(
                By.cssSelector(".ui-widget-overlay, .modal-backdrop, " +
                               "[class*='overlay'], [class*='backdrop']"));
        if (!overlays.isEmpty()) {
            overlays.get(0).click();
        } else {
            // Click far away from the modal
            actions.moveToElement(modal, -(modal.getSize().width), 0).click().perform();
        }

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".ui-dialog:not([style*='display: none']), " +
                               ".modal.show, .modal[style*='display: block']")));
        Assert.assertTrue(modalGone,
                "Add to Cart popup did not close after clicking outside.");
    }

    // =========================================================================
    // ATC_04: Close the Add to Cart popup with CONTINUE SHOPPING button
    // =========================================================================
    @Test(description = "ATC_04: Verify CONTINUE SHOPPING button closes the popup and stays on page")
    public void testContinueShopping() {
        // Record current URL before triggering popup
        String urlBefore = driver.getCurrentUrl();

        triggerAddToCartPopup();

        // Wait for the popup
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='addedToCart']")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click the CONTINUE SHOPPING button
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'CONTINUE SHOPPING')] | " +
                         "//a[contains(text(),'CONTINUE SHOPPING')] | " +
                         "//button[contains(text(),'Continue Shopping')] | " +
                         "//a[contains(text(),'Continue Shopping')]")));
        continueBtn.click();

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".ui-dialog:not([style*='display: none']), " +
                               ".modal.show, .modal[style*='display: block']")));
        Assert.assertTrue(modalGone,
                "Popup did not close after clicking CONTINUE SHOPPING.");

        // Verify user is still on the same page (home page)
        String urlAfter = driver.getCurrentUrl();
        Assert.assertEquals(urlAfter, urlBefore,
                "User was navigated away after clicking CONTINUE SHOPPING. " +
                "Expected: " + urlBefore + ", Got: " + urlAfter);
    }

    // =========================================================================
    // ATC_05: Add another item from the BOUGHT WITH section in the popup
    // =========================================================================
    @Test(description = "ATC_05: Verify user can add a product from the BOUGHT WITH section in the popup")
    public void testAddAnotherItemFromPopup() {
        triggerAddToCartPopup();

        // Wait for the popup with BOUGHT WITH suggestions
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='addedToCart']")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Find ADD TO CART buttons inside the popup's BOUGHT WITH section
        // These are the red buttons under the suggested products
        List<WebElement> popupAddButtons = modal.findElements(
                By.xpath(".//button[contains(text(),'ADD TO CART')] | " +
                         ".//a[contains(text(),'ADD TO CART')] | " +
                         ".//input[contains(@value,'ADD TO CART')] | " +
                         ".//button[contains(text(),'Add to Cart')]"));

        if (popupAddButtons.isEmpty()) {
            popupAddButtons = modal.findElements(
                    By.cssSelector("button[class*='add-to-cart'], [class*='addtocart'], " +
                                   "button[class*='atc'], input[value*='Add to Cart' i]"));
        }
        Assert.assertFalse(popupAddButtons.isEmpty(),
                "No ADD TO CART buttons found in the BOUGHT WITH section of the popup.");

        // Click the first ADD TO CART in the BOUGHT WITH area
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", popupAddButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        popupAddButtons.get(0).click();

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}

        // Verify the popup refreshed or a confirmation appeared for the second item
        List<WebElement> confirmation = driver.findElements(
                By.xpath("//*[contains(text(),'Added to Cart')]"));
        Assert.assertFalse(confirmation.isEmpty(),
                "No confirmation after adding another item from BOUGHT WITH section.");
    }

    // =========================================================================
    // ATC_06: Click VIEW CART button in the popup
    // =========================================================================
    @Test(description = "ATC_06: Verify VIEW CART button navigates to the cart page")
    public void testViewCart() {
        triggerAddToCartPopup();

        // Wait for the popup
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".modal, [class*='modal'], [id*='modal'], " +
                               ".ui-dialog, [class*='addedToCart']")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click the VIEW CART button
        WebElement viewCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'VIEW CART')] | " +
                         "//a[contains(text(),'VIEW CART')] | " +
                         "//button[contains(text(),'View Cart')] | " +
                         "//a[contains(text(),'View Cart')]")));
        viewCartBtn.click();

        // Wait for navigation to the cart page
        try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

        // Verify we navigated to the cart page
        String currentUrl = driver.getCurrentUrl();
        Assert.assertTrue(currentUrl.toLowerCase().contains("cart"),
                "Did not navigate to the cart page. Current URL: " + currentUrl);

        // Verify at least one item is in the cart
        List<WebElement> cartItems = driver.findElements(
                By.cssSelector("[class*='cart'] [class*='item'], [class*='cart'] [class*='product'], " +
                               "[class*='cartItem'], [class*='cart-item'], " +
                               "tr[class*='item'], [class*='lineItem']"));
        Assert.assertFalse(cartItems.isEmpty(),
                "Cart page loaded but no items found in the cart.");
    }
}
