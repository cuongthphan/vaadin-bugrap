package com.cuongphan.bugrap;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Grid;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.declarative.Design;
import org.vaadin.addons.searchbox.SearchBox;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;

import java.util.HashSet;
import java.util.Set;

public class MainView extends MainDesign implements View {
    private BugrapRepository bugrapRepository;
    private Set<String> customFilterSet = new HashSet<>();
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
    private ReportView bottomView;

    public MainView() {
        //get data
        bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
        bugrapRepository.populateWithTestData();

        //add a SearchBox to the MainView
        addSearchBoxToMainView();
        //set report grid columns expand ratio
        setGridColumnsExpandRatio();
        //update MainView UI
        updateMainViewUI();
    }

    private void updateMainViewUI() {
        //update projectCountLabel
        projectCountLable.setValue(Integer.toString(bugrapRepository.findProjects().size()));

        //display project in ascending order by name
        ListDataProvider<Project> projectLDP = new ListDataProvider<>(bugrapRepository.findProjects());
        projectLDP.setSortOrder(project -> project.getName(), SortDirection.ASCENDING);
        projectComboBox.setDataProvider(projectLDP);
        projectComboBox.setEmptySelectionAllowed(false);

        //add versions to version native select
        //and reset context menu and grid when a project is selected
        projectComboBox.addValueChangeListener(e -> {
            reportGrid.setItems();
            customFilterSet.clear();
            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(e.getValue()));

            if (projectVersionLDP.getItems().size() != 1) {
                versionNS.setDataProvider(projectVersionLDP);
                versionNS.setEmptySelectionCaption("All versions");
            } else {
                versionNS.setEmptySelectionAllowed(false);
                for (ProjectVersion pv : projectVersionLDP.getItems()) {
                    versionNS.setData(pv);
                }
            }
            versionNS.setValue(null);
            refreshGridData();
        });

        versionNS.addValueChangeListener(e -> {
            if (projectComboBox.getValue() != null) {
                refreshGridData();
            }
        });

        //filter reports with assignee buttons
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
        onlyMeItem = assigneeMB.addItem("Only me", assigneeCommand);
        everyoneItem = assigneeMB.addItem("Everyone", assigneeCommand);

        //filter reports with status buttons
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

        openItem = statusMB.addItem("Open", statusCommand);
        openItem.setCheckable(true);
        allKindsItem = statusMB.addItem("All kinds", statusCommand);
        allKindsItem.setCheckable(true);
        MenuBar.MenuItem customItem = statusMB.addItem("Custom", null);

        //add sub-menu item to Custom button
        MenuBar.Command customCommand = new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (selectedItem.getIcon() == VaadinIcons.CHECK_SQUARE_O) {
                    customFilterSet.remove(selectedItem.getText());
                    selectedItem.setIcon(VaadinIcons.THIN_SQUARE);
                } else {
                    customFilterSet.add(selectedItem.getText());
                    selectedItem.setIcon(VaadinIcons.CHECK_SQUARE_O);
                }
                for (MenuBar.MenuItem item : statusMB.getItems()) {
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

        reportGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        reportGrid.addSelectionListener(event -> {
            if (reportGrid.getSelectedItems().size() != 0) {
                bottomView = new ReportView();
                bottomView.breadcrumbsLayout.setVisible(false);
                bottomView.attachmentLayout.setVisible(false);

                //display second view if 1 report is selected
                if (reportGrid.getSelectedItems().size() == 1) {
                    bottomView.versionNS.setEmptySelectionAllowed(false);
                    bottomView.priorityNS.setEmptySelectionAllowed(false);
                    bottomView.typeNS.setEmptySelectionAllowed(false);
                    bottomView.openNewButton.setVisible(true);
                    bottomView.reportDetail.setVisible(true);

                    mainLayout.setSecondComponent(bottomView);
                    mainLayout.setSplitPosition(60, Unit.PERCENTAGE);
                    mainLayout.setLocked(false);
                }
                // if more than 1 reported chosen
                else {
                    bottomView.openNewButton.setVisible(false);
                    bottomView.reportDetail.setVisible(false);
                    bottomView.reportNameLabel.setValue(reportGrid.getSelectedItems().size() +
                            " reported selected - Select a single report to view contents");

                    for (Report report : reportGrid.getSelectedItems()) {
                        if (bottomView.priorityNS.getValue() != report.getPriority()) {
                            bottomView.priorityNS.setValue(null);
                        }
                        if (bottomView.typeNS.getValue() != report.getType()) {
                            bottomView.typeNS.setValue(null);
                        }
                        if (bottomView.statusNS.getValue() != report.getStatus()) {
                            bottomView.statusNS.setValue(null);
                        }
                        if (bottomView.assignedNS.getValue() != report.getAssigned()) {
                            bottomView.assignedNS.setValue(null);
                        }
                        if (bottomView.versionNS.getValue() != report.getVersion()) {
                            bottomView.versionNS.setValue(null);
                        }
                        if (bottomView.reportDetail.getValue() != report.getDescription()) {
                            bottomView.reportDetail.setValue("");
                        }
                    }
                    mainLayout.setSecondComponent(bottomView);
                    mainLayout.setSplitPosition(110, Unit.PIXELS, true);
                    mainLayout.setLocked(true);
                }
                //refreshBottomView();
            }
            else {
                mainLayout.setSecondComponent(null);
                mainLayout.setSplitPosition(100, Unit.PERCENTAGE);
            }
        });
    }

