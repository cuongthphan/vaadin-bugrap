package com.cuongphan.bugrap.pageobjects;

import com.cuongphan.bugrap.LoginView;
import com.cuongphan.bugrap.utils.Account;
import com.cuongphan.bugrap.utils.AccountList;
import com.vaadin.testbench.TestBenchTestCase;
import org.openqa.selenium.WebDriver;

public class MainPageObject extends TestBenchTestCase {

    public MainPageObject(WebDriver driver) {
        setDriver(driver);
    }

    public void navigateTo() {
        LoginPageObject loginPage = new LoginPageObject(getDriver());
        loginPage.navigateTo();

        Account account = AccountList.getInstance().getList().get(1);

        loginPage.enterEmail    (account.getEmail());
        loginPage.enterPassword (account.getPassword());

        loginPage.clickLoginButton();
    }
}
