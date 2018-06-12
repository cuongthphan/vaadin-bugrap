package com.cuongphan.bugrap;

import com.cuongphan.bugrap.pageobjects.LoginPageObject;
import com.cuongphan.bugrap.pageobjects.MainPageObject;
import com.cuongphan.bugrap.utils.*;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.LabelElement;
import com.vaadin.testbench.elements.NativeSelectElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.vaadin.bugrap.domain.BugrapRepository;

public class BugrapIT extends TestBenchTestCase {
    private static final String URL = "http://localhost";
    private static final String PORT = "8080";
    private static final String BASE_URL = URL + ":" + PORT;

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "/Users/cuongphanthanh/webdrivers/chromedriver");
        setDriver(new ChromeDriver());
    }

    @After
    public void tearDown() {
        getDriver().quit();
    }

    @Test
    public void testSuccessfulLogin() {
        for (Account account : AccountList.getInstance().getList()) {
            LoginPageObject loginPage = new LoginPageObject(getDriver());
            loginPage.navigateTo();
            loginPage.enterEmail    (account.getEmail());
            loginPage.enterPassword (account.getPassword());
            loginPage.clickLoginButton();
            Assert.assertTrue(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
        }
    }

    @Test
    public void testFailedLogin() {
        LoginPageObject loginPage = new LoginPageObject(getDriver());
        loginPage.navigateTo();

        loginPage.enterEmail("cuong@vaadin.com");
        loginPage.enterPassword("08082208");
        loginPage.clickLoginButton();

        Assert.assertFalse(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
        Assert.assertTrue($(LabelElement.class).id("error-label").getText().equals("Wrong email or password"));
    }

    @Test
    public void testInvalidEmail() {
        LoginPageObject loginPage = new LoginPageObject(getDriver());
        loginPage.navigateTo();

        loginPage.enterEmail("cuong@vaadin");
        loginPage.enterPassword("08082208");
        loginPage.clickLoginButton();

        Assert.assertFalse(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));
        Assert.assertTrue($(LabelElement.class).id("error-label").getText().equals("Invalid email"));
    }

    @Test
    public void testProjectChoosing() {
        MainPageObject mainPage = new MainPageObject(getDriver());
        mainPage.navigateTo();

        //check if view is correct
        Assert.assertTrue(getDriver().getCurrentUrl().contains(ViewNames.MAINAPPVIEW));

        //check if username label is correct
        Assert.assertEquals(
                $(LabelElement.class).id("username-label").getText(),
                AccountList.getInstance().getList().get(1).getUsername()
        );

        ComboBoxElement comboBox = $(ComboBoxElement.class).id("project-combobox");
        GridElement grid = $(GridElement.class).id("report-grid");

        //check number of reports of each project
        for (String str : comboBox.getPopupSuggestions()) {
            comboBox.selectByText(str);

            //check that version native select is not empty
            Assert.assertFalse($(NativeSelectElement.class).id("version-ns").getOptions().isEmpty());

            switch (str) {
                case "Project 1":
                    Assert.assertEquals(grid.getRowCount(), 30);
                    break;
                case "Project 2":
                    Assert.assertEquals(grid.getRowCount(), 38);
                    break;
                case "Project 3":
                    Assert.assertEquals(grid.getRowCount(), 34);
                    break;
                case "Project 4":
                    Assert.assertEquals(grid.getRowCount(), 38);
                    break;
                case "Project 5":
                    Assert.assertEquals(grid.getRowCount(), 0);
                    break;
                default:
                    break;
            }
        }
    }

    @Test
    public void testVersionFiltering() {
        MainPageObject mainPage = new MainPageObject(getDriver());
        mainPage.navigateTo();

        ComboBoxElement     comboBox    = $(ComboBoxElement.class)      .id("project-combobox");
        NativeSelectElement versionNS   = $(NativeSelectElement.class)  .id("version-ns");
        GridElement grid = $(GridElement.class).id("report-grid");

        //check number of reports of each version of every project
        for (String strPrj : comboBox.getPopupSuggestions()) {
            comboBox.selectByText(strPrj);

            //check that VERSION column is visible
            Assert.assertEquals(grid.getHeaderCell(0, 1).getText(), "VERSION");

            for (TestBenchElement element : versionNS.getOptions()) {
                versionNS.selectByText(element.getText());
                if (!element.getText().equals("All versions")) {
                    if (grid.getHeaderCell(0 , 0) != null) {

                        //check that VERSION column is hidden
                        Assert.assertNotEquals(grid.getHeaderCell(0, 1).getText(), "VERSION");
                    }
                }
                switch (strPrj + element.getText()) {
                    case "Project 1" + "Version 1":
                        Assert.assertEquals(grid.getRowCount(), 17);
                        break;
                    case "Project 1" + "Version 2":
                        Assert.assertEquals(grid.getRowCount(), 3);
                        break;
                    case "Project 1" + "Version 3":
                        Assert.assertEquals(grid.getRowCount(), 0);
                        break;
                    case "Project 1" + "All versions":
                        Assert.assertEquals(grid.getRowCount(), 30);
                        break;
                    case "Project 2" + "Version 1":
                        Assert.assertEquals(grid.getRowCount(), 0);
                        break;
                    case "Project 2" + "Version 2":
                        Assert.assertEquals(grid.getRowCount(), 16);
                        break;
                    case "Project 2" + "Version 3":
                        Assert.assertEquals(grid.getRowCount(), 1);
                        break;
                    case "Project 2" + "All versions":
                        Assert.assertEquals(grid.getRowCount(), 38);
                        break;
                    case "Project 3" + "Version 1":
                        Assert.assertEquals(grid.getRowCount(), 14);
                        break;
                    case "Project 3" + "Version 2":
                        Assert.assertEquals(grid.getRowCount(), 3);
                        break;
                    case "Project 3" + "Version 3":
                        Assert.assertEquals(grid.getRowCount(), 1);
                        break;
                    case "Project 3" + "All versions":
                        Assert.assertEquals(grid.getRowCount(), 34);
                        break;
                    case "Project 4" + "Version 1":
                        Assert.assertEquals(grid.getRowCount(), 2);
                        break;
                    case "Project 4" + "Version 2":
                        Assert.assertEquals(grid.getRowCount(), 13);
                        break;
                    case "Project 4" + "Version 3":
                        Assert.assertEquals(grid.getRowCount(), 0);
                        break;
                    case "Project 4" + "All versions":
                        Assert.assertEquals(grid.getRowCount(), 38);
                        break;
                    case "Project 5" + "Version 1":
                    case "Project 5" + "Version 2":
                    case "Project 5" + "Version 3":
                    case "Project 5" + "All versions":
                        Assert.assertEquals(grid.getRowCount(), 0);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
