package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateUserDto;
import de.uniks.pioneers.dto.UpdateUserDto;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.rest.UserApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final UserApiService userApiService;
    private final ObjectMapper mapper;
    private final LoginResultStorage loginResultStorage;

    @Inject
    public UserService(UserApiService userApiService, ObjectMapper mapper, LoginResultStorage loginResultStorage) {

        this.userApiService = userApiService;
        this.mapper = mapper;
        this.loginResultStorage = loginResultStorage;
    }

    public Observable<String> registerUser(String username, String password, String avatarAddress) {
        // Register user
        return this.userApiService.create(new CreateUserDto(username, avatarAddress, password))
                .map(user -> Constants.REGISTRATION_SUCCESS)
                .onErrorReturn(error -> new ErrorHandling().handleError(error, mapper));
    }

    public Observable<String> setUserOnline(String _id, String username, String password) {
        return this.userApiService.patchUser(_id, new UpdateUserDto(username, Constants.STATUS_ONLINE, null, null, password))
                .map(User::status)
                .onErrorReturn(error -> new ErrorHandling().handleError(error, mapper));
    }

    public Observable<String> setUserOffline() {

        return this.userApiService.patchUser(loginResultStorage.getLoginResult()._id(), new UpdateUserDto(null, Constants.STATUS_OFFLINE, null, null, null))
                .map(User::status)
                .onErrorReturn(error -> new ErrorHandling().handleError(error, mapper));
    }

    public Observable<User> patchUser(String name, String avatar, String password) {
        return this.userApiService.patchUser(loginResultStorage.getLoginResult()._id(), new UpdateUserDto(name, null, avatar, null, password))
                .onErrorReturn(error -> {
                    String errorMsg = new ErrorHandling().handleError(error, mapper);
                    return new User(errorMsg, errorMsg, errorMsg, errorMsg, null);
                });
    }

    public Observable<List<User>> findAllOnlineUsers() {
        return this.userApiService.getUserList(Constants.STATUS_ONLINE, null).onErrorReturn(error -> {
            String errorMsg = new ErrorHandling().handleError(error, mapper);
            List<User> list = new ArrayList<>();
            list.add(new User(errorMsg, errorMsg, errorMsg, errorMsg, null));
            return list;
        });
    }

    public Observable<List<User>> getAllUsers() {
        return this.userApiService.getUserList(null, null).onErrorReturn(error -> {
            String errorMsg = new ErrorHandling().handleError(error, mapper);
            List<User> list = new ArrayList<>();
            list.add(new User(errorMsg, errorMsg, errorMsg, errorMsg, null));
            return list;
        });
    }

    public String getUserId() {
        return loginResultStorage.getLoginResult()._id();
    }

    public Observable<User> getUser(String _id) {

        return userApiService.getUser(_id).onErrorReturn(error -> {
            // getUser was not successful
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new User(errorResult, null, null, null, null);
        });
    }

    public String getUserName() {
        return loginResultStorage.getLoginResult().name();
    }

    public boolean passwordValidation(String password) {
        // Return true if password is longer than 7 characters
        return password.length() >= 8;
    }
}
