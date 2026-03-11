package tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.*;
import ru.ya.BaseTest;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class Tests extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(Tests.class);
    // === ПОЛЯ КЛАССА ===
    /**
     * Тестовый класс для проверки поиска в Яндексе + обработки модального окна
     *
     * Почему такая структура:
     * 1. YaBeforeSearch — главная страница (до поиска)
     * 2. YaSearchResultsPage — страница результатов (после поиска)
     * 3. Методы возвращают this — можно строить цепочки вызовов
     */
    private YaBeforeSearch yandexMainPage;
    private YaSearchResultsPage searchResults;
    // === НАСТРОЙКА ПЕРЕД КАЖДЫМ ТЕСТОМ ===
    /**
     * Метод выполняется ДО каждого теста (@Test)
     *
     * Почему @BeforeEach здесь, а не в родительском классе:
     * - Браузер уже создан в родительском @BeforeEach
     * - Здесь мы только инициализируем страницу
     * - Порядок выполнения: родительский @BeforeEach → дочерний @BeforeEach
     */
    @BeforeEach
    public void setUp(){
        // ШАГ 1: Создание экземпляра страницы и привязка к браузеру
        // Почему именно здесь:
        // - Браузер (chromeDriver) уже создан в родительском @BeforeEach
        // - Страница не может работать без браузера
        // - Поле chromeDriver доступно, потому что оно protected в BaseTest
        logger.info("=== Инициализация теста ===");
        yandexMainPage = new YaBeforeSearch(chromeDriver);
        logger.info("Главная страница Яндекса инициализирована");
    }

    @Test
    public void firstTest() {
        // ШАГ 1: Открываем главную страницу Яндекса
        // Почему отдельный метод: разделение ответственности
        // Внутри вызывается: chromeDriver.get("https://ya.ru/");
        logger.info("=== Начало теста: Поиск гладиолусов ===");
        try {
            logger.info("Шаг 1: Открываем главную страницу Яндекса");
            yandexMainPage.open();

            logger.info("Шаг 2: Выполняем поиск 'Гладиолус'");
            yandexMainPage.find("Гладиолус");

            logger.info("Шаг 3: Создаём объект страницы результатов");
            searchResults = new YaSearchResultsPage(chromeDriver);

            logger.info("Шаг 4: Закрываем модальное окно, если оно появилось");
            searchResults.closeCapchaIfPresent();

            logger.info("Шаг 5: Ждём появления результатов поиска");
            searchResults.waitForSearchResults();

            logger.info("Шаг 6: Прокручиваем к ссылке на Ozon");
            searchResults.scrollToOzonGladiolusLink();

            logger.info("Шаг 7: Проверяем наличие ссылки на гладиолусы");
            boolean found = searchResults.isOzonGladiolusLinkPresent();

            logger.info("Результат проверки: {}", found);
            logger.info("=== Тест завершён успешно ===");

        } catch (Exception e) {
            logger.error("Тест упал с ошибкой: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testSearchWithCaptchaHandlingChain() {
        // Альтернативный вариант с цепочкой вызовов

        yandexMainPage.open();
        yandexMainPage.find("Гладиолус");

        // Цепочка вызовов: закрыть окно → подождать → прокрутить → проверить
        new YaSearchResultsPage(chromeDriver)
                .closeCapchaIfPresent()
                .waitForSearchResults()
                .scrollToOzonGladiolusLink();

        // Проверка отдельно
        boolean found = new YaSearchResultsPage(chromeDriver).isOzonGladiolusLinkPresent();
    }

    @Test
    public void LariIsLessThanManat() {
        try {
            // 1. Выполняем поиск
            yandexMainPage.open();
            yandexMainPage.find("курс доллара");

            // 2. Работаем с результатами
            searchResults = new YaSearchResultsPage(chromeDriver);
            searchResults.closeCapchaIfPresent();
            searchResults.waitForSearchResults();

            // 3. Кликаем и получаем новую страницу
            CbrCurrencyPage currencyPage = searchResults.clickCurrencyLink();

            // Ждём загрузки новой страницы
             currencyPage.waitForPageLoad();

            // 4. Проверяем переход
            System.out.println("Текущий URL: " + chromeDriver.getCurrentUrl());
            Assertions.assertTrue(
                   currencyPage.isPageLoaded(),
                    "Не перешли на страницу курсов ЦБ РФ. Текущий URL: " + chromeDriver.getCurrentUrl()
            );

            // 5. Проверяем заголовок
            Assertions.assertTrue(
                    currencyPage.isPageTitleContains("Курсы валют"),
                    "Заголовок страницы не содержит 'Курсы валют'. Текущий заголовок: " + chromeDriver.getTitle()
            );

            // 6. Проверяем таблицу
            Assertions.assertTrue(
                    currencyPage.isCurrencyTablePresent(),
                    "Таблица курсов валют отсутствует на странице"
            );

            // ШАГ 7: Получаем курсы валют
            logger.info("8️⃣  Получаем курсы валют...");
            double lariRate = currencyPage.getCurrencyRate("Лари");
            double manatRate = currencyPage.getCurrencyRate("Азербайджанский манат");

            // ШАГ 8: Выводим курсы для наглядности
            logger.info("📊 Курс Лари: {} ₽", lariRate);
            logger.info("📊 Курс Азербайджанский манат: {} ₽", manatRate);

            // ШАГ 9: Проверяем, что Лари дешевле маната
            logger.info("9️⃣  Проверяем: Лари < Азербайджанский манат?");

            assertTrue(
                    lariRate < manatRate,
                    String.format(
                            "❌ Ошибка: Курс Лари (%.4f) НЕ меньше курса маната (%.4f)",
                            lariRate, manatRate
                    )
            );

            System.out.println("✅ Тест пройден успешно");

        } catch (Exception e) {
            System.err.println("❌ Тест упал с ошибкой: " + e.getMessage());
            throw e; // Пробрасываем исключение для отчёта
        }
    }



    @Test
    public void vikiTablesSearch() throws InterruptedException {
        // 1. Выполняем поиск
        yandexMainPage.open();
        yandexMainPage.find("таблица википедия");

        // 2. Работаем с результатами
        searchResults = new YaSearchResultsPage(chromeDriver);
        searchResults.closeCapchaIfPresent();
        searchResults.waitForSearchResults();

        // 3. Ищем по заголовку Википедия:таблицы
        WebElement wiki = searchResults.findResultByTitle("Википедия:Таблицы — Википедия");

        // 4. Кликаем по заголовку Википедия:таблицы
        wiki.click();

    }
}
