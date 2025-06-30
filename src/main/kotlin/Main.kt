package org.example
import com.microsoft.playwright.*


fun main() {
    Playwright.create().use { playwright ->
        val browser = playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(true)
        )
        val page = browser.newPage()
        page.navigate("https://google.com")

        val html = page.content()
        println(html)

        browser.close()
    }
}