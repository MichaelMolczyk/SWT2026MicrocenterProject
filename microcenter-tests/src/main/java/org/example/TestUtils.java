package org.example;

import me.bramar.undetectedselenium.UndetectedChromeDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared utility methods for all Microcenter test classes.
 */
public class TestUtils {

    private static final Random rnd = new Random();

    // Track whether we've already passed Cloudflare this session
    private static boolean cfCleared = false;

    // ---- Click helpers ----

    /** Click via JavaScript — avoids ElementNotInteractableException */
    public static void jsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /** Try normal click first; fall back to JS click if blocked */
    public static void safeClick(WebDriver driver, WebElement element) {
        try {
            element.click();
        } catch (ElementNotInteractableException e) {
            jsClick(driver, element);
        }
    }

    // ---- Scroll helpers ----

    /** Scroll element into the centre of the viewport */
    public static void scrollTo(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center', behavior:'instant'});", element);
        sleep(500);
    }

    // ---- Popup dismissal ----

    /**
     * Dismiss the "Unlock Exclusive Deals" email signup popup if present.
     * Microcenter shows this modal on first visit inside a SHADOW DOM:
     *   <div> → #shadow-root (open) → <div class="weblayer--box-promotion-1">
     *     → <button aria-label="Close" class="close">
     * Regular Selenium selectors cannot pierce shadow roots, so we use JavaScript.
     */
    public static void dismissPopups(WebDriver driver) {
        try {
            // === Strategy 1: Shadow DOM popup (Microcenter's "Unlock Exclusive Deals") ===
            // The popup lives inside a shadow root. We must use JS to traverse it.
            Boolean shadowDismissed = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "// Find all elements with shadow roots\n" +
                    "var allElements = document.querySelectorAll('*');\n" +
                    "for (var i = 0; i < allElements.length; i++) {\n" +
                    "  var sr = allElements[i].shadowRoot;\n" +
                    "  if (!sr) continue;\n" +
                    "  // Look for the weblayer promotion popup close button\n" +
                    "  var closeBtn = sr.querySelector('button.close, button[aria-label=\"Close\"]');\n" +
                    "  if (closeBtn) {\n" +
                    "    closeBtn.click();\n" +
                    "    return true;\n" +
                    "  }\n" +
                    "  // Also try any button with close-related attributes inside the shadow root\n" +
                    "  var allBtns = sr.querySelectorAll('button');\n" +
                    "  for (var j = 0; j < allBtns.length; j++) {\n" +
                    "    var aria = (allBtns[j].getAttribute('aria-label') || '').toLowerCase();\n" +
                    "    var cls = (allBtns[j].className || '').toLowerCase();\n" +
                    "    if (aria.includes('close') || cls.includes('close')) {\n" +
                    "      allBtns[j].click();\n" +
                    "      return true;\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "return false;");
            if (shadowDismissed != null && shadowDismissed) {
                sleep(1000);
                System.out.println("[TestUtils] Dismissed shadow DOM popup (Unlock Exclusive Deals).");
                return;
            }

            // === Strategy 2: Regular DOM popup close buttons ===
            List<WebElement> closeButtons = driver.findElements(
                    By.cssSelector(
                            "button.close, button[aria-label='Close'], " +
                            ".ui-dialog-titlebar-close, .modal .close, .modal-close, " +
                            "[class*='popup'] button[class*='close'], " +
                            "[class*='overlay'] button[class*='close']"
                    ));
            for (WebElement btn : closeButtons) {
                try {
                    if (btn.isDisplayed()) {
                        btn.click();
                        sleep(500);
                        System.out.println("[TestUtils] Dismissed a regular DOM popup.");
                        return;
                    }
                } catch (Exception ignored) {}
            }

            // === Strategy 3: Find overlay/modal with X/× text ===
            Boolean dismissed = (Boolean) ((JavascriptExecutor) driver).executeScript(
                    "var overlays = document.querySelectorAll('[class*=\"overlay\"], [class*=\"modal\"], [class*=\"popup\"]');\n" +
                    "for (var i = 0; i < overlays.length; i++) {\n" +
                    "  var ov = overlays[i];\n" +
                    "  if (ov.offsetParent === null) continue;\n" +
                    "  var btns = ov.querySelectorAll('button, a, span');\n" +
                    "  for (var j = 0; j < btns.length; j++) {\n" +
                    "    var txt = (btns[j].textContent || '').trim();\n" +
                    "    var aria = (btns[j].getAttribute('aria-label') || '').toLowerCase();\n" +
                    "    if ((txt === '×' || txt === 'X' || txt === '✕' || aria.includes('close')) && btns[j].offsetParent !== null) {\n" +
                    "      btns[j].click();\n" +
                    "      return true;\n" +
                    "    }\n" +
                    "  }\n" +
                    "}\n" +
                    "return false;");
            if (dismissed != null && dismissed) {
                sleep(500);
                System.out.println("[TestUtils] Dismissed a popup via JS X-button search.");
            }
        } catch (Exception ignored) {
            // No popup to dismiss — that's fine
        }
    }

