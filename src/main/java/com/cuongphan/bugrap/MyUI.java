package com.cuongphan.bugrap;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import com.vaadin.ui.MenuBar;
import org.vaadin.addons.searchbox.SearchBox;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@StyleSheet({"https://fonts.googleapis.com/css?family=Roboto"})
public class MyUI extends UI {

    private BugrapRepository bugrapRepository;
    private BugrapDesign layout;
    private Set<String> checkedItems = new HashSet<>();
    private String focusItemStyle = "focus-item";
    private MenuBar.MenuItem everyoneItem;
    private MenuBar.MenuItem onlyMeItem;
    private MenuBar.MenuItem openSubItem;
    private MenuBar.MenuItem fixedSubItem;
    private MenuBar.MenuItem openItem;
    private MenuBar.MenuItem allKindsItem;
    private MenuBar.MenuItem invalidSubItem;
    private MenuBar.MenuItem wontFixSubItem;
    private MenuBar.MenuItem cantFixSubItem;
    private MenuBar.MenuItem duplicateSubItem;
    private MenuBar.MenuItem worksForMeSubItem;
    private MenuBar.MenuItem needsMoreInfoSubItem;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        layout = new BugrapDesign();

        //add search box
        SearchBox searchBox = new SearchBox(VaadinIcons.SEARCH, SearchBox.ButtonPosition.LEFT);
        searchBox.setButtonJoined(true);
        searchBox.getSearchField().setPlaceholder("Search reports...");
        searchBox.setSuggestionListSize(5);
        searchBox.setWidth(100, Unit.PERCENTAGE);

        layout.searchBoxLayout.addComponent(searchBox);

        //add context menu to custom button
        /*ContextMenu contextMenu = new ContextMenu(layout.customButton, true);
        ContextMenu.Command command = new ContextMenu.Command() {
            public void menuSelected(MenuItem item) {
                if (item.isChecked()) {
                    checkedItems.add(item.getText());
                } else {
                    checkedItems.remove(item.getText());
                }
            }
        };
        MenuItem openItem = contextMenu.addItem("Open", null, command);
        openItem.setCheckable(true);
        contextMenu.addSeparator();
        MenuItem fixedItem = contextMenu.addItem("Fixed", null, command);
        fixedItem.setCheckable(true);
        MenuItem invalidItem = contextMenu.addItem("Invalid", null, command);
        invalidItem.setCheckable(true);
        MenuItem wontFixItem = contextMenu.addItem("Won't fix", null, command);
        wontFixItem.setCheckable(true);
        MenuItem cantFixItem = contextMenu.addItem("Can't fix", null, command);
        cantFixItem.setCheckable(true);
        MenuItem duplicateItem = contextMenu.addItem("Duplicate", null, command);
        duplicateItem.setCheckable(true);
        MenuItem worksForMeItem = contextMenu.addItem("Works for me", null, command);
        worksForMeItem.setCheckable(true);
        MenuItem needMoreInfoItem = contextMenu.addItem("Need more information", null, command);
        needMoreInfoItem.setCheckable(true);
*/
        //set expand ratio for grid columns
        layout.reportGrid.getColumn("version").setExpandRatio(1);
        layout.reportGrid.getColumn("version").setHidden(true);
        layout.reportGrid.getColumn("priority").setExpandRatio(1);
        layout.reportGrid.getColumn("type").setExpandRatio(1);
        layout.reportGrid.getColumn("summary").setExpandRatio(8);
        layout.reportGrid.getColumn("assigned").setExpandRatio(1);
        layout.reportGrid.getColumn("timestamp").setExpandRatio(1);
        layout.reportGrid.getColumn("reportedTimestamp").setExpandRatio(1);

        //get data
        bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
        bugrapRepository.populateWithTestData();
        layout.projectCountLable.setValue(Integer.toString(bugrapRepository.findProjects().size()));
        layout.projectCountLable.setId("projectCountLabel");

        //add project list data provider to project combo box in ascending order by name
        ListDataProvider<Project> projectLDP = new ListDataProvider<>(bugrapRepository.findProjects());
        projectLDP.setSortOrder(project -> project.getName(), SortDirection.ASCENDING);
        layout.projectComboBox.setDataProvider(projectLDP);
        layout.projectComboBox.setEmptySelectionAllowed(false);

