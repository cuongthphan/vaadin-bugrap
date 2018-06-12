package com.cuongphan.bugrap.utils;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;

import java.util.HashSet;
import java.util.Set;

public class ReportFilter {
    public static Set<Report> findReports(String project, String version, String keyword, String assignee, String[] status) {
        BugrapRepository.ReportsQuery query = new BugrapRepository.ReportsQuery();
        Set<Report> result = new HashSet<>();

        for (Project prj : Databases.bugrapRepository.findProjects()) {
            if (prj.getName().equals(project)) {
                query.project = prj;
                for (ProjectVersion ver : Databases.bugrapRepository.findProjectVersions(prj)) {
                    if (ver.getVersion().equals(version)) {
                        query.projectVersion = ver;
                        break;
                    }
                }
                break;
            }
        }

        result = Databases.bugrapRepository.findReports(query);
        Set<Report> filteredReport = new HashSet<>();

        for (Report r : result) {
            if (keyword != null && !keyword.isEmpty()) {
                if (!r.getSummary().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredReport.add(r);
                    continue;
                }
            }

            if (assignee != null && !assignee.isEmpty()) {
                if (r.getAssigned() == null || (r.getAssigned()!= null && !r.getAssigned().getName().equals(assignee))) {
                    filteredReport.add(r);
                    continue;
                }
            }

            if (status == null || status.length == 0) {
                continue;
            }

            boolean hasStatus = false;
            for (String s : status) {
                if (r.getStatus() != null && r.getStatus().toString().equals(s)) {
                    hasStatus = true;
                    break;
                }
            }
            if (!hasStatus) {
                filteredReport.add(r);
                continue;
            }
        }

        for (Report r : filteredReport) {
            result.remove(r);
        }

        return result;
    }
}
