package com.cuongphan.bugrap;

import org.vaadin.bugrap.domain.entities.Report;

public class ReportSingleton {
    private static Report report = new Report();
    private static ReportSingleton reportSingleton;

    private ReportSingleton() {
    }

    public void setReport(Report report) {
        ReportSingleton.report.setSummary(report.getSummary());
        ReportSingleton.report.setDescription(report.getDescription());
        ReportSingleton.report.setAssigned(report.getAssigned());
        ReportSingleton.report.setType(report.getType());
        ReportSingleton.report.setPriority(report.getPriority());
        ReportSingleton.report.setVersion(report.getVersion());
        ReportSingleton.report.setStatus(report.getStatus());
        ReportSingleton.report.setAuthor(report.getAuthor());
        ReportSingleton.report.setOccursIn(report.getOccursIn());
        ReportSingleton.report.setProject(report.getProject());
        ReportSingleton.report.setReportedTimestamp(report.getReportedTimestamp());
        ReportSingleton.report.setTimestamp(report.getTimestamp());
        ReportSingleton.report.setConsistencyVersion(report.getConsistencyVersion());
        ReportSingleton.report.setId(report.getId());
    }

    public Report getReport() {
        return report;
    }

    public static ReportSingleton getInstance() {
        if (reportSingleton == null) {
            reportSingleton = new ReportSingleton();
        }
        return reportSingleton;
    }
}
