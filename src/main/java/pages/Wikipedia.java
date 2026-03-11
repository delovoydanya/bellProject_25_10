package pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}