    private void addSearchBoxToMainView() {
        searchBox = new SearchBox(VaadinIcons.SEARCH, SearchBox.ButtonPosition.LEFT);
        searchBox.setButtonJoined(true);
        searchBox.getSearchField().setPlaceholder("Search reports...");
        searchBox.setSuggestionListSize(5);
        searchBox.setWidth(100, Unit.PERCENTAGE);

        //searchBox.addSearchListener(event -> refreshGridData());
        searchBox.setSearchMode(SearchBox.SearchMode.DEBOUNCE);
        searchBox.setDebounceTime(200);

        searchBoxLayout.addComponent(searchBox);
    }

    private void setGridColumnsExpandRatio() {
        reportGrid.getColumn("version").setExpandRatio(1);
        reportGrid.getColumn("version").setHidden(true);
        reportGrid.getColumn("priority").setExpandRatio(1);
        reportGrid.getColumn("type").setExpandRatio(1);
        reportGrid.getColumn("summary").setExpandRatio(8);
        reportGrid.getColumn("assigned").setExpandRatio(1);
        reportGrid.getColumn("timestamp").setExpandRatio(1);
        reportGrid.getColumn("reportedTimestamp").setExpandRatio(1);
    }

    private void refreshGridData() {
        BugrapRepository.ReportsQuery query = new BugrapRepository.ReportsQuery();
        query.project = projectComboBox.getValue();
        if (query.project == null) {
            return;
        }

        query.projectVersion = versionNS.getValue();

        Set<Report> reports = bugrapRepository.findReports(query);
        ListDataProvider<Report> reportLDP = new ListDataProvider<>(reports);

        if (query.projectVersion == null) {
            reportGrid.getColumn("version").setHidden(false);
        } else {
            reportGrid.getColumn("version").setHidden(true);
        }
        reportLDP.setSortOrder(report -> report.getPriority(), SortDirection.DESCENDING);

        //filter the reports
        if (onlyMeItem.isChecked()) {
            reportLDP.addFilter(report -> report.getAssigned() != null && report.getAssigned().getName().equals(userName.getValue()));
        }

        if (openItem.isChecked()) {
            reportLDP.addFilter(report -> report.getStatus() != null && report.getStatus().toString().equals("Open"));
        } else if (allKindsItem.isChecked()) {

        } else if (!customFilterSet.isEmpty()) {
            reportLDP.addFilter(report -> report.getStatus() != null && customFilterSet.contains(report.getStatus().toString()));
        }

        if (!searchBox.getSearchField().getValue().isEmpty()) {
            reportLDP.addFilter(report -> report.getSummary() != null
                    && report.getSummary().toLowerCase().contains(searchBox.getSearchField().getValue().toLowerCase()));
        }

        reportGrid.setDataProvider(reportLDP);
    }
}