    // ---- Navigation ----

    /**
     * Navigate to a URL with Cloudflare handling.
     *
     * On the FIRST call per driver session, uses cloudflareGet() which:
     *  - Opens the URL in a new tab
     *  - Waits 10+ seconds for Cloudflare's JS challenge to complete
     *  - Checks for the cf_clearance cookie
     *
     * Subsequent calls use regular get() since the cf_clearance cookie persists.
     */
    public static void navigateTo(WebDriver driver, String url) {
        if (driver instanceof UndetectedChromeDriver ucDriver) {
            if (!hasCfClearance(driver)) {
                // First navigation — use cloudflareGet to pass the challenge
                System.out.println("[TestUtils] Using cloudflareGet for: " + url);
                boolean passed = ucDriver.cloudflareGet(url, 12000);
                if (passed) {
                    System.out.println("[TestUtils] Cloudflare challenge passed!");
                } else {
                    System.out.println("[TestUtils] cloudflareGet returned false — retrying with longer wait...");
                    // Retry with longer wait
                    passed = ucDriver.cloudflareGet(url, 20000);
                    if (!passed) {
                        System.out.println("[TestUtils] WARNING: Cloudflare may still be blocking. Proceeding anyway.");
                    }
                }
                // Add human-like delay after clearing CF
                sleep(2000 + rnd.nextInt(2000));
                // Dismiss any popups (e.g. "Unlock Exclusive Deals" email signup)
                dismissPopups(driver);
                return;
            }
        }

        // Normal navigation (cf_clearance already present or not UndetectedChromeDriver)
        driver.get(url);

        // Wait for page load
        try {
            new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                    d -> ((JavascriptExecutor) d)
                            .executeScript("return document.readyState").equals("complete"));
        } catch (TimeoutException e) {
            System.out.println("[TestUtils] Page load timeout, continuing...");
        }

        // Wait for Cloudflare challenge if it appears (check for cf-stage div)
        waitForCloudflare(driver);

        // Human-like random delay
        sleep(1000 + rnd.nextInt(2000));

