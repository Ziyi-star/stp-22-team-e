package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.LoginDto;
import de.uniks.pioneers.dto.RefreshDto;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.rest.AuthApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;

public class AuthenticationService {

    private final AuthApiService authApiService;
    private final ObjectMapper mapper;
    private final LoginResultStorage loginResultStorage;

    @Inject
    public AuthenticationService(AuthApiService authApiService, ObjectMapper mapper, LoginResultStorage loginResultStorage) {

        this.authApiService = authApiService;
        this.mapper = mapper;
        this.loginResultStorage = loginResultStorage;
    }

    public Observable<LoginResult> login(String username, String password) {

        return authApiService
                .login(new LoginDto(username, password))
                // authorize user with token and id
                .doOnNext(loginResultStorage::setLoginResult)
                .onErrorReturn(error -> {
                    // Login was not successful
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                        // some other error happened while login
                        return new LoginResult(Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, null, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR);
                    } else {
                        // return failed LoginResult with error message
                        return new LoginResult(errorResult, errorResult, Constants.LOGIN_ERROR, errorResult, errorResult, errorResult, null, errorResult, errorResult);
                    }
                });
    }

    public Observable<LoginResult> refresh() {
        return authApiService
                .refresh(new RefreshDto(loginResultStorage.getLoginResult().refreshToken()))
                .doOnNext(loginResultStorage::setLoginResult)
                .onErrorReturn(error -> {
                    // Login was not successful
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                        // some other error happened while login
                        return new LoginResult(Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR, null, Constants.LOGIN_ERROR, Constants.LOGIN_ERROR);
                    } else {
                        // return failed LoginResult with error message
                        return new LoginResult(errorResult, errorResult, Constants.LOGIN_ERROR, errorResult, errorResult, errorResult, null, errorResult, errorResult);
                    }
                });
    }

    public Observable<String> logout() {
        return authApiService
                .logout()
                .map(result -> Constants.LOGOUT_SUCCESS)
                .onErrorReturn(error -> {
                    // Logout was not successful
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    if (errorResult.equals(Constants.CUSTOM_ERROR)) {
                        // return failed Logout error message
                        return Constants.LOGOUT_ERROR + errorResult;
                    } else {
                        // some other error happened while logout
                        return Constants.LOGOUT_ERROR;
                    }
                });

    }

}
