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

public class addToCart {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "https://www.microcenter.com";
    private static final String TOP_DEALS_URL = "https://www.microcenter.com/search/search_results.aspx?Ntk=all&sortby=pricelow&N=4294966937&myStore=true";

    @BeforeMethod
    public void setUp() {
        EdgeOptions options = new EdgeOptions();
        options.setExperimentalOption("debuggerAddress", "localhost:9222");

        driver = new EdgeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        navigateTo(BASE_URL);
    }

    @AfterMethod
    public void tearDown() {
        driver = null;
    }

    private void navigateTo(String url) {
        ((JavascriptExecutor) driver).executeScript("window.location.href = arguments[0];", url);
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    // =========================================================================
    // Helper: Click an ADD TO CART button to trigger the popup
    // Navigates to Top Deals page where product_wrapper structure is reliable
    // =========================================================================
    private void triggerAddToCartPopup() {
        // Navigate to Top Deals page where we can reliably find ADD TO CART buttons
        navigateTo(TOP_DEALS_URL);
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
                "No ADD TO CART buttons found. Unable to trigger popup.");

        // Scroll to the first one and click it
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", addButtons.get(0));
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        addButtons.get(0).click();

        // Wait for the "Added to Cart" popup to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.ui-dialog, div[class*='addedCart']")));
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    // =========================================================================
    // ATC_01: Scroll within the Add to Cart popup (horizontal scrollbar)
    // =========================================================================
    @Test(description = "ATC_01: Verify user can scroll within the Add to Cart popup using the scrollbar")
    public void testScrollInPopup() {
        triggerAddToCartPopup();

        // Find the popup/modal container (jQuery UI dialog)
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.ui-dialog")));

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
                By.cssSelector("div.ui-dialog")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click the X button in the top-right corner (jQuery UI dialog close button)
        WebElement closeX = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.ui-dialog-titlebar-close, " +
                               "span.ui-icon-closethick, " +
                               ".ui-dialog .ui-dialog-titlebar button")));
        closeX.click();

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div.ui-dialog:not([style*='display: none'])")));
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
                By.cssSelector("div.ui-dialog")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click outside the popup on the overlay/backdrop
        Actions actions = new Actions(driver);
        List<WebElement> overlays = driver.findElements(
                By.cssSelector(".ui-widget-overlay"));
        if (!overlays.isEmpty()) {
            overlays.get(0).click();
        } else {
            // Click far away from the modal
            actions.moveToElement(modal, -(modal.getSize().width), 0).click().perform();
        }

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div.ui-dialog:not([style*='display: none'])")));
        Assert.assertTrue(modalGone,
                "Add to Cart popup did not close after clicking outside.");
    }

    // =========================================================================
    // ATC_04: Close the Add to Cart popup with CONTINUE SHOPPING button
    // =========================================================================
    @Test(description = "ATC_04: Verify CONTINUE SHOPPING button closes the popup and stays on page")
    public void testContinueShopping() {
        // Record current URL before triggering popup
        triggerAddToCartPopup();
        String urlBefore = driver.getCurrentUrl();

        // Wait for the popup
        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.ui-dialog")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click the CONTINUE SHOPPING button
        WebElement continueBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'CONTINUE SHOPPING')] | " +
                         "//button[contains(text(),'CONTINUE SHOPPING')] | " +
                         "//a[contains(text(),'Continue Shopping')] | " +
                         "//button[contains(text(),'Continue Shopping')]")));
        continueBtn.click();

        // Verify the popup closed
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean modalGone = wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector("div.ui-dialog:not([style*='display: none'])")));
        Assert.assertTrue(modalGone,
                "Popup did not close after clicking CONTINUE SHOPPING.");

        // Verify user is still on the same page
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
                By.cssSelector("div.ui-dialog")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Find ADD TO CART buttons inside the popup's BOUGHT WITH section
        List<WebElement> popupAddButtons = modal.findElements(
                By.xpath(".//button[contains(@name,'ADDtoCART')] | " +
                         ".//button[contains(@value,'ADD TO CART')] | " +
                         ".//button[contains(text(),'ADD TO CART')] | " +
                         ".//button[contains(text(),'Add to Cart')]"));

        if (popupAddButtons.isEmpty()) {
            popupAddButtons = modal.findElements(
                    By.cssSelector("button.btn-add, button[class*='add-to-cart']"));
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
                By.cssSelector("div.ui-dialog")));
        Assert.assertTrue(modal.isDisplayed(), "Add to Cart popup did not appear.");

        // Click the VIEW CART button
        WebElement viewCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'VIEW CART')] | " +
                         "//button[contains(text(),'VIEW CART')] | " +
                         "//a[contains(text(),'View Cart')] | " +
                         "//button[contains(text(),'View Cart')]")));
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
