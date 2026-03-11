package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class YaBeforeSearch {
    private static final Logger logger = LoggerFactory.getLogger(YaBeforeSearch.class);

    // WebDriver для работы с браузером
    // Почему поле класса: чтобы все методы класса имели доступ к драйверу
    // Почему защищённое (protected): чтобы наследники могли использовать
    protected WebDriver chromeDriver;

    // Явное ожидание для работы с динамическими элементами
    // ИСПРАВЛЕНО: был тип WebDriver, должен быть WebDriverWait
    // Почему нужно: страница может загружаться, элементы появляться асинхронно
    // Без ожиданий тесты будут падать на медленных страницах
    protected WebDriverWait wait;

    // Локатор поля поиска - используем аннотацию @FindBy
    // Почему @FindBy: это часть PageFactory, делает код чище и инициализирует элементы автоматически
    // Плюс: если изменится локатор, правим только здесь, не во всём коде
    @FindBy(id = "text")
    protected WebElement searchField;

    // Локатор кнопки поиска
    // ИСПРАВЛЕНО: используем более надёжный локатор
    // Почему @FindBy: для единообразия и автоматической инициализации
    @FindBy(className = "search3__button")
    protected WebElement searchButton;

    /**
     * Конструктор класса
     *
     * Почему конструктор принимает только WebDriver:
     * - WebDriver создаётся один раз на тест
     * - Страница может использоваться несколько раз с разными данными
     * - Параметры поиска передаются в методы, а не в конструктор
     *
     * @param chromeDriver - экземпляр браузера
     */
    public YaBeforeSearch(WebDriver chromeDriver) {
        this.chromeDriver = chromeDriver;

        // Инициализируем все элементы с аннотациями @FindBy
        // Почему обязательно: без этого @FindBy не будет работать!
        // PageFactory автоматически найдёт элементы на странице
        PageFactory.initElements(chromeDriver, this);

        // Создаём объект явного ожидания с таймаутом 10 секунд
        // Почему 10 секунд: достаточно для большинства сценариев
        // Если элемент не появится за 10 секунд - выбросит TimeoutException
        this.wait = new WebDriverWait(chromeDriver, 10);
    }
    /**
     * Метод для открытия главной страницы Яндекса
     *
     * Почему отдельный метод:
     * - Разделение ответственности (открытие ≠ поиск)
     * - Переиспользование в разных тестах
     * - Читаемость тестов: "открыть страницу" вместо "driver.get(...)"
     *
     * @return this - для цепочки вызовов (fluent interface)
     */
    public YaBeforeSearch open() {
        chromeDriver.get("https://ya.ru/");
        return this; // Возвращаем this для цепочки вызовов
    }

    /**
     * Проверка, что страница загрузилась
     *
     * Почему отдельный метод:
     * - Проверки лучше выносить в отдельные методы
     * - Можно использовать в разных местах
     * - Чёткое разделение: "открыть" ≠ "проверить загрузку"
     *
     * @return true, если страница загружена
     */
    public boolean isPageLoaded() {
        // Ждём, пока поле поиска станет видимым на странице
        // Почему именно так: если поле видно - страница точно загрузилась
        // Используем wait.until() вместо простого isDisplayed()
        return wait.until(ExpectedConditions.visibilityOf(searchField)).isDisplayed();
    }

    /**
     * Метод поиска с параметризацией
     *
     * Почему параметр метода, а не конструктора:
     * - Конструктор вызывается один раз при создании объекта
     * - Метод можно вызывать много раз с разными параметрами
     * - Соответствует реальному поведению: пользователь может искать много раз
     *
     * @param word - слово для поиска (параметр метода)
     * @return this - для цепочки вызовов
     */
    public YaBeforeSearch find(String word) {
        // Ждём, пока поле поиска станет кликабельным
        // Почему ожидание: элемент может быть не готов к взаимодействию сразу
        // Без этого тест может упасть, если страница загружается медленно
        wait.until(ExpectedConditions.elementToBeClickable(searchField));

        // Очищаем поле на случай, если там что-то есть
        // Почему важно: если предыдущий тест оставил текст, новый поиск будет некорректным
        searchField.clear();

        // Вводим текст из параметра
        // Почему параметр: word - это то, что передадим при вызове метода
        searchField.sendKeys(word);

        // Ждём, пока кнопка поиска станет кликабельной
        wait.until(ExpectedConditions.elementToBeClickable(searchButton));

        // Кликаем по кнопке поиска
        searchButton.click();

        return this; // Возвращаем this для цепочки вызовов
    }

    /**
     * Альтернативный метод поиска через нажатие Enter
     *
     * Почему отдельный метод:
     * - Не все сайты имеют кнопку поиска
     * - Иногда удобнее нажать Enter
     * - Гибкость: можно выбрать способ отправки поиска
     *
     * @param word - слово для поиска
     * @return this - для цепочки вызовов
     */
    public YaBeforeSearch findWithEnter(String word) {
        wait.until(ExpectedConditions.elementToBeClickable(searchField));
        searchField.clear();
        searchField.sendKeys(word);

        // Нажимаем клавишу Enter вместо клика по кнопке
        searchField.sendKeys(Keys.ENTER);

        return this;
    }

    /**
     * Метод только для ввода текста (без отправки поиска)
     *
     * Почему может понадобиться:
     * - Для сложных сценариев, где нужно ввести текст, но не отправлять сразу
     * - Для проверки автодополнения (suggestions)
     * - Для тестирования валидации ввода
     *
     * @param word - слово для ввода
     * @return this - для цепочки вызовов
     */
    public YaBeforeSearch enterSearchTerm(String word) {
        wait.until(ExpectedConditions.elementToBeClickable(searchField));
        searchField.clear();
        searchField.sendKeys(word);
        return this;
    }

    /**
     * Метод для получения текущего значения поля поиска
     *
     * Почему полезно:
     * - Можно проверить, что текст действительно введён
     * - Для отладки тестов
     * - Для проверки автодополнения
     *
     * @return текст из поля поиска
     */
    public String getSearchFieldValue() {
        return searchField.getAttribute("value");
    }

    /**
     * Метод для получения видимости поля поиска
     *
     * Почему полезно:
     * - Проверка, что элемент действительно отображается
     * - Отладка проблем с локаторами
     *
     * @return true, если поле видимо
     */
    public boolean isSearchFieldVisible() {
        return searchField.isDisplayed();
    }
}
