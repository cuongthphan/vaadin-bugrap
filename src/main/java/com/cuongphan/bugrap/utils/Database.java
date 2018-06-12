package com.cuongphan.bugrap.utils;

import org.vaadin.bugrap.domain.BugrapRepository;

public class Database {
    private static BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
    private static Database database;
    private Database() {
    }

    public static Database getInstance() {
        if (database == null) {
            database = new Database();
        }
        return database;
    }

    public BugrapRepository getBugrapRepo() {
        return bugrapRepository;
    }
}
