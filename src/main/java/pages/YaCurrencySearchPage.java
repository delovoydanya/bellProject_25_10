package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;

public class YaCurrencySearchPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    // Контейнер результатов
    private final By SEARCH_RESULTS_CONTAINER = By.id("search-result");

    // Ссылки-результаты (внутри которых заголовки)
    private final By RESULT_LINKS = By.xpath("//li[contains(@class, 'serp-item')]//a");

    public YaCurrencySearchPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 15);
    }
    public YaCurrencySearchPage closePopupIfPresent() {
        try {
            WebElement popupCloseButton = driver.findElement(
                    By.xpath("//button[@type='button' and @title='Нет, спасибо']")
            );
            if (popupCloseButton.isDisplayed()) {
                popupCloseButton.click();
            }
        } catch (Exception ignored) {
            // Окно не появилось — ничего не делаем
        }
        return this; // для цепочки вызовов
    }
    public YaCurrencySearchPage waitForSearchResults() {
        // Сначала попробуем закрыть всплывающее окно (оно может мешать)
        closePopupIfPresent();

        wait.until(ExpectedConditions.presenceOfElementLocated(SEARCH_RESULTS_CONTAINER));
        return this;
    }

    /**
     * Находит и кликает по ссылке, содержащей текст "Официальные курсы валют на заданную дату"
     * @return true, если клик выполнен; false — если не найдено
     */
    public boolean clickOfficialRatesLink() {
        List<WebElement> links = driver.findElements(RESULT_LINKS);
        for (WebElement link : links) {
            String text = link.getText().trim();
            if (text.startsWith("Официальные курсы валют на заданную дату")) {
                link.click();
                return true;
            }
        }
        return false;
    }
}