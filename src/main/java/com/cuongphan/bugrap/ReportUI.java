package com.cuongphan.bugrap;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.util.HashSet;
import java.util.Set;

@Theme("mytheme")
@StyleSheet({"https://fonts.googleapis.com/css?family=Roboto"})
public class ReportUI extends UI {
    private ReportView reportView = new ReportView();
    private BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");

    @Override
    protected void init(VaadinRequest request) {
        bugrapRepository.populateWithTestData();

        reportView.openNewButton.setVisible(false);
        Report report = ReportSingleton.getInstance().getReport();

        reportView.projectLabel.setValue(report.getProject().getName());
        if (report.getVersion() != null) {
            reportView.versionLabel.setValue(report.getVersion().getVersion());
        }
        else {
            reportView.versionLabel.setValue(null);
        }

        reportView.reportNameLabel.setValue(report.getSummary());

        ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(bugrapRepository.findProjectVersions(report.getProject()));
        reportView.versionNS.setDataProvider(projectVersionLDP);
        reportView.versionNS.setValue(report.getVersion());

        Set<Report.Priority> prioritySet = new HashSet<>();
        for (Report.Priority priority : Report.Priority.values()) {
            prioritySet.add(priority);
        }
        ListDataProvider<Report.Priority> priorityLDP = new ListDataProvider<>(prioritySet);
        reportView.priorityNS.setDataProvider(priorityLDP);
        reportView.priorityNS.setValue(report.getPriority());

        Set<Report.Type> typeSet = new HashSet<>();
        for (Report.Type type : Report.Type.values()) {
            typeSet.add(type);
        }
        ListDataProvider<Report.Type> typeLDP = new ListDataProvider<>(typeSet);
        reportView.typeNS.setDataProvider(typeLDP);
        reportView.typeNS.setValue(report.getType());

        Set<Report.Status> statusSet = new HashSet<>();
        for (Report.Status status : Report.Status.values()) {
            statusSet.add(status);
        }
        ListDataProvider<Report.Status> statusLDP = new ListDataProvider<>(statusSet);
        reportView.statusNS.setDataProvider(statusLDP);
        reportView.statusNS.setValue(report.getStatus());

        ListDataProvider<Reporter> reporterLDP = new ListDataProvider<>(bugrapRepository.findReporters());
        reportView.assignedNS.setDataProvider(reporterLDP);
        reportView.assignedNS.setValue(report.getAssigned());

        reportView.reportDetail.setValue(report.getDescription());

        addListeners();
        setContent(reportView);
    }

    private void addListeners() {
        reportView.updateButton.addClickListener(e -> {
            Report report = ReportSingleton.getInstance().getReport();
            report = bugrapRepository.getReportById(report.getId());

            report.setDescription(reportView.reportDetail.getValue());
            report.setAssigned(reportView.assignedNS.getValue());
            report.setType(reportView.typeNS.getValue());
            report.setPriority(reportView.priorityNS.getValue());
            report.setVersion(reportView.versionNS.getValue());
            report.setStatus(reportView.statusNS.getValue());

            bugrapRepository.save(report);
            ReportSingleton.getInstance().setReport(report);

            Notification.show(bugrapRepository.getReportById(report.getId()).getAssigned().getName() + "\n"
                + report.getAssigned().getName());
        });

        reportView.revertButton.addClickListener(e -> {
            Report report = ReportSingleton.getInstance().getReport();
            report = bugrapRepository.getReportById(report.getId());

            reportView.priorityNS.setValue(report.getPriority());
            reportView.typeNS.setValue(report.getType());
            reportView.statusNS.setValue(report.getStatus());
            reportView.assignedNS.setValue(report.getAssigned());
            reportView.versionNS.setValue(report.getVersion());
            reportView.reportDetail.setValue(report.getDescription());

            Notification.show(report.getAssigned().getName());
        });
    }
}
