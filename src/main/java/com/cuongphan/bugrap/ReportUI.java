package com.cuongphan.bugrap;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

@Theme("mytheme")
@StyleSheet({"https://fonts.googleapis.com/css?family=Roboto"})
public class ReportUI extends UI {
    private ReportView reportView = new ReportView();

    @Override
    protected void init(VaadinRequest request) {

        setContent(reportView);
    }
}
