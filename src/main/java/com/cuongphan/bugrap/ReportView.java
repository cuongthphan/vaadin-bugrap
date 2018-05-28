package com.cuongphan.bugrap;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.ui.Button;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@SuppressWarnings("deprecation")
public class ReportView extends ReportDesign implements View {
    private BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
    private Report report;

    private final Button.ClickListener update_button_clicked = new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            report = bugrapRepository.getReportById(report.getId());

            report.setDescription(reportDetail.getValue());
            report.setAssigned(assignedNS.getValue());
            report.setType(typeNS.getValue());
            report.setPriority(priorityNS.getValue());
            report.setVersion(versionNS.getValue());
            report.setStatus(statusNS.getValue());

            bugrapRepository.save(report);

            ReportSingleton.getInstance().clearReports();
            ReportSingleton.getInstance().addReport(report);

            Broadcaster.broadcast("Update button clicked");
        }
    };
    private final Button.ClickListener revert_button_clicked = new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            LinkedList<Report> reportList = ReportSingleton.getInstance().getReports();

            Report report = reportList.getFirst();
            report = bugrapRepository.getReportById(report.getId());

            priorityNS.setValue(report.getPriority());
            typeNS.setValue(report.getType());
            statusNS.setValue(report.getStatus());
            assignedNS.setValue(report.getAssigned());
            versionNS.setValue(report.getVersion());
            reportDetail.setValue(report.getDescription());
        }
    };

    public ReportView() {
        bugrapRepository.populateWithTestData();

        LinkedList<Report> reportList = ReportSingleton.getInstance().getReports();

        if (reportList.size() == 1) {
            report = reportList.getFirst();

            if (report.getProject() != null) {
                projectLabel.setValue(report.getProject().getName());
            }
            else {
                projectLabel.setValue(null);
            }
            if (report.getVersion() != null) {
                versionLabel.setValue(report.getVersion().getVersion());
            }
            else {
                versionLabel.setValue(null);
            }

            reportNameLabel.setValue(report.getSummary());

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(report.getProject()));
            versionNS.setDataProvider(projectVersionLDP);
            versionNS.setValue(report.getVersion());

            Set<Report.Priority> prioritySet = new HashSet<>();
            for (Report.Priority priority : Report.Priority.values()) {
                prioritySet.add(priority);
            }
            ListDataProvider<Report.Priority> priorityLDP = new ListDataProvider<>(prioritySet);
            priorityNS.setDataProvider(priorityLDP);
            priorityNS.setValue(report.getPriority());

            Set<Report.Type> typeSet = new HashSet<>();
            for (Report.Type type : Report.Type.values()) {
                typeSet.add(type);
            }
            ListDataProvider<Report.Type> typeLDP = new ListDataProvider<>(typeSet);
            typeNS.setDataProvider(typeLDP);
            typeNS.setValue(report.getType());

            Set<Report.Status> statusSet = new HashSet<>();
            for (Report.Status status : Report.Status.values()) {
                statusSet.add(status);
            }
            ListDataProvider<Report.Status> statusLDP = new ListDataProvider<>(statusSet);
            statusNS.setDataProvider(statusLDP);
            statusNS.setValue(report.getStatus());

            ListDataProvider<Reporter> reporterLDP = new ListDataProvider<>(bugrapRepository.findReporters());
            assignedNS.setDataProvider(reporterLDP);
            assignedNS.setValue(report.getAssigned());

            if (report.getDescription() != null) {
                reportDetail.setValue(report.getDescription());
            }
            else {
                reportDetail.setValue(null);
            }

            addListeners();
        }

        if (reportList.size() > 1) {
            openNewButton.setVisible(false);
        }
    }

    private void addListeners() {
        updateButton.addClickListener(update_button_clicked);

        revertButton.addClickListener(revert_button_clicked);
    }

    public void removeListener() {
        updateButton.removeClickListener(update_button_clicked);
        updateButton.removeClickListener(revert_button_clicked);
    }

}
