# Microcenter Automated Test Suite

Selenium + TestNG + Maven automated test suite for [microcenter.com](https://www.microcenter.com).

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 11 or higher |
| Maven | 3.6+ |
| Chrome | Latest stable |
| Git | Any recent version |

> **ChromeDriver is managed automatically** by WebDriverManager — no manual download needed.

---

## Project Structure

```
microcenter-tests/
├── pom.xml                          ← Maven dependencies & build config
├── testng.xml                       ← Test suite definition (all 8 classes)
├── .gitignore
└── src/
    └── test/
        └── java/
            └── com/microcenter/
                ├── base/
                │   └── BaseTest.java            ← WebDriver setup/teardown
                └── tests/
                    ├── HomePageTest.java         ← TC-HP-01 to TC-HP-07
                    ├── TopDealsPageTest.java     ← TC-TD-01 to TC-TD-07
                    ├── AddToCartTest.java        ← TC-ATC-01 to TC-ATC-07
                    ├── CartOptionsTest.java      ← TC-CART-01 to TC-CART-07
                    ├── ComputersPageTest.java    ← TC-COMP-01 to TC-COMP-07
                    ├── PCPartsDropdownTest.java  ← TC-PCPARTS-01 to TC-PCPARTS-06
                    ├── ComputerDropdownTest.java ← TC-COMP-DROP-01 to TC-COMP-DROP-06
                    └── AppleDropdownTest.java    ← TC-APPLE-01 to TC-APPLE-07
```

---

## Test Classes Summary

| # | Class | What It Tests |
|---|-------|---------------|
| 1 | `HomePageTest` | Logo, nav bar, search bar, hero banner, footer, Top Deals link |
| 2 | `TopDealsPageTest` | Page load, products displayed, price/name/link on each card |
| 3 | `AddToCartTest` | Add to Cart button, popup modal, Store Pickup / Ship to Home / Continue Shopping / View Cart options |
| 4 | `CartOptionsTest` | Cart page load, empty state, item appears after add, quantity selector, remove button, subtotal, checkout button |
| 5 | `ComputersPageTest` | Category page load, heading, product listing, filter sidebar, embedded video (presence, visibility, valid src) |
| 6 | `PCPartsDropdownTest` | Hover dropdown exists, links present, sidebar exists; **URL comparison: dropdown vs sidebar** |
| 7 | `ComputerDropdownTest` | Same structure as #6 for the **Computers** nav item |
| 8 | `AppleDropdownTest` | Same structure for **Apple**, but compares against the **gray ribbon** secondary nav instead of a sidebar |

---

## Running the Tests

### Run the full suite
```bash
mvn test
```

### Run a single test class
```bash
mvn -Dtest=HomePageTest test
mvn -Dtest=PCPartsDropdownTest test
```

### Run in headless mode (no browser window)
In `BaseTest.java`, uncomment this line:
```java
options.addArguments("--headless=new");
```

---

## Test Reports

After each run, TestNG generates an HTML report at:
```
target/surefire-reports/index.html
```

The URL comparison tests (classes 6–8) also write detailed output directly to the console and to the `Reporters` section of the TestNG HTML report, showing:
- **URLs in BOTH** dropdown and sidebar/ribbon
- **URLs ONLY in dropdown** (not on the landing page)
- **URLs ONLY in sidebar/ribbon** (not in the dropdown)

---

## Pushing to GitHub

```bash
cd microcenter-tests
git init
git add .
git commit -m "Initial commit: Microcenter automated test suite"
git remote add origin https://github.com/YOUR_USERNAME/microcenter-tests.git
git push -u origin main
```

---

## Extending the Suite

Each test class follows the same pattern:
1. Extend `BaseTest` (gets fresh `driver` and `wait` per test method)
2. Annotate with `@Test` and a `description` string
3. Add `testng.xml` entry if creating a new class

The URL comparison pattern in classes 6–8 can be reused for any other dropdown nav item by changing the nav XPath and the landing-page sidebar/ribbon selector.
