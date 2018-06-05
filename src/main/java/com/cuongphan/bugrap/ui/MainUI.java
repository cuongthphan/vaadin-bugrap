package com.cuongphan.bugrap.ui;

import com.cuongphan.bugrap.utils.Broadcaster;
import com.cuongphan.bugrap.utils.ViewNames;
import com.cuongphan.bugrap.MainAppView;
import com.cuongphan.bugrap.ReportView;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

import javax.servlet.annotation.WebServlet;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@StyleSheet({"https://fonts.googleapis.com/css?family=Roboto"})
@Push
@PushStateNavigation
public class MainUI extends UI {
    public Navigator navigator;
    public MainAppView mainAppView;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        navigator = new Navigator(this, this);

        mainAppView = new MainAppView();
        Broadcaster.register(mainAppView);
        navigator.addView(ViewNames.MAINAPPVIEW, mainAppView);
        navigator.addView(ViewNames.FULLREPORTVIEW, new ReportView());
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MainUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
