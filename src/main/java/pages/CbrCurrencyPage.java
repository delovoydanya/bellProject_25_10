package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;




public class CbrCurrencyPage {
    private static final Logger logger = LoggerFactory.getLogger(CbrCurrencyPage.class);
    private WebDriver driver;


    public CbrCurrencyPage(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Ждём загрузки страницы
     */
    public void waitForPageLoad() {
        logger.info("⏳ Ждём загрузки таблицы курсов...");

        // Правильный локатор для таблицы ЦБ РФ
        new WebDriverWait(driver, 20).until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.className("data")  // ← КЛАСС "data" — официальный класс таблицы ЦБ РФ
                )
        );

        logger.info("✅ Таблица курсов загружена");

    }

    public void debugPageStructure() {
        System.out.println("=== СТРУКТУРА СТРАНИЦЫ ЦБ РФ ===");
        System.out.println("URL: " + driver.getCurrentUrl());
        System.out.println("Заголовок: " + driver.getTitle());

        // Все таблицы на странице
        System.out.println("\nТаблицы (class/id):");
        driver.findElements(By.tagName("table")).forEach(table -> {
            String cls = table.getAttribute("class");
            String id = table.getAttribute("id");
            System.out.println("  • class='" + cls + "', id='" + id + "'");
        });

        // Заголовки h2 (там обычно "Курс валют")
        System.out.println("\nЗаголовки h2:");
        driver.findElements(By.tagName("h2")).forEach(h2 -> {
            System.out.println("  • " + h2.getText());
        });

        System.out.println("==============================");
    }

    public boolean isPageLoaded() {
        try {
            // Ищем таблицу с классом "data" — официальный класс таблицы ЦБ РФ
            WebElement table = driver.findElement(By.className("data"));
            return table.isDisplayed();
        } catch (Exception e) {
            logger.warn("Таблица курсов не найдена: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Получить курс валюты по её названию
     *
     * @param currencyName название валюты (например, "Лари", "Азербайджанский манат")
     * @return курс в рублях как double
     * @throws RuntimeException если валюта не найдена
     */
    public double getCurrencyRate(String currencyName) {
        logger.info("🔍 Ищем курс валюты: {}", currencyName);

        // Находим таблицу
        WebElement table = driver.findElement(By.className("data"));

        // Находим все строки таблицы (кроме заголовка)
        List<WebElement> rows = table.findElements(By.tagName("tr"));

        // Проходим по строкам (пропускаем первую — заголовок)
        for (int i = 1; i < rows.size(); i++) {
            WebElement row = rows.get(i);

            // Получаем все ячейки строки
            List<WebElement> cells = row.findElements(By.tagName("td"));

            // Проверяем, что строка имеет достаточно ячеек
            if (cells.size() >= 4) {
                String currencyText = cells.get(3).getText(); // 4-я ячейка — название валюты

                // Сравниваем название (игнорируем регистр и лишние пробелы)
                if (currencyText.toLowerCase().contains(currencyName.toLowerCase())) {
                    // 5-я ячейка — курс
                    String rateText = cells.get(4).getText();
                    logger.info("✅ Найдена валюта '{}' с курсом: {}", currencyName, rateText);

                    // Преобразуем строку в число (заменяем запятую на точку)
                    return parseRate(rateText);
                }
            }
        }

        throw new RuntimeException("Валюта '" + currencyName + "' не найдена в таблице");
    }

    /**
     * Вспомогательный метод: преобразовать строку курса в число
     * <p>
     * Пример: "28,7835" → 28.7835
     */
    private double parseRate(String rateText) {
        // Убираем пробелы и заменяем запятую на точку для парсинга
        String cleaned = rateText.replace(" ", "").replace(",", ".");
        return Double.parseDouble(cleaned);
    }

    /**
     * Проверка, что курс валюты1 меньше курса валюты2
     *
     * @param currency1 название первой валюты
     * @param currency2 название второй валюты
     * @return true, если курс currency1 < currency2
     */
    public boolean isCurrency1LessThanCurrency2(String currency1, String currency2) {
        double rate1 = getCurrencyRate(currency1);
        double rate2 = getCurrencyRate(currency2);
        return rate1 < rate2;
    }


    public boolean isPageTitleContains(String text) {
        return driver.getTitle().toLowerCase().contains(text.toLowerCase());
    }

    public boolean isCurrencyTablePresent() {
        try {
            // Правильный локатор для таблицы ЦБ РФ
            WebElement table = driver.findElement(By.className("data"));
            return table.isDisplayed();
        } catch (Exception e) {
            logger.warn("Таблица курсов не найдена: {}", e.getMessage());
            return false;
        }
    }
}


