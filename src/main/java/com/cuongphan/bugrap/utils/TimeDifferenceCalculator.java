package com.cuongphan.bugrap.utils;

import java.text.SimpleDateFormat;

import java.util.Date;

public class TimeDifferenceCalculator {
    private TimeDifferenceCalculator() {

    }

    public static String calc(Date date) {
        long diff = (new Date()).getTime() - date.getTime();

        if (diff > 0) {
            long diffMin = diff / (60 * 1000) % 60;
            long diffHr = diff / (60 * 60 * 1000) % 60;
            long diffDay = diff / (24 * 60 * 60 * 1000);
            long diffWeek = diff / (7 * 24 * 60 * 60 * 1000);

            if (diffWeek > 1) {
                return new SimpleDateFormat("EEE HH:mm, dd-MM-yyyy").format(date);
            }
            else if (diffWeek == 1) {
                return "1 week ago";
            }
            else if (diffDay < 7 && diffDay > 1) {
                return diffDay + " days ago";
            }
            else if (diffDay == 1) {
                return "1 day ago";
            }
            else if (diffHr > 1) {
                return diffHr + " hours ago";
            }
            else if (diffHr == 1) {
                return "1 hour ago";
            }
            else if (diffMin > 1) {
                return diffMin + " minutes ago";
            }
            else if (diffMin == 1) {
                return "1 minute ago";
            }
            else {
                return "Just now";
            }
        }

        return "Just now";
    }
}
