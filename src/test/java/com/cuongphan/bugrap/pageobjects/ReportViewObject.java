package com.cuongphan.bugrap.pageobjects;

import com.vaadin.testbench.TestBenchTestCase;
import org.openqa.selenium.WebDriver;

public class ReportViewObject extends TestBenchTestCase {
    public ReportViewObject(WebDriver driver) {
        setDriver(driver);
    }

    public void navigateTo() {
        MainPageObject mainPageObject = new MainPageObject(getDriver());
        mainPageObject.navigateTo();

        mainPageObject.openReportWindow();
    }
}
