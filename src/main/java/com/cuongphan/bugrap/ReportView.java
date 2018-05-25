package com.cuongphan.bugrap;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Window;
import com.vaadin.ui.declarative.Design;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.util.HashSet;
import java.util.Set;

public class ReportView extends ReportDesign implements View {
    public ReportView() {
        addListeners();
    }

    private void addListeners() {
        openNewButton.addClickListener(event -> {
            Window window = new Window();
            breadcrumbsLayout.setVisible(true);
            openNewButton.setVisible(false);
            attachmentLayout.setVisible(true);

            //window.setContent(bottomView);
            window.setSizeFull();
            window.setResizable(false);
            window.addCloseListener(e -> {
                breadcrumbsLayout.setVisible(false);
                openNewButton.setVisible(true);
                attachmentLayout.setVisible(false);
                //mainLayout.setSecondComponent(bottomView);
            });
            //this.getUI().addWindow(window);
        });

        /*updateButton.addClickListener(event -> {
            for (Report r : reportGrid.getSelectedItems()) {
                if (priorityNS.getValue() != null) {
                    r.setPriority(bottomView.priorityNS.getValue());
                }
                if (bottomView.typeNS.getValue() != null) {
                    r.setType(bottomView.typeNS.getValue());
                }
                if (bottomView.statusNS.getValue() != null) {
                    r.setStatus(bottomView.statusNS.getValue());
                }
                if (bottomView.assignedNS.getValue() != null) {
                    r.setAssigned(bottomView.assignedNS.getValue());
                }
                if (bottomView.versionNS.getValue() != null) {
                    r.setVersion(bottomView.versionNS.getValue());
                }
                if (bottomView.reportDetail.getValue() != null) {
                    r.setDescription(bottomView.reportDetail.getValue());
                }
            }
            topView.reportGrid.getDataProvider().refreshAll();
            refreshBottomView();
        });

        revertButton.addClickListener(event -> {
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
        });*/
    }

    private void refreshBottomView() {
        /*for (Report report : topView.reportGrid.getSelectedItems()) {
            bottomView.projectLabel.setValue(report.getProject().getName());
            if (report.getVersion() != null) {
                bottomView.versionLabel.setValue(report.getVersion().getVersion());
            }
            else {
                bottomView.versionLabel.setValue(null);
            }

            bottomView.reportNameLabel.setValue(report.getSummary());

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
        }*/
    }

}
