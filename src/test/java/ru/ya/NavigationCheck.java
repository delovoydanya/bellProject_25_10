package ru.ya;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.WebDriver;

public class NavigationCheck {

    public static void assertOnYandexMainPage(WebDriver driver) {
        String expectedUrl = "https://ya.ru/";
        String actualUrl = driver.getCurrentUrl();

        Assertions.assertTrue(
                driver.getCurrentUrl().startsWith("https://ya.ru/"),
                "Страница не является ya.ru. Текущий URL: " + driver.getCurrentUrl()
        );
    }
}
