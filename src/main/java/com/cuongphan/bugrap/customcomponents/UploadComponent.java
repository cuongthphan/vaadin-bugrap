package com.cuongphan.bugrap.customcomponents;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;

import java.io.File;

public class UploadComponent extends CustomComponent {
    public HorizontalLayout uploadComponentLayout;
    public Label attachmentNameLabel;
    public ProgressBar attachmentProgressBar;
    public Button attachmentCancelButton;
    public File file;

    public UploadComponent() {
        uploadComponentLayout = new HorizontalLayout();
        attachmentNameLabel = new Label("Attachment name");
        attachmentProgressBar = new ProgressBar(0.0f);
        attachmentCancelButton = new Button(VaadinIcons.CLOSE_SMALL);
        attachmentCancelButton.addStyleName("attachment-cancel-button");
        uploadComponentLayout.addComponent(attachmentNameLabel);
        uploadComponentLayout.setComponentAlignment(attachmentNameLabel, Alignment.MIDDLE_LEFT);
        uploadComponentLayout.addComponent(attachmentProgressBar);
        uploadComponentLayout.setComponentAlignment(attachmentProgressBar, Alignment.MIDDLE_LEFT);
        uploadComponentLayout.addComponent(attachmentCancelButton);
        uploadComponentLayout.setComponentAlignment(attachmentCancelButton, Alignment.MIDDLE_LEFT);

        setWidth("-1px");

        setCompositionRoot(uploadComponentLayout);
    }
}
