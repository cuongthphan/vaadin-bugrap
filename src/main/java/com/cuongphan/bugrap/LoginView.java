package com.cuongphan.bugrap;

import com.cuongphan.bugrap.ui.MainUI;
import com.cuongphan.bugrap.utils.Account;
import com.cuongphan.bugrap.utils.ViewNames;
import com.vaadin.data.*;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.shared.Registration;
import com.vaadin.ui.UI;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Reporter;

import java.util.concurrent.atomic.AtomicReference;

public class LoginView extends LoginDesign implements View {

    private final Binder<Account> accountBinder = new Binder<>();

    public LoginView() {
        BugrapRepository bugrapRepository = new BugrapRepository("/Users/cuongphanthanh/bugrap-database");
        bugrapRepository.populateWithTestData();
        Account account = new Account();
        final Registration[] reg = new Registration[3];

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

        ShortcutListener shortcut =
                new ShortcutListener("Enter", ShortcutListener.KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                loginButton.click();
            }
        };

        emailTF.addFocusListener(event -> {
            reg[0] = emailTF.addShortcutListener(shortcut);
        });

        passwordField.addFocusListener(event -> {
           reg[1] = passwordField.addShortcutListener(shortcut);
        });

        loginButton.addFocusListener(event -> {
            reg[2] = loginButton.addShortcutListener(shortcut);
        });

        emailTF.addBlurListener(event -> {
            reg[0].remove();
        });

        passwordField.addBlurListener(event -> {
            reg[1].remove();
        });

        loginButton.addBlurListener(event -> {
            reg[2].remove();
        });

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
