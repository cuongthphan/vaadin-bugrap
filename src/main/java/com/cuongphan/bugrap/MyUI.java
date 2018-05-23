package com.cuongphan.bugrap;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalSplitPanel;
import org.vaadin.addons.searchbox.SearchBox;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import javax.servlet.annotation.WebServlet;
import java.util.HashSet;
import java.util.Optional;
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
    private MainView topLayout;
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
    private SearchBox searchBox;
    private VerticalSplitPanel mainLayout;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        mainLayout = new VerticalSplitPanel();

        topLayout = new MainView();

        //add search box
        searchBox = new SearchBox(VaadinIcons.SEARCH, SearchBox.ButtonPosition.LEFT);
        searchBox.setButtonJoined(true);
        searchBox.getSearchField().setPlaceholder("Search reports...");
        searchBox.setSuggestionListSize(5);
        searchBox.setWidth(100, Unit.PERCENTAGE);

        searchBox.addSearchListener(event -> refreshGridData());
        searchBox.setSearchMode(SearchBox.SearchMode.DEBOUNCE);
        searchBox.setDebounceTime(200);

        topLayout.searchBoxLayout.addComponent(searchBox);

        //set expand ratio for grid columns
        topLayout.reportGrid.getColumn("version").setExpandRatio(1);
        topLayout.reportGrid.getColumn("version").setHidden(true);
        topLayout.reportGrid.getColumn("priority").setExpandRatio(1);
        topLayout.reportGrid.getColumn("type").setExpandRatio(1);
        topLayout.reportGrid.getColumn("summary").setExpandRatio(8);
        topLayout.reportGrid.getColumn("assigned").setExpandRatio(1);
        topLayout.reportGrid.getColumn("timestamp").setExpandRatio(1);
        topLayout.reportGrid.getColumn("reportedTimestamp").setExpandRatio(1);

        //get data
        bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
        bugrapRepository.populateWithTestData();
        topLayout.projectCountLable.setValue(Integer.toString(bugrapRepository.findProjects().size()));
        topLayout.projectCountLable.setId("projectCountLabel");

        //add project list data provider to project combo box in ascending order by name
        ListDataProvider<Project> projectLDP = new ListDataProvider<>(bugrapRepository.findProjects());
        projectLDP.setSortOrder(project -> project.getName(), SortDirection.ASCENDING);
        topLayout.projectComboBox.setDataProvider(projectLDP);
        topLayout.projectComboBox.setEmptySelectionAllowed(false);

        //add version from chosen project to version native select
        //and reset context menu and grid
        topLayout.projectComboBox.addValueChangeListener(e -> {
            topLayout.reportGrid.setItems();
            checkedItems.clear();

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(e.getValue()));

            if (projectVersionLDP.getItems().size() != 1) {
                topLayout.versionNS.setDataProvider(projectVersionLDP);
                topLayout.versionNS.setEmptySelectionCaption("All versions");
            } else {
                topLayout.versionNS.setEmptySelectionAllowed(false);
                for (ProjectVersion pv : projectVersionLDP.getItems()) {
                    topLayout.versionNS.setData(pv);
                }
            }

            topLayout.versionNS.setValue(null);
            refreshGridData();
        });

        topLayout.versionNS.addValueChangeListener(e -> {
            if (e.isUserOriginated()) {
                if (topLayout.projectComboBox.getValue() != null)
                    refreshGridData();
            }
        });

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

        onlyMeItem = topLayout.assigneeMB.addItem("Only me", assigneeCommand);
        everyoneItem = topLayout.assigneeMB.addItem("Everyone", assigneeCommand);

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

        openItem = topLayout.statusMB.addItem("Open", statusCommand);
        openItem.setCheckable(true);
        allKindsItem = topLayout.statusMB.addItem("All kinds", statusCommand);
        allKindsItem.setCheckable(true);
        MenuBar.MenuItem customItem = topLayout.statusMB.addItem("Custom", null);

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
                for (MenuBar.MenuItem item : topLayout.statusMB.getItems()) {
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

        topLayout.reportGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        topLayout.reportGrid.addSelectionListener(event -> {
            if (topLayout.reportGrid.getSelectedItems().size() != 0) {
                ReportView bottomLayout = new ReportView();
                int selectedAmount = topLayout.reportGrid.getSelectedItems().size();

                event.getFirstSelectedItem().ifPresent(report -> {
                    bottomLayout.reportNameLabel.setValue(report.getSummary());

                    ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(report.getProject()));
                    bottomLayout.versionNS.setDataProvider(projectVersionLDP);
                    bottomLayout.versionNS.setValue(report.getVersion());

                    Set<Report.Priority> prioritySet = new HashSet<>();
                    for (Report.Priority priority : Report.Priority.values()) {
                        prioritySet.add(priority);
                    }
                    ListDataProvider<Report.Priority> priorityLDP = new ListDataProvider<>(prioritySet);
                    bottomLayout.priorityNS.setDataProvider(priorityLDP);
                    bottomLayout.priorityNS.setValue(report.getPriority());

                    Set<Report.Type> typeSet = new HashSet<>();
                    for (Report.Type type : Report.Type.values()) {
                        typeSet.add(type);
                    }
                    ListDataProvider<Report.Type> typeLDP = new ListDataProvider<>(typeSet);
                    bottomLayout.typeNS.setDataProvider(typeLDP);
                    bottomLayout.typeNS.setValue(report.getType());

                    Set<Report.Status> statusSet = new HashSet<>();
                    for (Report.Status status : Report.Status.values()) {
                        statusSet.add(status);
                    }
                    ListDataProvider<Report.Status> statusLDP = new ListDataProvider<>(statusSet);
                    bottomLayout.statusNS.setDataProvider(statusLDP);
                    bottomLayout.statusNS.setValue(report.getStatus());

                    ListDataProvider<Reporter> reporterLDP = new ListDataProvider<>(bugrapRepository.findReporters());
                    bottomLayout.assignedNS.setDataProvider(reporterLDP);
                    bottomLayout.assignedNS.setValue(report.getAssigned());

                    bottomLayout.reportDetail.setValue(report.getDescription());
                });
                if (selectedAmount == 1) {
                    bottomLayout.versionNS.setEmptySelectionAllowed(false);
                    bottomLayout.priorityNS.setEmptySelectionAllowed(false);
                    bottomLayout.typeNS.setEmptySelectionAllowed(false);

                    mainLayout.setSecondComponent(bottomLayout);
                    mainLayout.setSplitPosition(65, Unit.PERCENTAGE);
                }
                else {
                    bottomLayout.openNewButton.setVisible(false);
                    bottomLayout.reportDetail.setVisible(false);
                    bottomLayout.reportNameLabel.setValue(selectedAmount + " reported selected - Select a single report to view contents");

                    for (Report report : topLayout.reportGrid.getSelectedItems()) {
                        if (bottomLayout.priorityNS.getValue() != report.getPriority()) {
                            bottomLayout.priorityNS.setValue(null);
                        }
                        if (bottomLayout.typeNS.getValue() != report.getType()) {
                            bottomLayout.typeNS.setValue(null);
                        }
                        if (bottomLayout.statusNS.getValue() != report.getStatus()) {
                            bottomLayout.statusNS.setValue(null);
                        }
                        if (bottomLayout.assignedNS.getValue() != report.getAssigned()) {
                            bottomLayout.assignedNS.setValue(null);
                        }
                        if (bottomLayout.versionNS.getValue() != report.getVersion()) {
                            bottomLayout.versionNS.setValue(null);
                        }
                    }
                    mainLayout.setSecondComponent(bottomLayout);
                    mainLayout.setSplitPosition(160, Unit.PIXELS, true);
                }
            }
            else {
                mainLayout.setSecondComponent(null);
                mainLayout.setSplitPosition(100, Unit.PERCENTAGE);
            }


        });

        mainLayout.setFirstComponent(topLayout);
        mainLayout.setSplitPosition(100, Unit.PERCENTAGE);
        setContent(mainLayout);
    }

    private void refreshGridData() {
        BugrapRepository.ReportsQuery query = new BugrapRepository.ReportsQuery();
        query.project = topLayout.projectComboBox.getValue();
        if (query.project == null) {
            return;
        }

        query.projectVersion = topLayout.versionNS.getValue();

        Set<Report> reports = bugrapRepository.findReports(query);
        ListDataProvider<Report> reportLDP = new ListDataProvider<>(reports);

        if (query.projectVersion == null) {
            topLayout.reportGrid.getColumn("version").setHidden(false);
        } else {
            topLayout.reportGrid.getColumn("version").setHidden(true);
        }
        reportLDP.setSortOrder(report -> report.getPriority(), SortDirection.DESCENDING);

        //filter the reports
        if (onlyMeItem.isChecked()) {
            reportLDP.addFilter(report -> report.getAssigned() != null && report.getAssigned().getName().equals(topLayout.userName.getValue()));
        }

        if (openItem.isChecked()) {
            reportLDP.addFilter(report -> report.getStatus() != null && report.getStatus().toString().equals("Open"));
        } else if (allKindsItem.isChecked()) {

        } else if (!checkedItems.isEmpty()) {
            reportLDP.addFilter(report -> report.getStatus() != null && checkedItems.contains(report.getStatus().toString()));
        }

        if (!searchBox.getSearchField().getValue().isEmpty()) {
            reportLDP.addFilter(report -> report.getSummary() != null
                    && report.getSummary().toLowerCase().contains(searchBox.getSearchField().getValue().toLowerCase()));
        }

        topLayout.reportGrid.setDataProvider(reportLDP);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
