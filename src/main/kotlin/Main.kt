package org.example
import com.microsoft.playwright.*
import org.pois_noir.botzilla.Component

fun main() {
    var scraper_component = Component("scraper", "ppap")

    val handler = { input : Map<String, String> ->
        // process input
        val barcode = input["barcode"].toString()
        println(barcode)
        scraper(barcode)

        Result.success(input) // example
    }

    scraper_component.onMessage = handler

    while (true) {}
}


fun scraper(barcode: String) {

    Playwright.create().use { playwright ->
        val browser = playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(true)
        )
        val page = browser.newPage()

        page.navigate("https://www.maxi.ca/en/search?search-bar=" + barcode)
        playwright.selectors().setTestIdAttribute("data-testid")

        page.waitForSelector("[data-testid='product-image']", Page.WaitForSelectorOptions())

        val header_element = page.querySelector("[data-testid='heading']")
        if (header_element != null && header_element.innerText() == "We were unable to find results for \"$barcode\"") {
            println("product is not found")
            return
        }

        val product_selectors = page.querySelectorAll(".css-yyn1h")

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