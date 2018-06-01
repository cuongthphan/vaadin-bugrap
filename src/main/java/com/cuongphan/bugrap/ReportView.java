package com.cuongphan.bugrap;

import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.FileResource;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Comment;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("deprecation")
public class ReportView extends ReportDesign implements View {
    private BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
    private Report report;
    private LinkedList<UploadComponent> uploadComponentLinkedList = new LinkedList<>();

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
    private UploadComponent uploadComponent;

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

            reportDetail.setValue(report.getDescription());
            if (report.getAuthor() != null) {
                authorNameLabel.setValue(report.getAuthor().getName());
            }
            else {
                authorNameLabel.setValue("Anonymous");
            }

            reportDetailLayout.addStyleName("report-detail-layout");
            addUpdateAndRevertListeners();

            HasValue.ValueChangeListener reportValueChangeEvent = new HasValue.ValueChangeListener() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    if (event.isUserOriginated()) {
                        setUpdateAndRevertStatus();
                    }
                }
            };

            versionNS.addValueChangeListener(reportValueChangeEvent);
            priorityNS.addValueChangeListener(reportValueChangeEvent);
            typeNS.addValueChangeListener(reportValueChangeEvent);
            assignedNS.addValueChangeListener(reportValueChangeEvent);
            statusNS.addValueChangeListener(reportValueChangeEvent);
            reportDetail.addValueChangeListener(reportValueChangeEvent);
        }

        openNewButton.setVisible(false);

        addUploadLayoutListeners();
        addCommentValueChangeListener();
        addCancelButtonListener();
        addDoneButtonListener();

        UploadReceiver uploadReceiver = new UploadReceiver();
        attachmentButton.setReceiver(uploadReceiver);
        attachmentButton.addStartedListener(uploadReceiver);
        attachmentButton.addProgressListener(uploadReceiver);
        attachmentButton.addFinishedListener(uploadReceiver);
        attachmentButton.addSucceededListener(uploadReceiver);
    }

    private void addDoneButtonListener() {
        doneButton.addClickListener(event -> {
            uploadLayout.removeAllComponents();
            String username = ((MainUI) getUI()).mainAppView.topView.userName.getValue();

            if (commentTextArea.getValue() != null && !commentTextArea.getValue().isEmpty()) {
                Comment comment = new Comment();
                comment.setComment(commentTextArea.getValue());
                commentTextArea.setValue(null);
                for (Reporter reporter : bugrapRepository.findReporters()) {
                    if (reporter.getName().equals(username)) {
                        comment.setAuthor(reporter);
                        break;
                    }
                }
                comment.setReport(ReportSingleton.getInstance().getReports().getFirst());
                comment.setTimestamp(new Date());
                comment.setType(Comment.Type.COMMENT);
                bugrapRepository.save(comment);
            }

            for (UploadComponent uploadComponent : uploadComponentLinkedList) {
                Comment comment = new Comment();
                comment.setReport(ReportSingleton.getInstance().getReports().getFirst());
                comment.setTimestamp(new Date());
                comment.setType(Comment.Type.ATTACHMENT);
                Path filePath = Paths.get(uploadComponent.file.getAbsolutePath());

                try {
                    comment.setAttachment(Files.readAllBytes(filePath));
                    comment.setAttachmentName(uploadComponent.file.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (Reporter reporter : bugrapRepository.findReporters()) {
                    if (reporter.getName().equals(username)) {
                        comment.setAuthor(reporter);
                        break;
                    }
                }
                bugrapRepository.save(comment);
                uploadComponent.file.delete();
            }

            uploadComponentLinkedList.clear();
        });
    }

    private void addCancelButtonListener() {
        cancelButton.addClickListener(event -> {
            uploadLayout.removeAllComponents();
            for (UploadComponent uploadComponent : uploadComponentLinkedList) {
                uploadComponent.file.delete();
            }
            uploadComponentLinkedList.clear();
        });
    }

    private void setUpdateAndRevertStatus() {
        report = ReportSingleton.getInstance().getReports().getFirst();

        if (!((priorityNS.getValue() == null && report.getPriority() == null) ||
                (priorityNS.getValue() != null && priorityNS.getValue().equals(report.getPriority()))) ||
                !((typeNS.getValue() == null && report.getType() == null) ||
                (typeNS.getValue() != null && typeNS.getValue().equals(report.getType()))) ||
                !((statusNS.getValue() == null && report.getStatus() == null) ||
                (statusNS.getValue() != null && statusNS.getValue().equals(report.getStatus()))) ||
                !((assignedNS.getValue() == null && report.getAssigned() == null) ||
                (assignedNS.getValue() != null && assignedNS.getValue().equals(report.getAssigned()))) ||
                !((versionNS.getValue() == null && report.getVersion() == null) ||
                (versionNS.getValue() != null && versionNS.getValue().equals(report.getVersion()))) ||
                !((reportDetail.getValue() == null && report.getDescription() == null) ||
                (reportDetail.getValue() != null && reportDetail.getValue().equals(report.getDescription())))) {
            updateButton.setEnabled(true);
            revertButton.setEnabled(true);
        }
        else {
            updateButton.setEnabled(false);
            revertButton.setEnabled(false);
        }
    }

    private void addUploadLayoutListeners() {
        uploadLayout.addComponentAttachListener(event -> {
            if (!uploadLayout.getParent().isVisible()) {
                commentTextArea.setRows(commentTextArea.getRows() - 2);
            }
            uploadLayout.getParent().setVisible(true);
            doneButton.setEnabled(true);
            cancelButton.setEnabled(true);
        });
        uploadLayout.addComponentDetachListener(event -> {
            if (uploadLayout.getComponentCount() == 0) {
                uploadLayout.getParent().setVisible(false);
                commentTextArea.setRows(commentTextArea.getRows() + 2);

                if (commentTextArea.getValue().isEmpty()) {
                    doneButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                }
            }
        });
    }

    private void addCommentValueChangeListener() {
        commentTextArea.addValueChangeListener(event -> {
            doneButton.setEnabled(true);
            cancelButton.setEnabled(true);

            if (commentTextArea.getValue().isEmpty() && uploadLayout.getComponentCount() == 0) {
                doneButton.setEnabled(false);
                cancelButton.setEnabled(false);
            }
        });
    }

    private void addUpdateAndRevertListeners() {
        updateButton.addClickListener(update_button_clicked);
        revertButton.addClickListener(revert_button_clicked);
    }

    public void removeUpdateAndRevertListener() {
        updateButton.removeClickListener(update_button_clicked);
        updateButton.removeClickListener(revert_button_clicked);
    }

    public class UploadReceiver implements Upload.Receiver, Upload.StartedListener, Upload.ProgressListener, Upload.FinishedListener, Upload.SucceededListener {
        public File file;

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            FileOutputStream fos = null;
            try {
                file = new File(filename);
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                Notification.show("Could not open file<br/>",
                        e.getMessage(),
                        Notification.Type.ERROR_MESSAGE);
                return null;
            }
            return new ByteArrayOutputStream();
        }

        @Override
        public void uploadStarted(Upload.StartedEvent event) {
            uploadComponent = new UploadComponent();
            uploadComponentLinkedList.add(uploadComponent);
            uploadLayout.removeAllComponents();

            for (UploadComponent component : uploadComponentLinkedList) {
                uploadLayout.addComponent(component);
            }

            uploadComponent.attachmentNameLabel.setValue(event.getFilename());
            uploadComponent.attachmentCancelButton.addClickListener(e -> {
               uploadComponentLinkedList.remove((UploadComponent) e.getComponent().getParent().getParent());
               uploadLayout.removeComponent(e.getComponent().getParent().getParent());

            });
            uploadLayout.addComponent(uploadComponent);
        }

        @Override
        public void updateProgress(long readBytes, long contentLength) {
            uploadComponent.attachmentProgressBar.setValue( readBytes / (float) contentLength);
        }

        @Override
        public void uploadFinished(Upload.FinishedEvent event) {
            uploadComponent.attachmentProgressBar.setVisible(false);
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            uploadComponent.file = file;
        }
    }
}
