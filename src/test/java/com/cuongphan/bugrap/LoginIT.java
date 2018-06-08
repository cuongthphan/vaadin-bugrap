package com.cuongphan.bugrap;

import com.cuongphan.bugrap.utils.Account;
import com.cuongphan.bugrap.utils.AccountList;
import com.cuongphan.bugrap.utils.ViewNames;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;

public class LoginIT extends TestBenchTestCase {
    private static final String URL = "http://localhost";
    private static final String PORT = "8080";
    private ChromeDriver driver;
    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/cuongphanthanh/webdrivers/chromedriver");
        setDriver(driver = new ChromeDriver());
        driver.get(URL + ":" + PORT);
    }

    @After
    public void tearDown() {
        driver.quit();
    }

    @Test
    public void testSuccessfulLogin() {
        for (Account account : AccountList.getInstance().getList()) {
            setCredentials(account.getUsername(), account.getPassword());
            clickLogin();
            Assert.assertTrue(driver.getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
            driver.get(URL + ":" + PORT);
        }
    }

    @Test
    public void testFailedLogin() {
        setCredentials("cuong@vaadin.com", "08082208");
        clickLogin();
        Assert.assertFalse(driver.getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
        Assert.assertTrue($(LabelElement.class).first().getText().equals("Wrong email or password"));
    }

    @Test
    public void testInvalidEmail() {
        setCredentials("cuong@vaadin", "08082208");
        clickLogin();
        Assert.assertTrue($(LabelElement.class).first().getText().equals("Invalid email"));
    }

    public void setCredentials(String email, String password) {
        $(TextFieldElement.class).caption("Email").first().setValue(email);
        $(PasswordFieldElement.class).caption("Password").first().setValue(password);
    }

    public void clickLogin() {
        $(ButtonElement.class).caption("Login").first().click();
    }
}
