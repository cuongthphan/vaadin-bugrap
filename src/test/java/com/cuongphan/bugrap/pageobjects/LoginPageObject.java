package com.cuongphan.bugrap.pageobjects;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.PasswordFieldElement;
import com.vaadin.testbench.elements.TextFieldElement;
import org.openqa.selenium.WebDriver;

public class LoginPageObject extends TestBenchTestCase {
    private static final String URL = "http://localhost";
    private static final String PORT = "8080";
    private static final String BASE_URL = URL + ":" + PORT;

    public LoginPageObject(WebDriver driver) {
        setDriver(driver);
    }

    public void navigateTo() {
        getDriver().get(BASE_URL);
    }

    public void enterEmail(String email) {
        $(TextFieldElement.class).id("email-textfield").setValue(email);
    }

    public void enterPassword(String password) {
        $(PasswordFieldElement.class).id("passwordfield").setValue(password);
    }

    public void clickLoginButton() {
        $(ButtonElement.class).id("login-button").click();
    }
}