        //add version from chosen project to version native select
        //and reset context menu and grid
        layout.projectComboBox.addValueChangeListener(e -> {
            layout.reportGrid.setItems();
            checkedItems.clear();
            /*for (MenuItem item : contextMenu.getItems()) {
                item.setChecked(false);
            }*/
            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(e.getValue()));
            layout.versionNS.setDataProvider(projectVersionLDP);
            if (projectVersionLDP.getItems().size() > 1) {
                layout.versionNS.setEmptySelectionCaption("All versions");
            } else {
                layout.versionNS.setEmptySelectionAllowed(false);
            }

            layout.versionNS.setValue(null);
        });

        layout.versionNS.addValueChangeListener(e -> {
            if (e.isUserOriginated()) {
                if (layout.projectComboBox.getValue() != null)
                    refreshGridData();
            }
        });

        //filter with assignee buttons
        /*layout.onlyMeButton.addClickListener(e -> {
            if (layout.onlyMeButton.getStyleName().equals(focusButtonStyle)) {
                return;
            }

            layout.onlyMeButton.addStyleName(focusButtonStyle);
            layout.everyoneButton.removeStyleName(focusButtonStyle);
            refreshGridData();
        });

        layout.everyoneButton.addClickListener(e -> {
            if (layout.everyoneButton.getStyleName().equals(focusButtonStyle)) {
                return;
            }
            layout.onlyMeButton.removeStyleName(focusButtonStyle);
            layout.everyoneButton.addStyleName(focusButtonStyle);
            refreshGridData();
        });
*/
        //filter with status buttons
        /*layout.openButton.addClickListener(e -> {
            layout.openButton.setEnabled(false);
            layout.allKindsButton.setEnabled(true);
            layout.customButton.set
        });*/

        MenuBar.Command assigneeCommand = new MenuBar.Command() {
            MenuBar.MenuItem previous = null;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (previous != null) {
                    previous.setChecked(false);
                }
                if (previous == selectedItem) {
                    selectedItem.setChecked(false);
                    previous = null;
                } else {
                    selectedItem.setCheckable(true);
                    selectedItem.setChecked(true);
                    previous = selectedItem;
                }
                refreshGridData();
            }
        };

        onlyMeItem = layout.assigneeMB.addItem("Only me", assigneeCommand);
        everyoneItem = layout.assigneeMB.addItem("Everyone", assigneeCommand);

        MenuBar.Command statusCommand = new MenuBar.Command() {
            MenuBar.MenuItem previous = null;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (previous != null) {
                    previous.setChecked(false);
                }
                if (!selectedItem.getText().equals("Custom")) {
                    if (previous == selectedItem) {
                        selectedItem.setChecked(false);
                        previous = null;
                    } else {
                        selectedItem.setCheckable(true);
                        selectedItem.setChecked(true);
                        previous = selectedItem;
                    }
                }
                refreshGridData();
            }
        };

        openItem = layout.statusMB.addItem("Open", statusCommand);
        openItem.setCheckable(true);
        allKindsItem = layout.statusMB.addItem("All kinds", statusCommand);
        allKindsItem.setCheckable(true);
        MenuBar.MenuItem customItem = layout.statusMB.addItem("Custom", null);

        MenuBar.Command customCommand = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (selectedItem.getIcon() == VaadinIcons.CHECK_SQUARE_O) {
                    checkedItems.remove(selectedItem.getText());
                    selectedItem.setIcon(VaadinIcons.THIN_SQUARE);
                } else {
                    checkedItems.add(selectedItem.getText());
                    selectedItem.setIcon(VaadinIcons.CHECK_SQUARE_O);
                }
                for (MenuBar.MenuItem item : layout.statusMB.getItems()) {
                    if (!item.getText().equals("Custom")) {
                        item.setChecked(false);
                    }
                }
                refreshGridData();
            }
        };

        openSubItem = customItem.addItem("Open", VaadinIcons.THIN_SQUARE, customCommand);
        customItem.addSeparator();
        fixedSubItem = customItem.addItem("Fixed", VaadinIcons.THIN_SQUARE, customCommand);
        invalidSubItem = customItem.addItem("Invalid", VaadinIcons.THIN_SQUARE, customCommand);
        wontFixSubItem = customItem.addItem("Won't fix", VaadinIcons.THIN_SQUARE, customCommand);
        cantFixSubItem = customItem.addItem("Can't fix", VaadinIcons.THIN_SQUARE, customCommand);
        duplicateSubItem = customItem.addItem("Duplicate", VaadinIcons.THIN_SQUARE, customCommand);
        worksForMeSubItem = customItem.addItem("Works for me", VaadinIcons.THIN_SQUARE, customCommand);
        needsMoreInfoSubItem = customItem.addItem("Needs more information", VaadinIcons.THIN_SQUARE, customCommand);


        setContent(layout);
    }

    private void refreshGridData() {
        BugrapRepository.ReportsQuery query = new BugrapRepository.ReportsQuery();
        query.project = layout.projectComboBox.getValue();
        if (query.project == null)
            return;
        query.projectVersion = layout.versionNS.getValue();

        Set<Report> reports = bugrapRepository.findReports(query);
        ListDataProvider<Report> reportLDP = new ListDataProvider<>(reports);

        if (query.projectVersion == null) {
            layout.reportGrid.getColumn("version").setHidden(false);
        } else {
            layout.reportGrid.getColumn("version").setHidden(true);
        }
        reportLDP.setSortOrder(report -> report.getPriority(), SortDirection.DESCENDING);

        //filter the reports
        if (onlyMeItem.isChecked()) {
            reportLDP.addFilter(report -> report.getAssigned() != null && report.getAssigned().getName().equals(layout.userName.getValue()));
        }

        if (openItem.isChecked()) {
            reportLDP.addFilter(report -> report.getStatus() != null && report.getStatus().toString().equals("Open"));
        } else if (allKindsItem.isChecked()) {

        } else if (!checkedItems.isEmpty()) {
            reportLDP.addFilter(report -> report.getStatus() != null && checkedItems.contains(report.getStatus().toString()));
        }
        /*if (layout.onlyMeButton.getStyleName().equals(focusItemStyle)) {
            SerializablePredicate<Report> reportSP = new SerializablePredicate<Report>() {
                @Override
                public boolean test(Report report) {
                    if (report.getAssigned() != null)
                        if (report.getAssigned().getName().equals(layout.userName.getValue()))
                            return true;
                    return false;
                }
            };
            reportLDP.setFilter(reportSP);
        }
        */

        layout.reportGrid.setDataProvider(reportLDP);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
