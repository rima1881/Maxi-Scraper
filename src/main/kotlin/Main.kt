package org.example
import com.microsoft.playwright.*
import com.microsoft.playwright.options.LoadState
import com.microsoft.playwright.options.WaitForSelectorState
import com.microsoft.playwright.options.WaitUntilState


fun main() {
    Playwright.create().use { playwright ->
        val browser = playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(true)
        )
        val page = browser.newPage()

        page.navigate("https://www.maxi.ca/en/search?search-bar=eggs")
        playwright.selectors().setTestIdAttribute("data-testid")

        page.waitForSelector("[data-testid='product-image']", Page.WaitForSelectorOptions())
//        val html = page.content()
//        println(html)

        val items = page.querySelectorAll("[data-testid='product-title']")

        for (item in items) {
            println(item.innerText())
        }

        browser.close()
    }
}