package com.cuongphan.bugrap;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.annotations.DesignRoot;
import com.vaadin.ui.declarative.Design;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;

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
public class ReportView extends VerticalLayout {
    protected Button openNewButton;
    protected Label reportNameLabel;
    protected NativeSelect<org.vaadin.bugrap.domain.entities.Report.Priority> priorityNS;
    protected NativeSelect<org.vaadin.bugrap.domain.entities.Report.Type> typeNS;
    protected NativeSelect<org.vaadin.bugrap.domain.entities.Report.Status> statusNS;
    protected NativeSelect<org.vaadin.bugrap.domain.entities.Reporter> assignedNS;
    protected NativeSelect<org.vaadin.bugrap.domain.entities.ProjectVersion> versionNS;
    protected Button updateButton;
    protected Button revertButton;
    protected TextArea reportDetail;

    public ReportView() {
        Design.read(this);
    }
}