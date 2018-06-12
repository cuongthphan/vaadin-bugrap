package com.cuongphan.bugrap.utils;

import java.util.ArrayList;
import java.util.List;

public class AccountList {
    private static List<Account> accountArrayList = new ArrayList<>();
    private static AccountList ourInstance = new AccountList();

    public static AccountList getInstance() {
        return ourInstance;
    }

    private AccountList() {
        accountArrayList.add(new Account(
                "admin@bugrap.com",
                "d033e22ae348aeb5660fc2140aec35850c4da997",
                "admin"
        ));

        accountArrayList.add(new Account(
                "manager@bugrap.com",
                "1a8565a9dc72048ba03b4156be3e569f22771f23",
                "manager"
        ));

        accountArrayList.add(new Account(
                "developer@bugrap.com",
                "3dacbce532ccd48f27fa62e993067b3c35f094f7",
                "developer"
        ));
    }

    public List<Account> getList() {
        return accountArrayList;
    }
}
