package com.cuongphan.bugrap.utils;

import org.vaadin.bugrap.domain.BugrapRepository;

public class Databases {
    public static BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");

    private Databases() {
    }
}
