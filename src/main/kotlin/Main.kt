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

        val product_selectors = page.querySelectorAll(".css-yyn1h")

//        println(items[0].innerText())
        for (product in product_selectors) {
            val img = product.querySelector("img").getAttribute("src")
            val title = product.querySelector("[data-testid='product-title']").innerText()

            var price_element = product.querySelector("[data-testid='regular-price']")
            if (price_element == null) {
                price_element = product.querySelector("[data-testid='sale-price']")
            }

            val price = price_element.innerText()

            println(img)
            println(title)
            println(price)
        }

        browser.close()
    }
}