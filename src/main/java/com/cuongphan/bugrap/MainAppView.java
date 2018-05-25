package com.cuongphan.bugrap;

import com.vaadin.navigator.View;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.VerticalSplitPanel;

public class MainAppView implements View {
    public MainAppView(){
        VerticalSplitPanel splitPanel = new VerticalSplitPanel();

        MainView topView = new MainView();
        ReportView bottomView = new ReportView();
        splitPanel.setFirstComponent(topView);
        splitPanel.setSecondComponent(bottomView);
        splitPanel.setSplitPosition(100, Sizeable.Unit.PERCENTAGE);
    }
}
