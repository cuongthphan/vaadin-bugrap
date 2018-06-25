package com.cuongphan.bugrap.bdd;

import com.cuongphan.bugrap.pageobjects.LoginPageObject;
import com.cuongphan.bugrap.utils.ViewNames;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.LabelElement;
import org.jbehave.core.annotations.*;
import org.junit.Assert;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.PageFactory;

public class LoginSteps extends TestBenchTestCase {

    private LoginPageObject loginPageObject;

    @BeforeScenario
    public void setUpWebDriver() {
        System.setProperty("webdriver.chrome.driver", "/Users/cuongphanthanh/webdrivers/chromedriver");
        setDriver(new ChromeDriver());
        loginPageObject = PageFactory.initElements(getDriver(), LoginPageObject.class);
    }

    @AfterScenario
    public void tearDownWebDriver() {
        getDriver().quit();
    }

    @Given("the Login Page")
    public void givenTheLoginPage() {
        loginPageObject.navigateTo();
    }

    @When("I enter email field with $email and password field with $password")
    public void enter(
            @Named("email") String email,
            @Named("password") String password) {
        loginPageObject.enterEmail(email);
        loginPageObject.enterPassword(password);
        loginPageObject.clickLoginButton();
    }

    @Then("I should get the message as $message")
    public void theMessageShouldBe(@Named("message") String message){
        Assert.assertFalse(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
        Assert.assertTrue($(LabelElement.class).id("error-label").getText().equals(message));
    }

    @Then("I should get to the Main Page")
    public void thePageShouldBeMainPage() {
        Assert.assertTrue(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
    }
}
