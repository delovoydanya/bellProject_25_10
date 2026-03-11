// pages/YaSearchResultsPage.java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;

public class YaSearchResultsPage {
    private static final Logger logger = LoggerFactory.getLogger(YaSearchResultsPage.class);
    private final WebDriver driver;
    private final WebDriverWait wait;

    // === ЛОКАТОРЫ (всё внутри класса) ===
    private final By SEARCH_RESULTS_CONTAINER = By.xpath("//ul[@id='search-result']");
    private final By MODAL_CLOSE_BUTTON = By.xpath("//button[@type='button' and @title='Нет, спасибо']");
    private final By ALL_RESULT_LINKS = By.xpath("//ul[@id='search-result']//a[@href]");


    /**
     * Закрывает всплывающее окно "Нет, спасибо", если оно появилось
     */
    public YaSearchResultsPage closeCapchaIfPresent() {
        try {
            WebElement closeCapcha = driver.findElement(MODAL_CLOSE_BUTTON);
            if (closeCapcha.isDisplayed()) {
                closeCapcha.click();
            }
        } catch (Exception ignored) {
            // Окно не появилось — ничего не делаем
        }
        return this;
    }

    /**
     * Ожидает появления контейнера с результатами поиска
     */
    public YaSearchResultsPage waitForSearchResults() {
        wait.until(ExpectedConditions.presenceOfElementLocated(SEARCH_RESULTS_CONTAINER));
        return this;
    }

    /**
     * Прокручивает к первой найденной ссылке на Ozon с гладиолусами
     */
    public YaSearchResultsPage scrollToOzonGladiolusLink() {
        List<WebElement> links = driver.findElements(ALL_RESULT_LINKS);
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && href.toLowerCase().contains("ozon.ru/category/gladiolusy")) {
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block: 'center'});", link
                );
                break;
            }
        }
        return this;
    }

    /**
     * Проверяет, что среди результатов есть ссылка на гладиолусы на Ozon
     */
    public boolean isOzonGladiolusLinkPresent() {
        return driver.findElements(ALL_RESULT_LINKS).stream()
                .map(el -> el.getAttribute("href"))
                .filter(href -> href != null)
                .anyMatch(href -> href.toLowerCase().contains("ozon.ru/category/gladiolusy"));
    }

    /**
     * Находит и возвращает элемент ссылки на страницу курсов ЦБ РФ
     *
     * Почему именно так:
     * - Возвращает сам элемент (не строку href), чтобы можно было кликнуть
     * - Использует точное сравнение без лишних пробелов
     * - Бросает исключение при отсутствии — тест упадёт осознанно
     * - Фильтрует по атрибуту элемента, а не преобразует в строку
     */
    public WebElement findCurrencyLinkElement() {
        logger.info("Поиск ссылки на курсы валют ЦБ РФ");

        WebElement link = driver.findElements(ALL_RESULT_LINKS).stream()
                .filter(el -> {
                    String href = el.getAttribute("href");
                    return href != null &&
                            href.toLowerCase().contains("https://cbr.ru/currency_base/daily/");
                })
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Ссылка на курсы валют не найдена");
                    return new NoSuchElementException("Ссылка на курсы валют ЦБ РФ не найдена");
                });

        logger.info("Ссылка найдена: {}", link.getAttribute("href"));
        return link;
    }

    // КОНСТРУКТОР — должен быть в КАЖДОМ классе страницы
    public YaSearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 15);
    }

    // ... остальные методы (findCurrencyLinkElement, clickCurrencyLink и т.д.) ...



    public CbrCurrencyPage clickCurrencyLink() {

        // Запоминаем текущую вкладку
        String originalWindow = driver.getWindowHandle();

        // Кликаем по ссылке
        findCurrencyLinkElement().click();

        // Ждём появления новой вкладки
        new WebDriverWait(driver, 10).until(
                webDriver -> webDriver.getWindowHandles().size() > 1
        );

        // Переключаемся на новую вкладку
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        return new CbrCurrencyPage(driver);
    }

    /**
     * Находит заголовок результата поиска по тексту и возвращает кликабельный элемент
     *
     * @param titleText текст для поиска в заголовке (регистронезависимый, частичное совпадение)
     * @return WebElement — кликабельный элемент ссылки
     * @throws NoSuchElementException если заголовок не найден
     */
    public WebElement findResultByTitle(String titleText) {
        logger.info("🔍 Ищем результат поиска с текстом заголовка: '{}'", titleText);

        // Правильный локатор для заголовков результатов Яндекса
        // Структура: <div class="OrganicTitle"> → <a> → <h2 class="OrganicTitle-LinkText">
        By RESULT_TITLES = By.xpath(
                "//div[contains(@class, 'OrganicTitle')]//h2[contains(@class, 'OrganicTitle-LinkText')]"
        );

        WebElement titleElement = driver.findElements(RESULT_TITLES).stream()
                .filter(el -> {
                    try {
                        String text = el.getText().trim();
                        // Пропускаем пустые или очень короткие тексты
                        if (text.isEmpty() || text.length() < 3) {
                            return false;
                        }
                        // Логируем для отладки первые совпадения
                        if (logger.isDebugEnabled()) {
                            logger.debug("  Проверяем: '{}'", text.length() > 60 ? text.substring(0, 60) + "..." : text);
                        }
                        return text.toLowerCase().contains(titleText.toLowerCase());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("❌ Результат с текстом '{}' не найден в заголовках", titleText);

                    // Для отладки выводим первые 5 заголовков
                    logger.warn("📋 Доступные заголовки в результатах поиска:");
                    int[] counter = {1}; // массив для захвата в лямбду
                    driver.findElements(RESULT_TITLES).stream()
                            .map(el -> el.getText().trim())
                            .filter(text -> !text.isEmpty() && text.length() >= 3)
                            .limit(5)
                            .forEach(text ->
                                    logger.warn("  {}. '{}'",
                                            counter[0]++,
                                            text.length() > 70 ? text.substring(0, 70) + "..." : text
                                    )
                            );

                    return new NoSuchElementException(
                            String.format("Результат с текстом '%s' не найден в заголовках поиска", titleText)
                    );
                });

        // НАХОДИМ РОДИТЕЛЬСКИЙ ЭЛЕМЕНТ <a> ДЛЯ КЛИКА
        WebElement resultLink = titleElement.findElement(
                By.xpath("./ancestor::a")
        );

        String foundTitle = titleElement.getText().trim();
        String href = resultLink.getAttribute("href");
        logger.info("✅ Найден результат: '{}' -> {}",
                foundTitle.length() > 70 ? foundTitle.substring(0, 70) + "..." : foundTitle,
                href != null ? href.substring(0, Math.min(href.length(), 80)) + (href.length() > 80 ? "..." : "") : "без href"
        );

        return resultLink; // Возвращаем кликабельный элемент <a>
    }

    public Wikipedia clickWikiHeader(WebElement header) {
        // Запоминаем текущую вкладку
        String originalWindow = driver.getWindowHandle();

        // Кликаем по хедеру
        header.click();

        // Ждём появления новой вкладки
        new WebDriverWait(driver, 10).until(
                webDriver -> webDriver.getWindowHandles().size() > 1
        );

        // Переключаемся на новую вкладку
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        return new Wikipedia(driver);
    }
}