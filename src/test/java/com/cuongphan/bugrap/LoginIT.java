package com.cuongphan.bugrap;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.GridElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;

public class LoginIT extends TestBenchTestCase {
    private static final String URL = "http://localhost";
    private static final String PORT = "8080";
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
    public void test() {
        $(ButtonElement.class).caption("Login").first().click();
        Assert.assertTrue($(GridElement.class).exists());
        
    }
}
