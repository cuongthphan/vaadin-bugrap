package com.cuongphan.bugrap;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.contextmenu.ContextMenu;
import com.vaadin.contextmenu.MenuItem;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import org.vaadin.addons.searchbox.SearchBox;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.peter.buttongroup.ButtonGroup;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final BugrapApp layout = new BugrapApp();

        //add search box
        SearchBox searchBox = new SearchBox(VaadinIcons.SEARCH, SearchBox.ButtonPosition.LEFT);
        searchBox.setButtonJoined(true);
        searchBox.getSearchField().setPlaceholder("Search reports...");
        searchBox.setSuggestionListSize(5);
        searchBox.addSearchListener(e -> Notification.show(e.getSearchTerm()));
        searchBox.setWidth(100, Unit.PERCENTAGE);

        layout.searchBoxLayout.addComponent(searchBox);

        //add Assignee button group
        ButtonGroup assigneeBG = new ButtonGroup();
        Button onlyMeButton = new Button("Only me");
        onlyMeButton.setId("onlyMeButton");
        assigneeBG.addButton(onlyMeButton);

        Button everyoneButton = new Button("Everyone");
        everyoneButton.setId("everyoneButton");
        assigneeBG.addButton(everyoneButton);

        layout.filterLayout.addComponent(assigneeBG,
                layout.filterLayout.getComponentIndex(layout.assigneeLabel) + 1);

        //add Status button group
        ButtonGroup statusBG = new ButtonGroup();
        Button openButton = new Button("Open");
        openButton.setId("openButton");
        statusBG.addButton(openButton);

        Button allKindsButton = new Button("All kinds");
        allKindsButton.setId("allKindsButton");
        statusBG.addButton(allKindsButton);

        Button customButton = new Button("Custom");
        customButton.setId("customButton");

        //add context menu to custom button
        ContextMenu contextMenu = new ContextMenu(customButton, true);

        MenuItem openItem = contextMenu.addItem("Open", e -> {
            Notification.show("Open: " + e.isChecked());
        });
        openItem.setCheckable(true);
        MenuItem fixedItem = contextMenu.addItem("Fixed", e -> {
            Notification.show("Fixed: " + e.isChecked());
        });
        fixedItem.setCheckable(true);
        MenuItem invalidItem = contextMenu.addItem("Invalid", e -> {
            Notification.show("Invalid: " + e.isChecked());
        });
        invalidItem.setCheckable(true);
        MenuItem wontFixItem = contextMenu.addItem("Won't fix", e -> {
            Notification.show("Won't fix: " + e.isChecked());
        });
        wontFixItem.setCheckable(true);
        MenuItem cantFixItem = contextMenu.addItem("Can't fix", e -> {
            Notification.show("Can't fix: " + e.isChecked());
        });
        cantFixItem.setCheckable(true);
        MenuItem duplicateItem = contextMenu.addItem("Duplicate", e -> {
            Notification.show("Duplicate: " + e.isChecked());
        });
        duplicateItem.setCheckable(true);
        MenuItem worksForMeItem = contextMenu.addItem("Works for me", e -> {
            Notification.show("Works for me: " + e.isChecked());
        });
        worksForMeItem.setCheckable(true);
        MenuItem needMoreInfoItem = contextMenu.addItem("Need more information", e -> {
            Notification.show("Need more information: " + e.isChecked());
        });
        needMoreInfoItem.setCheckable(true);

        statusBG.addButton(customButton);

        layout.filterLayout.addComponent(statusBG,
                layout.filterLayout.getComponentIndex(layout.statusLabel) + 1);

        //set expand ratio for grid columns
        layout.grid.getColumn("priority").setExpandRatio(1);
        layout.grid.getColumn("type").setExpandRatio(1);
        layout.grid.getColumn("summary").setExpandRatio(8);
        layout.grid.getColumn("assigned-to").setExpandRatio(2);
        layout.grid.getColumn("last-modified").setExpandRatio(1);
        layout.grid.getColumn("reported").setExpandRatio(1);

        setContent(layout);

        BugrapRepository bugrapRepository = new BugrapRepository(".");


    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
