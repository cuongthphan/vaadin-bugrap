package com.cuongphan.bugrap;

import com.cuongphan.bugrap.customcomponents.PriorityComponent;
import com.cuongphan.bugrap.utils.*;
import com.cuongphan.bugrap.ui.MainUI;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.BrowserWindowOpener;
import com.vaadin.server.SerializableComparator;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import org.vaadin.addons.searchbox.SearchBox;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.util.*;

public class MainAppView extends VerticalSplitPanel implements View, Broadcaster.BroadcastListener {
    public MainView topView;
    private Set<String> checkedItems = new HashSet<>();
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
    private Set<Report> previousSelectedItems = new HashSet<>();

    public MainAppView() {
        setSizeFull();

        topView = new MainView();
        bottomView = new ReportView();
        topView.logoutLayout.addLayoutClickListener(event -> {
            ((MainUI)UI.getCurrent()).navigator.navigateTo(ViewNames.LOGINVIEW);
        });

        setFirstComponent(topView);
        setSecondComponent(bottomView);
        setSplitPosition(100, Unit.PERCENTAGE);

        //add search box
        searchBox = new SearchBox(VaadinIcons.SEARCH, SearchBox.ButtonPosition.LEFT);
        searchBox.setId("searchbox");
        searchBox.setButtonJoined(true);
        searchBox.getSearchField().setPlaceholder("Search reports...");
        searchBox.getSearchField().setId("searchfield");
        searchBox.setSuggestionListSize(5);
        searchBox.setWidth(100, Unit.PERCENTAGE);
        searchBox.addSearchListener(event -> refreshGridData());
        searchBox.setSearchMode(SearchBox.SearchMode.DEBOUNCE);
        searchBox.setDebounceTime(200);


        topView.searchBoxLayout.addComponent(searchBox);

        //set expand ratio for grid columns
        topView.reportGrid.addComponentColumn(report -> new PriorityComponent(report.getPriority()))
                .setCaption("PRIORITY")
                .setId("priority")
                .setComparator(new SerializableComparator<Report>() {
                    @Override
                    public int compare(Report o1, Report o2) {
                        return o1.getPriority().compareTo(o2.getPriority());
                    }
                });
        topView.reportGrid.addComponentColumn(report -> new Label(TimeDifferenceCalculator.calc(report.getTimestamp())))
                .setId("timestamp")
                .setCaption("LAST MODIFIED")
        ;
        topView.reportGrid.addComponentColumn(report -> new Label(TimeDifferenceCalculator.calc(report.getReportedTimestamp())))
                .setId("reportedTimestamp")
                .setCaption("REPORTED")
        ;
        topView.reportGrid.getColumn("version").setExpandRatio(1);
        topView.reportGrid.getColumn("version").setHidden(true);
        topView.reportGrid.getColumn("priority").setExpandRatio(1);
        topView.reportGrid.getColumn("type").setExpandRatio(1);
        topView.reportGrid.getColumn("summary").setExpandRatio(7);
        topView.reportGrid.getColumn("assigned").setExpandRatio(1);
        topView.reportGrid.getColumn("timestamp").setExpandRatio(1);
        topView.reportGrid.getColumn("reportedTimestamp").setExpandRatio(1);
        topView.reportGrid.sort("version", SortDirection.DESCENDING);

        topView.reportGrid.setColumnOrder(
                "version", "priority", "type", "summary", "assigned", "timestamp", "reportedTimestamp"
        );
        //get data
        topView.projectCountLable.setValue(Integer.toString(Database.getInstance().getBugrapRepo().findProjects().size()));
        topView.projectCountLable.setId("projectCountLabel");

        //add project list data provider to project combo box in ascending order by name
        ListDataProvider<Project> projectLDP = new ListDataProvider<>(Database.getInstance().getBugrapRepo().findProjects());
        projectLDP.setSortOrder(project -> project.getName(), SortDirection.ASCENDING);
        topView.projectComboBox.setDataProvider(projectLDP);
        topView.projectComboBox.setEmptySelectionAllowed(false);

        //add version from chosen project to version native select
        //and reset context menu and grid
        topView.projectComboBox.addValueChangeListener(e -> {
            topView.reportGrid.setItems();
            checkedItems.clear();

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(Database.getInstance().getBugrapRepo().findProjectVersions(e.getValue()));

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
            previousSelectedItems.clear();
            for (Report r : topView.reportGrid.getSelectedItems()) {
                if ((e.getValue() == null) || (r.getVersion()!= null && r.getVersion().equals(e.getValue()))) {
                    previousSelectedItems.add(r);
                }
            }
            refreshGridData();
            for (Report r : previousSelectedItems) {
                topView.reportGrid.select(r);
            }
            previousSelectedItems.clear();
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
            boolean previousIsCustom = false;

            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                if (previous != null) {
                    if (!previous.isChecked()) {
                        previousIsCustom = false;
                    }
                    else {
                        previousIsCustom = true;
                    }
                    previous.setChecked(false);
                }
                if (previous == selectedItem) {
                    if (previousIsCustom) {
                        selectedItem.setChecked(true);
                    }
                    else {
                        selectedItem.setChecked(false);
                        previous = null;
                    }
                } else {
                    selectedItem.setCheckable(true);
                    selectedItem.setChecked(true);
                    previous = selectedItem;
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
            ReportSingleton.getInstance().clearReports();
            for (Report report : topView.reportGrid.getSelectedItems()) {
                ReportSingleton.getInstance().addReport(report);
            }

            if (topView.reportGrid.getSelectedItems().size() != 0) {
                bottomView = new ReportView();

                bottomView.breadcrumbsLayout.setVisible(false);
                bottomView.attachmentLayout.setVisible(false);
                setSecondComponent(bottomView);
                setLocked(false);

                //display second view if 1 report is selected
                if (topView.reportGrid.getSelectedItems().size() == 1) {
                    setSplitPosition(60, Unit.PERCENTAGE);
                }
                // if more than 1 reported chosen
                else {
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
        opener.setWindowName(topView.usernameLabel.getValue() + "'s report view");
        opener.setUriFragment("" + ReportSingleton.getInstance().getReports().getFirst().getId());

        bottomView.openNewButton.addClickListener(event -> {
            for (Report report : topView.reportGrid.getSelectedItems()) {
                ReportSingleton.getInstance().clearReports();
                ReportSingleton.getInstance().addReport(report);
            }
        });

        bottomView.removeUpdateAndRevertListener();

        bottomView.updateButton.addClickListener(event -> {
            bottomView.updateButton.setEnabled(false);
            bottomView.revertButton.setEnabled(false);

            Reporter author = null;
            for (Reporter reporter : Database.getInstance().getBugrapRepo().findReporters()) {
                if (reporter.getName().equals(topView.usernameLabel.getValue())) {
                    author = reporter;
                    break;
                }
            }

            for (Report r : topView.reportGrid.getSelectedItems()) {
                Report report = Database.getInstance().getBugrapRepo().getReportById(r.getId());
                if (author != null) {
                    r.setAuthor(author);
                    report.setAuthor(author);
                }
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
                Database.getInstance().getBugrapRepo().save(report);
            }
            topView.reportGrid.getSelectedItems().iterator().next().setTimestamp(new Date());
            topView.reportGrid.getDataProvider().refreshAll();

            refreshBottomView();
        });

        bottomView.revertButton.addClickListener(event -> {
            bottomView.updateButton.setEnabled(false);
            bottomView.revertButton.setEnabled(false);

            for (Report r : topView.reportGrid.getSelectedItems()) {
                Report origin = Database.getInstance().getBugrapRepo().getReportById(r.getId());
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

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(Database.getInstance().getBugrapRepo().findProjectVersions(report.getProject()));
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

            ListDataProvider<Reporter> reporterLDP = new ListDataProvider<>(Database.getInstance().getBugrapRepo().findReporters());
            bottomView.assignedNS.setDataProvider(reporterLDP);
            bottomView.assignedNS.setValue(report.getAssigned());

            if (report.getAuthor() != null) {
                bottomView.authorNameLabel.setValue(report.getAuthor().getName());
            }
            else {
                bottomView.authorNameLabel.setValue("Anonymous");
            }

            bottomView.timeStampLabel.setValue("(" +
                    TimeDifferenceCalculator.calc(Database.getInstance().getBugrapRepo().getReportById(report.getId()).getTimestamp()) + ")"
            );
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

        Set<Report> reports = Database.getInstance().getBugrapRepo().findReports(query);
        ListDataProvider<Report> reportLDP = new ListDataProvider<>(reports);

        if (query.projectVersion == null) {
            topView.reportGrid.getColumn("version").setHidden(false);
            topView.reportGrid.sort("version", SortDirection.ASCENDING);
        } else {
            topView.reportGrid.getColumn("version").setHidden(true);
            topView.reportGrid.sort("priority", SortDirection.DESCENDING);
        }
        reportLDP.setSortOrder(report -> report.getPriority(), SortDirection.DESCENDING);

        //filter the reports
        if (onlyMeItem.isChecked()) {
            reportLDP.addFilter(report -> report.getAssigned() != null && report.getAssigned().getName().equals(topView.usernameLabel.getValue()));
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
            String s;
        }
        reportLDP.addFilter(report -> report.getSummary() != null
                && report.getSummary().toLowerCase().contains(searchBox.getSearchField().getValue().toLowerCase()));

        //topView.reportGrid.setItems(reportLDP.getItems());
        topView.reportGrid.setDataProvider(reportLDP);
        topView.reportGrid.scrollToStart();
    }

    @Override
    public void receiveBroadcast(String message) {
        Report report = Database.getInstance().getBugrapRepo().getReportById(Long.parseLong(message));
        if (topView.usernameLabel.getValue()!= null && !topView.usernameLabel.getValue().isEmpty()) {
            for (Reporter reporter : Database.getInstance().getBugrapRepo().findReporters()) {
                if (reporter.getName().equals(topView.usernameLabel.getValue())) {
                    report.setAuthor(reporter);
                    Database.getInstance().getBugrapRepo().save(report);
                    break;
                }
            }
        }

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

        refreshBottomView();
    }

    @Override
    public void detach() {
        Broadcaster.unregister(this);
        super.detach();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        topView.usernameLabel.setValue(event.getParameters());
    }
}