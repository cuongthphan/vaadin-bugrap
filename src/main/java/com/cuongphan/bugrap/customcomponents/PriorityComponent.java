package com.cuongphan.bugrap.customcomponents;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import org.vaadin.bugrap.domain.entities.Report;

import java.util.List;

public class PriorityComponent extends CustomComponent {
    private HorizontalLayout layout;
    private int priorityInt;

    public PriorityComponent(Report.Priority priority) {
        switch (priority) {
            case BLOCKER:
                priorityInt = 6;
                break;
            case CRITICAL:
                priorityInt = 5;
                break;
            case MAJOR:
                priorityInt = 4;
                break;
            case NORMAL:
                priorityInt = 3;
                break;
            case MINOR:
                priorityInt = 2;
                break;
            case TRIVIAL:
                priorityInt = 1;
                break;
            default:
                break;
        }

        layout = new HorizontalLayout();
        layout.setStyleName("priority-layout");
        layout.setHeight("-1px");
        layout.setWidth("-1px");

        for (int i = 0; i < priorityInt; i++) {
            HorizontalLayout subLayout = new HorizontalLayout();
            subLayout.setHeight(24, Unit.PIXELS);
            subLayout.setWidth(10, Unit.PIXELS);
            subLayout.addStyleName("priority");
            layout.addComponent(subLayout);
        }

        setWidth("-1px");
        setHeight("-1px");

        setCompositionRoot(layout);
    }
}
