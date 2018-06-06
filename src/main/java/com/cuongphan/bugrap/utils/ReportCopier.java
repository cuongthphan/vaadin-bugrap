package com.cuongphan.bugrap.utils;

import org.vaadin.bugrap.domain.entities.Report;

public class ReportCopier {
    private ReportCopier() {

    }

    public static void copy(Report des, Report src) {
        des.setAuthor(src.getAuthor());
        des.setTimestamp(src.getTimestamp());
        des.setSummary(src.getSummary());
        des.setPriority(src.getPriority());
        des.setType(src.getType());
        des.setStatus(src.getStatus());
        des.setAssigned(src.getAssigned());
        des.setVersion(src.getVersion());
        des.setDescription(src.getDescription());
        des.setId(src.getId());
        des.setConsistencyVersion(src.getConsistencyVersion());
        des.setReportedTimestamp(src.getReportedTimestamp());
        des.setProject(src.getProject());
        des.setOccursIn(src.getOccursIn());
    }
}
