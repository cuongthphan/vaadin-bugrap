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
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.*;

public class BugrapIT extends TestBenchTestCase {
    private static final String URL = "http://localhost";
    private static final String PORT = "8080";
    private static final String BASE_URL = URL + ":" + PORT;

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/cuongphanthanh/webdrivers/chromedriver");
        setDriver(new ChromeDriver());
        getDriver().get(URL + ":" + PORT);
    }

    @After
    public void tearDown() {
        getDriver().quit();
    }

    @Test
    public void testSuccessfulLogin() {
        for (Account account : AccountList.getInstance().getList()) {
            setCredentials(account.getUsername(), account.getPassword());
            clickLogin();
            Assert.assertTrue(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
            getDriver().get(BASE_URL);
        }
    }

    @Test
    public void testFailedLogin() {
        setCredentials("cuong@vaadin.com", "08082208");
        clickLogin();
        Assert.assertFalse(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
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

    @Test
    public void testProjectChoosing() {
        getDriver().get(BASE_URL + "/" + ViewNames.MAINAPPVIEW + "/developer");
        Assert.assertTrue($(LabelElement.class).id("username-label").getText().equals("developer"));

        ComboBoxElement comboBox = $(ComboBoxElement.class).id("project-combobox");
        GridElement grid = $(GridElement.class).first();
        
        for (String str : comboBox.getPopupSuggestions()) {
            if (!str.equals("Project 5")) {
                comboBox.selectByText(str);
                Assert.assertTrue(grid.getRowCount() > 0);
            }
        }
        getDriver().get(BASE_URL);
    }
}
