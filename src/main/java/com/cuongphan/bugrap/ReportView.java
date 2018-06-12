package com.cuongphan.bugrap;

import com.cuongphan.bugrap.customcomponents.CommentComponent;
import com.cuongphan.bugrap.utils.*;
import com.cuongphan.bugrap.ui.MainUI;
import com.cuongphan.bugrap.customcomponents.UploadComponent;
import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.*;
import com.vaadin.util.FileTypeResolver;
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
    private Report report;
    private Report bindedReport;
    private LinkedList<UploadComponent> uploadComponentLinkedList = new LinkedList<>();
    private LinkedList<CommentComponent> commentComponentLinkedList = new LinkedList<>();

    private final Button.ClickListener update_button_clicked = new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            report = Database.getInstance().getBugrapRepo().getReportById(report.getId());

            report.setDescription(bindedReport.getDescription());
            report.setAssigned(bindedReport.getAssigned());
            report.setType(bindedReport.getType());
            report.setPriority(bindedReport.getPriority());
            report.setVersion(bindedReport.getVersion());
            report.setStatus(bindedReport.getStatus());

            Database.getInstance().getBugrapRepo().save(report);

            ReportSingleton.getInstance().clearReports();
            ReportSingleton.getInstance().addReport(report);

            Broadcaster.broadcast(((MainUI)getParent()).getPage().getUriFragment());

            ReportSingleton.getInstance().clearReports();
            ReportSingleton.getInstance().addReport(Database.getInstance().getBugrapRepo().getReportById(report.getId()));

            updateButton.setEnabled(false);
            revertButton.setEnabled(false);
            report = Database.getInstance().getBugrapRepo().getReportById(report.getId());

            authorNameLabel.setValue(((MainUI)getParent()).mainAppView.topView.usernameLabel.getValue());
            timeStampLabel.setValue("(" + TimeDifferenceCalculator.calc(report.getTimestamp()) + ")");
        }
    };

    private final Button.ClickListener revert_button_clicked = new Button.ClickListener() {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            ReportCopier.copy(bindedReport, report);
            reportBinder.removeBean();
            reportBinder.setBean(bindedReport);
            updateButton.setEnabled(false);
            revertButton.setEnabled(false);
        }
    };

    private UploadComponent uploadComponent;
    private Binder<Report> reportBinder = null;

    public ReportView() {
        Database.getInstance().getBugrapRepo().populateWithTestData();
        bindedReport = new Report();

        LinkedList<Report> reportList = ReportSingleton.getInstance().getReports();
        if (reportList.size() != 0) {
            report = reportList.getFirst();

            ReportCopier.copy(bindedReport, report);

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

            ListDataProvider<ProjectVersion> projectVersionLDP = new ListDataProvider<>(Database.getInstance().getBugrapRepo().findProjectVersions(report.getProject()));
            versionNS.setDataProvider(projectVersionLDP);
            //versionNS.setValue(report.getVersion());

            Set<Report.Priority> prioritySet = new HashSet<>();
            for (Report.Priority priority : Report.Priority.values()) {
                prioritySet.add(priority);
            }
            ListDataProvider<Report.Priority> priorityLDP = new ListDataProvider<>(prioritySet);
            priorityNS.setDataProvider(priorityLDP);
            //priorityNS.setValue(report.getPriority());

            Set<Report.Type> typeSet = new HashSet<>();
            for (Report.Type type : Report.Type.values()) {
                typeSet.add(type);
            }
            ListDataProvider<Report.Type> typeLDP = new ListDataProvider<>(typeSet);
            typeNS.setDataProvider(typeLDP);
            //typeNS.setValue(report.getType());

            Set<Report.Status> statusSet = new HashSet<>();
            for (Report.Status status : Report.Status.values()) {
                statusSet.add(status);
            }
            ListDataProvider<Report.Status> statusLDP = new ListDataProvider<>(statusSet);
            statusNS.setDataProvider(statusLDP);
            //statusNS.setValue(report.getStatus());

            ListDataProvider<Reporter> reporterLDP = new ListDataProvider<>(Database.getInstance().getBugrapRepo().findReporters());
            assignedNS.setDataProvider(reporterLDP);
            //assignedNS.setValue(report.getAssigned());

            //reportDetail.setValue(report.getDescription());
            if (report.getAuthor() != null) {
                authorNameLabel.setValue(report.getAuthor().getName());
            }
            else {
                authorNameLabel.setValue("Anonymous");
            }

            timeStampLabel.setValue("(" + TimeDifferenceCalculator.calc(report.getTimestamp()) + ")");

            reportDetailLayout.addStyleName("report-detail-layout");
            addUpdateAndRevertListeners();

            HasValue.ValueChangeListener reportValueChangeEvent = new HasValue.ValueChangeListener() {
                @Override
                public void valueChange(HasValue.ValueChangeEvent event) {
                    if (event.isUserOriginated()) {
                        setUpdateAndRevertStatus();
                    }
                    if (event.getSource().getClass() == TextArea.class) {
                        ((TextArea)event.getSource()).setRows(10);
                    }
                }
            };

            versionNS.addValueChangeListener    (reportValueChangeEvent);
            priorityNS.addValueChangeListener   (reportValueChangeEvent);
            typeNS.addValueChangeListener       (reportValueChangeEvent);
            assignedNS.addValueChangeListener   (reportValueChangeEvent);
            statusNS.addValueChangeListener     (reportValueChangeEvent);
            reportDetail.addValueChangeListener (reportValueChangeEvent);
        }

        openNewButton.setVisible(false);

        addUploadLayoutListeners();
        addCommentValueChangeListener();
        addCancelButtonListener();
        addDoneButtonListener();

        UploadReceiver uploadReceiver = new UploadReceiver();

        attachmentButton.setReceiver            (uploadReceiver);
        attachmentButton.addStartedListener     (uploadReceiver);
        attachmentButton.addProgressListener    (uploadReceiver);
        attachmentButton.addFinishedListener    (uploadReceiver);
        attachmentButton.addSucceededListener   (uploadReceiver);

        reportBinder = new Binder<>();
        reportBinder.forField(priorityNS)   .bind(Report::getPriority,      Report::setPriority);
        reportBinder.forField(typeNS)       .bind(Report::getType,          Report::setType);
        reportBinder.forField(statusNS)     .bind(Report::getStatus,        Report::setStatus);
        reportBinder.forField(assignedNS)   .bind(Report::getAssigned,      Report::setAssigned);
        reportBinder.forField(versionNS)    .bind(Report::getVersion,       Report::setVersion);
        reportBinder.forField(reportDetail) .bind(Report::getDescription,   Report::setDescription);

        if (reportList.size() == 1) {
            reportBinder.setBean(bindedReport);

            versionNS.setEmptySelectionAllowed(false);
            priorityNS.setEmptySelectionAllowed(false);
            typeNS.setEmptySelectionAllowed(false);
            openNewButton.setVisible(true);
            reportDetail.setVisible(true);
        }
        else if (reportList.size() > 1) {
            report = reportList.getFirst();

            openNewButton.setVisible(false);
            reportDetail.setVisible(false);
            reportNameLabel.setValue(ReportSingleton.getInstance().getReports().size() +
                    " reported selected - Select a single report to view contents");

            ReportCopier.copy(bindedReport, report);

            for (Report r : reportList) {
                if (r.getPriority() != bindedReport.getPriority()) {
                    bindedReport.setPriority(null);
                }
                if (r.getType() != bindedReport.getType()) {
                    bindedReport.setPriority(null);
                }
                if (r.getStatus() != bindedReport.getStatus()) {
                    bindedReport.setStatus(null);
                }
                if (r.getAssigned() != bindedReport.getAssigned()) {
                    bindedReport.setAssigned(null);
                }
                if (r.getVersion() != bindedReport.getVersion()) {
                    bindedReport.setVersion(null);
                }
            }
            reportBinder.setBean(bindedReport);
        }

        //display comments
        displayComments();
    }

    private void displayComments() {
        while (1 < reportDescriptionLayout.getComponentCount()) {
            reportDescriptionLayout.removeComponent(reportDescriptionLayout.getComponent(1));
        }
        if (!ReportSingleton.getInstance().getReports().isEmpty()) {
            for (Comment comment : Database.getInstance().getBugrapRepo().findComments(ReportSingleton.getInstance().getReports().getFirst())) {
                CommentComponent component = new CommentComponent();
                component.authorNameLabel.setValue(comment.getAuthor().getName());

                reportDescriptionLayout.addComponent(component);
                if (comment.getType() == Comment.Type.COMMENT) {
                    component.commentDetail.setValue(comment.getComment());
                }
                else {
                    component.commentDetail.setValue(comment.getAttachmentName());
                    component.commentDetail.addStyleName("link");
                    component.commentWrapper.addLayoutClickListener(event -> {
                        Window subWindow = new Window();
                        subWindow.center();

                        File f = new File(comment.getAttachmentName());
                        try (FileOutputStream fos = new FileOutputStream(f)) {
                            fos.write(comment.getAttachment());
                            fos.flush();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        subWindow.addCloseListener(e -> {
                            f.delete();
                        });

                        String extension = FileTypeResolver.getMIMEType(f).split("\\/")[0];
                        if (extension.equals("image")) {
                            Image image = new Image(f.getName(), new FileResource(
                                    new File(f.getAbsolutePath())
                            ));
                            image.setWidth("500px");

                            VerticalLayout verticalLayout = new VerticalLayout();
                            verticalLayout.addComponent(image);
                            subWindow.setContent(verticalLayout);
                        }

                        UI.getCurrent().addWindow(subWindow);
                    });
                }
                commentComponentLinkedList.add(component);
            }
        }
    }

    private void addDoneButtonListener() {
        doneButton.addClickListener(event -> {
            uploadLayout.removeAllComponents();
            String username = ((MainUI) getUI()).mainAppView.topView.usernameLabel.getValue();

            if (commentTextArea.getValue() != null && !commentTextArea.getValue().isEmpty()) {
                Comment comment = new Comment();
                comment.setComment(commentTextArea.getValue());
                commentTextArea.setValue("");
                for (Reporter reporter : Database.getInstance().getBugrapRepo().findReporters()) {
                    if (reporter.getName().equals(username)) {
                        comment.setAuthor(reporter);
                        break;
                    }
                }
                comment.setReport(ReportSingleton.getInstance().getReports().getFirst());
                comment.setTimestamp(new Date());
                comment.setType(Comment.Type.COMMENT);
                Database.getInstance().getBugrapRepo().save(comment);
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
                for (Reporter reporter : Database.getInstance().getBugrapRepo().findReporters()) {
                    if (reporter.getName().equals(username)) {
                        comment.setAuthor(reporter);
                        break;
                    }
                }
                Database.getInstance().getBugrapRepo().save(comment);
                uploadComponent.file.delete();
            }

            uploadComponentLinkedList.clear();
            displayComments();
            reportDescriptionPanel.setScrollTop(Integer.MAX_VALUE);
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
        Report temp = Database.getInstance().getBugrapRepo().getReportById(report.getId());

        if (priorityNS.getValue() != temp.getPriority() ||
                typeNS.getValue() != temp.getType() ||
                statusNS.getValue() != temp.getStatus() ||
                (assignedNS.getValue() == null && assignedNS.getValue() != temp.getAssigned()) ||
                (assignedNS.getValue() != null && !assignedNS.getValue().equals(temp.getAssigned())) ||
                (versionNS.getValue() == null && versionNS.getValue() != temp.getVersion()) ||
                (versionNS.getValue() !=null && !versionNS.getValue().equals(temp.getVersion()))) {
            updateButton.setEnabled(true);
            revertButton.setEnabled(true);
        } else {
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
            return fos;
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