        // Dismiss any popups (e.g. "Unlock Exclusive Deals" email signup)
        dismissPopups(driver);
    }

    /**
     * Check if the cf_clearance cookie is present (means we passed Cloudflare).
     */
    public static boolean hasCfClearance(WebDriver driver) {
        return driver.manage().getCookies().stream()
                .anyMatch(c -> c.getName().equals("cf_clearance"));
    }

    /**
     * If Cloudflare's challenge page is showing, wait for it to resolve.
     * Checks for the #cf-stage div which indicates an active challenge.
     */
    private static void waitForCloudflare(WebDriver driver) {
        try {
            // Quick check — is the CF challenge page showing?
            List<WebElement> cfStage = driver.findElements(By.id("cf-stage"));
            if (!cfStage.isEmpty()) {
                System.out.println("[TestUtils] Cloudflare challenge detected, waiting...");
                // Wait up to 15 seconds for the challenge div to disappear
                new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(ExpectedConditions.invisibilityOfElementLocated(By.id("cf-stage")));
                // Extra wait after challenge clears
                sleep(3000);
                System.out.println("[TestUtils] Cloudflare challenge cleared.");
            }
        } catch (TimeoutException e) {
            System.out.println("[TestUtils] WARNING: Cloudflare challenge did not clear within 15s.");
        } catch (Exception ignored) {
            // No CF challenge present — proceed normally
        }
    }

    // ---- Sleep ----

    public static void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }

    // ---- Element finders ----

    /**
     * Return the first *visible* element matching any of the supplied selectors.
     * Falls back to a waited search if the initial quick scan finds nothing.
     */
    public static WebElement findFirst(WebDriver driver, WebDriverWait wait, By... selectors) {
        // Quick scan – no wait
        for (By sel : selectors) {
            try {
                for (WebElement el : driver.findElements(sel)) {
                    if (el.isDisplayed()) return el;
                }
            } catch (Exception ignored) {}
        }
        // Waited scan
        for (By sel : selectors) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(sel));
            } catch (Exception ignored) {}
        }
        throw new NoSuchElementException(
                "Could not find a visible element with any of the provided selectors.");
    }

    /** Return elements matching the first selector that produces a non-empty list */
    public static List<WebElement> findAll(WebDriver driver, By... selectors) {
        for (By sel : selectors) {
            List<WebElement> elems = driver.findElements(sel);
            if (!elems.isEmpty()) return elems;
        }
        return Collections.emptyList();
    }

    // ---- Hover helper ----

    public static void hover(WebDriver driver, WebElement element) {
        new Actions(driver).moveToElement(element).perform();
        sleep(1000);
    }

    // ---- Microcenter-specific: top nav ----

    /**
     * Find a top-level nav link inside {@code nav#navContainer}.
     * These links carry class {@code upperNav}.
     *
     * @param tabText  visible link text, e.g. "Computers", "PC Parts", "Apple"
     */
    public static WebElement findNavTab(WebDriver driver, WebDriverWait wait, String tabText) {
        return findFirst(driver, wait,
                By.xpath("//nav[@id='navContainer']//a[contains(normalize-space(),'" + tabText + "') and contains(@class,'upperNav')]"),
                By.xpath("//ul[@id='tabs']//a[contains(normalize-space(),'" + tabText + "')]"),
                By.cssSelector("nav#navContainer a.upperNav")
        );
    }

    /**
     * Hover over a nav tab, then find and return a dropdown link.
     * Dropdown links carry class {@code upperDropNav} inside {@code li.generalLink}.
     */
    public static WebElement findDropdownLink(WebDriver driver, WebDriverWait wait,
                                              String tabText, String linkText) {
        WebElement tab = findNavTab(driver, wait, tabText);
        hover(driver, tab);

        return findFirst(driver, wait,
                By.xpath("//li[contains(@class,'mainDropdownNav')]//a[contains(@class,'upperDropNav') and contains(normalize-space(),'" + linkText + "')]"),
                By.xpath("//li[@class='generalLink']//a[contains(normalize-space(),'" + linkText + "')]"),
                By.xpath("//a[contains(@class,'upperDropNav') and contains(normalize-space(),'" + linkText + "')]")
        );
    }

    // ---- Microcenter-specific: product buttons ----

    /** Find all ADD TO CART buttons on a product listing page */
    public static List<WebElement> findAddToCartButtons(WebDriver driver) {
        return findAll(driver,
                By.cssSelector("button.btn-add"),
                By.cssSelector("button[name='ADDtoCART']"),
                By.cssSelector("button.STBTN"),
                By.xpath("//button[contains(@value,'ADD TO CART')]"),
                By.xpath("//button[contains(text(),'ADD TO CART')]")
        );
    }

    /** Find all QUICK VIEW elements on a product listing page */
    public static List<WebElement> findQuickViewButtons(WebDriver driver) {
        return findAll(driver,
                By.cssSelector("div.quickview a, div.quickview button"),
                By.cssSelector("div.quickview"),
                By.xpath("//a[contains(text(),'QUICK VIEW') or contains(text(),'Quick View')]"),
                By.xpath("//button[contains(text(),'QUICK VIEW') or contains(text(),'Quick View')]")
        );
    }
}
