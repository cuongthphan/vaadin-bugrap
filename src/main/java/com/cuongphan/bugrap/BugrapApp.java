package com.cuongphan.bugrap;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Grid;

/**
 * !! DO NOT EDIT THIS FILE !!
 * <p>
 * This class is generated by Vaadin Designer and will be overwritten.
 * <p>
 * Please make a subclass with logic and additional interfaces as needed,
 * e.g class LoginView extends LoginDesign implements View { }
 */
@DesignRoot
@AutoGenerated
@SuppressWarnings("serial")
public class BugrapApp extends VerticalLayout {
    protected Label userName;
    protected Label logoutLabel;
    protected Label bugLabel;
    protected Label requestAFeature;
    protected Label manageLabel;
    protected Label projectCountLable;
    protected HorizontalLayout searchBoxLayout;
    protected HorizontalLayout filterLayout;
    protected Label assigneeLabel;
    protected Label statusLabel;
    protected Grid grid;

    public BugrapApp() {
        Design.read(this);
    }
}
