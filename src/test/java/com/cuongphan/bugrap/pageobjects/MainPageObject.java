package com.cuongphan.bugrap.pageobjects;

import com.cuongphan.bugrap.utils.Account;
import com.cuongphan.bugrap.utils.AccountList;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.NativeSelectElement;
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

    public void openReportWindow() {
        ComboBoxElement projectComboBox = $(ComboBoxElement.class)      .id("project-combobox");
        NativeSelectElement versionNS       = $(NativeSelectElement.class)  .id("version-ns");
        GridElement reportGrid      = $(GridElement.class)          .id("report-grid");

        projectComboBox .selectByText("Project 1");
        versionNS       .selectByText("Version 1");

        reportGrid.getCell(0, 0).click();
        $(ButtonElement.class).id("report-link-button").click();
    }
}
