package com.cuongphan.bugrap.utils;

import org.vaadin.bugrap.domain.entities.Report;

import java.util.LinkedList;

public class ReportSingleton {
    private static LinkedList<Report> reportList = new LinkedList<>();
    private static ReportSingleton reportSingleton;

    private ReportSingleton() {
    }

    public void addReport(Report report) {
        reportList.add(report);
    }

    public void clearReports() {
        reportList.clear();
    }

    public LinkedList<Report> getReports() {
        return reportList;
    }

    public static ReportSingleton getInstance() {
        if (reportSingleton == null) {
            reportSingleton = new ReportSingleton();
        }
        return reportSingleton;
    }
}
