package com.cuongphan.bugrap;

import com.cuongphan.bugrap.ui.MainUI;
import com.cuongphan.bugrap.utils.ViewNames;
import com.vaadin.navigator.View;

public class LoginView extends LoginDesign implements View {
    public LoginView() {
        loginButton.addClickListener(event -> {
            ((MainUI)getParent()).navigator.navigateTo(ViewNames.MAINAPPVIEW);
        });
    }
}
