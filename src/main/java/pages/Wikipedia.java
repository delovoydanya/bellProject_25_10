package pages;

import net.bytebuddy.dynamic.DynamicType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class Wikipedia {
    private WebDriver driver;
    private WebDriverWait wait;
    private static Logger logger = LoggerFactory.getLogger(CbrCurrencyPage.class);
    private WebElement link;

    public Wikipedia(WebDriver driver, WebDriverWait wait, Logger logger) {
        this.logger = logger;
        this.driver = driver;
        this.wait = wait;
    }

    public Wikipedia(WebElement resultLink) {
        this.link = resultLink;
    }

    public Wikipedia(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 15);
    }

    public List<String> findTable(String titleText) {
        logger.info("🔍 Ищем таблицу: '{}'", titleText);

//        By RESULT_TABLE = By.xpath("//table[contains(@class, 'wikitable')]//caption[contains(text(), '" + titleText + "')]/ancestor::table");
        By RESULT_TABLE = By.xpath("//html//body//div//div//div//div//table[1]");
        WebElement tableElement = wait.until(ExpectedConditions.presenceOfElementLocated(RESULT_TABLE));
        logger.info("✅ Таблица найдена!"); 
        List<WebElement> tableRows = tableElement.findElements(By.cssSelector("tbody tr"));

        List<String> teachers = tableRows.stream()
                .skip(1)
                .map(row -> {
                    List<WebElement> cells = row.findElements(By.tagName("td"));
                    if (cells.size() >= 3) {
                        String surname = cells.get(0).getText().trim();
                        String name = cells.get(1).getText().trim();
                        String patronymic = cells.get(2).getText().trim();
                        return surname + name + patronymic;
                    }
                    return "";
                })
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        logger.info("✅ Таблица найдена!");
        return teachers;
    }

    public boolean isFirstAndLast(String first, String last, Wikipedia wikipedia) {
        List<String> teachers = wikipedia.findTable("Преподаватели кафедры программирования");
        String firstTeacher = teachers.get(0);
        String lastTeacher = teachers.get(teachers.size() - 1);
        logger.info("Первый преподаватель: " + firstTeacher);
        logger.info("Последний преподаватель: " + lastTeacher);

        boolean isFirstCorrect = firstTeacher.contains(first.replaceAll("\\s+", ""));
        boolean isLastCorrect = lastTeacher.contains(last.replaceAll("\\s+", ""));

        if (isFirstCorrect && isLastCorrect) {
            return true;
        } else {
            return false;
        }
    }
}