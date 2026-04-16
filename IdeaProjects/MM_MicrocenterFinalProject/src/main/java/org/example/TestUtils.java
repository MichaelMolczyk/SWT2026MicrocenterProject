package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Shared utility methods for all Microcenter test classes.
 */
public class TestUtils {

    // ---- Click helpers ----

    /** Click via JavaScript — avoids ElementNotInteractableException */
    public static void jsClick(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /** Try normal click first; fall back to JS click if blocked */
    public static void safeClick(WebDriver driver, WebElement element) {
        try {
            element.click();
        } catch (ElementNotInteractableException | ElementClickInterceptedException e) {
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

    // ---- Navigation ----

    /** Navigate via JS (bypasses Cloudflare bot detection on driver.get()) */
    public static void navigateTo(WebDriver driver, String url) {
        ((JavascriptExecutor) driver).executeScript(
                "window.location.href = arguments[0];", url);
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(
                d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete"));
        sleep(1500);
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
