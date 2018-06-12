package com.cuongphan.bugrap;

import com.cuongphan.bugrap.ui.MainUI;
import com.cuongphan.bugrap.utils.Account;
import com.cuongphan.bugrap.utils.ReportFilter;
import com.cuongphan.bugrap.utils.ViewNames;
import com.vaadin.data.*;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.navigator.View;
import com.vaadin.ui.UI;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Reporter;

public class LoginView extends LoginDesign implements View {

    private final Binder<Account> accountBinder = new Binder<>();

    public LoginView() {
        BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
        bugrapRepository.populateWithTestData();
        Account account = new Account();

        BindingValidationStatusHandler statusHandler = new BindingValidationStatusHandler() {
            @Override
            public void statusChange(BindingValidationStatus<?> statusChange) {
                errorLabel.setValue(emailTF.getValue().isEmpty()? "" : statusChange.getMessage().orElse(""));
            }
        };

        accountBinder.setBean(account);

        accountBinder.forField(emailTF)
                .withValidator(new EmailValidator("Invalid email"))
                .withValidationStatusHandler(statusHandler)
                .bind(Account::getEmail, Account::setEmail);
        accountBinder.forField(passwordField)
                .bind(Account::getPassword, Account::setPassword);

        loginButton.addClickListener(event -> {
            if (!accountBinder.isValid()) {
                return;
            }
            for (Reporter reporter : bugrapRepository.findReporters()) {
                if (reporter.getEmail().equals(account.getEmail())
                        && reporter.getPassword().equals(account.getPassword())) {
                    resetForm();
                    errorLabel.setValue("");
                    ((MainUI) UI.getCurrent()).navigator.navigateTo(ViewNames.MAINAPPVIEW + "/" + reporter.getName());
                    break;
                }
            }
            errorLabel.setValue("Wrong email or password");
        });
    }

    public void resetForm() {
        emailTF.setValue("");
        passwordField.setValue("");
    }
}
