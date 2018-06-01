package com.cuongphan.bugrap;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class CommentComponent extends com.vaadin.ui.CustomComponent {
    public HorizontalLayout commentLayout;
    public Label authorIconLabel;
    public VerticalLayout commentDetailLayout;
    public Label authorNameLabel;
    public Label commentDetail;

    public CommentComponent() {
        commentLayout = new HorizontalLayout();
        commentLayout.setWidth(100, Unit.PERCENTAGE);

        authorIconLabel = new Label();
        authorIconLabel.setValue(null);
        authorIconLabel.setIcon(VaadinIcons.USER);
        commentLayout.addComponent(authorIconLabel);

        commentDetailLayout = new VerticalLayout();
        commentDetailLayout.addStyleName("report-detail-layout");
        commentDetailLayout.setWidth(100, Unit.PERCENTAGE);
        commentLayout.addComponent(commentDetailLayout);
        commentLayout.setExpandRatio(commentDetailLayout, 1);

        authorNameLabel = new Label();
        authorNameLabel.setValue("Author name here");
        commentDetailLayout.addComponent(authorNameLabel);

        commentDetail = new Label();
        commentDetail.setValue("This is where the comment is");
        commentDetailLayout.addComponentsAndExpand(commentDetail);
    }
}
