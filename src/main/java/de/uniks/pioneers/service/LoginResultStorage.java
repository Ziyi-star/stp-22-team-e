package de.uniks.pioneers.service;

import de.uniks.pioneers.model.LoginResult;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginResultStorage {

    private LoginResult loginResult;


    @Inject
    public LoginResultStorage() {

    }

    public LoginResult getLoginResult() {
        return loginResult;
    }

    public void setLoginResult(LoginResult loginResult) {
        this.loginResult = loginResult;
    }
}
