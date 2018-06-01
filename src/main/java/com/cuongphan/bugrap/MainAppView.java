package com.cuongphan.bugrap;

import com.cuongphan.bugrap.utils.Broadcaster;
import com.cuongphan.bugrap.ui.MainUI;
import com.cuongphan.bugrap.utils.ReportSingleton;
import com.cuongphan.bugrap.utils.ViewNames;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import org.vaadin.addons.searchbox.SearchBox;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class MainAppView extends VerticalSplitPanel implements View, Broadcaster.BroadcastListener {
    private BugrapRepository bugrapRepository;
    public MainView topView;
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
    private ReportView bottomView;

    public MainAppView() {
        setSizeFull();

        topView = new MainView();

        setFirstComponent(topView);
        setSplitPosition(100, Unit.PERCENTAGE);

        //add search box
        searchBox = new SearchBox(VaadinIcons.SEARCH, SearchBox.ButtonPosition.LEFT);
        searchBox.setButtonJoined(true);
        searchBox.getSearchField().setPlaceholder("Search reports...");
        searchBox.setSuggestionListSize(5);
        searchBox.setWidth(100, Unit.PERCENTAGE);
        searchBox.addSearchListener(event -> refreshGridData());
        searchBox.setSearchMode(SearchBox.SearchMode.DEBOUNCE);
        searchBox.setDebounceTime(200);

        topView.searchBoxLayout.addComponent(searchBox);

        //set expand ratio for grid columns
        topView.reportGrid.getColumn("version").setExpandRatio(1);
        topView.reportGrid.getColumn("version").setHidden(true);
        topView.reportGrid.getColumn("priority").setExpandRatio(1);
        topView.reportGrid.getColumn("type").setExpandRatio(1);
        topView.reportGrid.getColumn("summary").setExpandRatio(8);
        topView.reportGrid.getColumn("assigned").setExpandRatio(1);
        topView.reportGrid.getColumn("timestamp").setExpandRatio(1);
        topView.reportGrid.getColumn("reportedTimestamp").setExpandRatio(1);

        //get data
        bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
        bugrapRepository.populateWithTestData();
        topView.projectCountLable.setValue(Integer.toString(bugrapRepository.findProjects().size()));
        topView.projectCountLable.setId("projectCountLabel");

        //add project list data provider to project combo box in ascending order by name
        ListDataProvider<Project> projectLDP = new ListDataProvider<>(bugrapRepository.findProjects());
        projectLDP.setSortOrder(project -> project.getName(), SortDirection.ASCENDING);
        topView.projectComboBox.setDataProvider(projectLDP);
        topView.projectComboBox.setEmptySelectionAllowed(false);

        //add version from chosen project to version native select
        //and reset context menu and grid
        topView.projectComboBox.addValueChangeListener(e -> {
            topView.reportGrid.setItems();
            checkedItems.clear();

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(e.getValue()));

            if (projectVersionLDP.getItems().size() != 1) {
                topView.versionNS.setDataProvider(projectVersionLDP);
                topView.versionNS.setEmptySelectionCaption("All versions");
            } else {
                topView.versionNS.setEmptySelectionAllowed(false);
                for (ProjectVersion pv : projectVersionLDP.getItems()) {
                    topView.versionNS.setData(pv);
                }
            }

            topView.versionNS.setValue(null);
            refreshGridData();
        });

        topView.versionNS.addValueChangeListener(e -> {
            if (e.isUserOriginated()) {
                if (topView.projectComboBox.getValue() != null)
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

        onlyMeItem = topView.assigneeMB.addItem("Only me", assigneeCommand);
        everyoneItem = topView.assigneeMB.addItem("Everyone", assigneeCommand);

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

        openItem = topView.statusMB.addItem("Open", statusCommand);
        openItem.setCheckable(true);
        allKindsItem = topView.statusMB.addItem("All kinds", statusCommand);
        allKindsItem.setCheckable(true);
        MenuBar.MenuItem customItem = topView.statusMB.addItem("Custom", null);

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
                for (MenuBar.MenuItem item : topView.statusMB.getItems()) {
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

        topView.reportGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        topView.reportGrid.addSelectionListener(event -> {
            if (topView.reportGrid.getSelectedItems().size() != 0) {
                bottomView = new ReportView();

                bottomView.breadcrumbsLayout.setVisible(false);
                bottomView.attachmentLayout.setVisible(false);
                setSecondComponent(bottomView);
                setLocked(false);
                refreshBottomView();

                //display second view if 1 report is selected
                if (topView.reportGrid.getSelectedItems().size() == 1) {
                    bottomView.versionNS.setEmptySelectionAllowed(false);
                    bottomView.priorityNS.setEmptySelectionAllowed(false);
                    bottomView.typeNS.setEmptySelectionAllowed(false);
                    bottomView.openNewButton.setVisible(true);
                    bottomView.reportDetail.setVisible(true);

                    ReportSingleton.getInstance().getReports().clear();
                    for (Report report : topView.reportGrid.getSelectedItems()) {
                        ReportSingleton.getInstance().addReport(report);
                    }

                    setSplitPosition(60, Unit.PERCENTAGE);
                }
                // if more than 1 reported chosen
                else {
                    bottomView.openNewButton.setVisible(false);
                    bottomView.reportDetail.setVisible(false);
                    bottomView.reportNameLabel.setValue(topView.reportGrid.getSelectedItems().size() +
                            " reported selected - Select a single report to view contents");

                    refreshBottomView();
                    setSplitPosition(90, Unit.PIXELS, true);
                }
                addBottomViewListener();
            }
            else {
                setSecondComponent(null);
                setSplitPosition(100, Unit.PERCENTAGE);
            }
        });
    }

    private void addBottomViewListener() {
        BrowserWindowOpener opener = new BrowserWindowOpener(MainUI.class, ViewNames.FULLREPORTVIEW);

        opener.extend(bottomView.openNewButton);
        opener.setWindowName("Report");

        bottomView.openNewButton.addClickListener(event -> {
            for (Report report : topView.reportGrid.getSelectedItems()) {
                ReportSingleton.getInstance().clearReports();
                ReportSingleton.getInstance().addReport(report);
            }
        });

        bottomView.removeUpdateAndRevertListener();

        bottomView.updateButton.addClickListener(event -> {
            for (Report r : topView.reportGrid.getSelectedItems()) {
                Report report = bugrapRepository.getReportById(r.getId());
                if (bottomView.priorityNS.getValue() != null) {
                    r.setPriority(bottomView.priorityNS.getValue());
                    report.setPriority(bottomView.priorityNS.getValue());
                }
                if (bottomView.typeNS.getValue() != null) {
                    r.setType(bottomView.typeNS.getValue());
                    report.setType(bottomView.typeNS.getValue());
                }
                if (bottomView.statusNS.getValue() != null) {
                    r.setStatus(bottomView.statusNS.getValue());
                    report.setStatus(bottomView.statusNS.getValue());
                }
                if (bottomView.assignedNS.getValue() != null) {
                    r.setAssigned(bottomView.assignedNS.getValue());
                    report.setAssigned(bottomView.assignedNS.getValue());
                }
                if (bottomView.versionNS.getValue() != null) {
                    r.setVersion(bottomView.versionNS.getValue());
                    report.setVersion(bottomView.versionNS.getValue());
                }
                if (topView.reportGrid.getSelectedItems().size() == 1) {
                    r.setDescription(bottomView.reportDetail.getValue());
                    report.setDescription(bottomView.reportDetail.getValue());
                }
                bugrapRepository.save(report);
            }
            topView.reportGrid.getDataProvider().refreshAll();
            refreshBottomView();
        });

        bottomView.revertButton.addClickListener(event -> {
            for (Report r : topView.reportGrid.getSelectedItems()) {
                Report origin = bugrapRepository.getReportById(r.getId());
                r.setPriority(origin.getPriority());
                r.setType(origin.getType());
                r.setStatus(origin.getStatus());
                r.setAssigned(origin.getAssigned());
                r.setVersion(origin.getVersion());
                r.setDescription(origin.getDescription());
            }
            topView.reportGrid.getDataProvider().refreshAll();
            refreshBottomView();
        });
    }

    private void refreshBottomView() {
        for (Report report : topView.reportGrid.getSelectedItems()) {
            bottomView.projectLabel.setValue(report.getProject().getName());
            if (report.getVersion() != null) {
                bottomView.versionLabel.setValue(report.getVersion().getVersion());
            }
            else {
                bottomView.versionLabel.setValue(null);
            }

            if (topView.reportGrid.getSelectedItems().size() == 1) {
                bottomView.reportNameLabel.setValue(report.getSummary());
            }

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(report.getProject()));
            bottomView.versionNS.setDataProvider(projectVersionLDP);
            bottomView.versionNS.setValue(report.getVersion());

            Set<Report.Priority> prioritySet = new HashSet<>();
            for (Report.Priority priority : Report.Priority.values()) {
                prioritySet.add(priority);
            }
            ListDataProvider<Report.Priority> priorityLDP = new ListDataProvider<>(prioritySet);
            bottomView.priorityNS.setDataProvider(priorityLDP);
            bottomView.priorityNS.setValue(report.getPriority());

            Set<Report.Type> typeSet = new HashSet<>();
            for (Report.Type type : Report.Type.values()) {
                typeSet.add(type);
            }
            ListDataProvider<Report.Type> typeLDP = new ListDataProvider<>(typeSet);
            bottomView.typeNS.setDataProvider(typeLDP);
            bottomView.typeNS.setValue(report.getType());

            Set<Report.Status> statusSet = new HashSet<>();
            for (Report.Status status : Report.Status.values()) {
                statusSet.add(status);
            }
            ListDataProvider<Report.Status> statusLDP = new ListDataProvider<>(statusSet);
            bottomView.statusNS.setDataProvider(statusLDP);
            bottomView.statusNS.setValue(report.getStatus());

            ListDataProvider<Reporter> reporterLDP = new ListDataProvider<>(bugrapRepository.findReporters());
            bottomView.assignedNS.setDataProvider(reporterLDP);
            bottomView.assignedNS.setValue(report.getAssigned());

            bottomView.reportDetail.setValue(report.getDescription());

            break;
        }

        for (Report report : topView.reportGrid.getSelectedItems()) {
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
        }
    }

    private void refreshGridData() {
        BugrapRepository.ReportsQuery query = new BugrapRepository.ReportsQuery();
        query.project = topView.projectComboBox.getValue();
        if (query.project == null) {
            return;
        }

        query.projectVersion = topView.versionNS.getValue();

        Set<Report> reports = bugrapRepository.findReports(query);
        ListDataProvider<Report> reportLDP = new ListDataProvider<>(reports);

        if (query.projectVersion == null) {
            topView.reportGrid.getColumn("version").setHidden(false);
        } else {
            topView.reportGrid.getColumn("version").setHidden(true);
        }
        reportLDP.setSortOrder(report -> report.getPriority(), SortDirection.DESCENDING);

        //filter the reports
        if (onlyMeItem.isChecked()) {
            reportLDP.addFilter(report -> report.getAssigned() != null && report.getAssigned().getName().equals(topView.userName.getValue()));
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

        topView.reportGrid.setDataProvider(reportLDP);
    }

    @Override
    public void receiveBroadcast(String message) {
        Report report = ReportSingleton.getInstance().getReports().getFirst();

        for (Report r : topView.reportGrid.getSelectedItems()) {
            if (r.getId() == report.getId()) {
                r.setDescription(report.getDescription());
                r.setVersion(report.getVersion());
                r.setAssigned(report.getAssigned());
                r.setStatus(report.getStatus());
                r.setType(report.getType());
                r.setPriority(report.getPriority());
                r.setSummary(report.getSummary());
                break;
            }
        }

        LinkedList<Report> selectedReports = new LinkedList<>();

        for (Report r : topView.reportGrid.getSelectedItems()) {
            selectedReports.add(r);
        }

        refreshGridData();

        for (Report r : selectedReports) {
            topView.reportGrid.select(r);
        }

        //refreshGridData();
        //topView.reportGrid.getDataProvider().refreshAll();
        refreshBottomView();
    }

    @Override
    public void detach() {
        Broadcaster.unregister(this);
        super.detach();
    }
}