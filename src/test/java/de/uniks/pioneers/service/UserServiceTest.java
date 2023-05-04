package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateUserDto;
import de.uniks.pioneers.dto.UpdateUserDto;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.rest.UserApiService;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Spy
    ObjectMapper mapper;

    @Mock
    UserApiService userApiService;

    @Mock
    LoginResultStorage loginResult;

    @InjectMocks
    UserService userService;

    @Test
    void registerUser() {
        // Success case
        // Return an observable user when used user api service
        when(userApiService.create(any())).thenReturn(Observable.just(new User("1", "Rick", "offline", null, null)));

        // Register an normal user
        final String resultSuccess = userService.registerUser("Rick", "12", "http://12345678.png").blockingFirst();

        // Check that registration was successful
        assertEquals(Constants.REGISTRATION_SUCCESS, resultSuccess);

        // Verify that create method was used
        verify(userApiService).create(new CreateUserDto("Rick", "http://12345678.png", "12"));

        // Error case
        // Create error message
        final String errMsg = "That's an error";

        // Response with an error that is not a http exception when used user api service
        // and try to register a user
        when(userApiService.create(any())).thenReturn(Observable.error(new Throwable()));
        final String errorResult = userService.registerUser("Rick", "12", "http://12345678.png\"").blockingFirst();

        // Check that you get a registration error message
        assertEquals(Constants.CUSTOM_ERROR, errorResult);

        // Create an error response body with no message
        final String errorResponseNoMsg = "{" + "\"statusCode\": 400," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoMsg = ResponseBody.create(errorResponseNoMsg, MediaType.parse("application/json"));

        // Response with an error that is a http exception and try to register a user
        when(userApiService.create(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoMsg))));
        final String errorResultNoMsg = userService.registerUser("Rick", "12", "http://12345678.png").blockingFirst();

        // Check that you get a registration error message
        assertEquals(Constants.CUSTOM_ERROR, errorResultNoMsg);

        // Create an error response with message with no array
        // and try to register a user
        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"That's an error\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));

        when(userApiService.create(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoArray))));
        final String errorResultNoArray = userService.registerUser("Rick", "12", "http://12345678.png").blockingFirst();

        // Check that you get the error message from response
        assertEquals(errMsg, errorResultNoArray);

        // Create an error response with message in array
        // and try to register a user
        final String errorResponseArray = "{" + "\"statusCode\": 400," + "\"message\": [\"That's an error\"]," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyArray = ResponseBody.create(errorResponseArray, MediaType.parse("application/json"));

        when(userApiService.create(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyArray))));
        final String errorResultArray = userService.registerUser("Rick", "12", "http://12345678.png").blockingFirst();

        // Check that you get the error message from response
        assertEquals(errMsg, errorResultArray);
    }

    @Test
    void setUserOnline() {

        // ---- Success ----

        // (1) Successful return from server after setting status to online
        when(userApiService.patchUser(anyString(), any())).thenReturn(Observable.just(new User("1234", "Joe", Constants.STATUS_ONLINE, null, null)));

        // set user status online
        final String userStatusOnline = userService.setUserOnline("1234", "Joe", "Irrelevant").blockingFirst();

        // Check that user status is online
        assertEquals(Constants.STATUS_ONLINE, userStatusOnline);

        // Verify that setStatus method from userApiService was used
        verify(userApiService).patchUser("1234", new UpdateUserDto("Joe", Constants.STATUS_ONLINE, null, null, "Irrelevant"));


        // ---- Error ----

        // (2) Server response is any error instead of status 'online'
        when(userApiService.patchUser(anyString(), any())).thenReturn(Observable.error(new Throwable()));

        // try to set user status to online
        final String errorResponse = userService.setUserOnline("1234", "Joe", "Irrelevant").blockingFirst();

        // Check that any error was thrown
        assertNotEquals(Constants.STATUS_ONLINE, errorResponse);

        // (3) HTTP error

        // build error body
        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"Custom Error Message\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));

        // server response is http error
        when(userApiService.patchUser(anyString(), any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoArray))));

        // try to set user status to online
        final String httpErrorResponse = userService.setUserOnline("456", "Hans", "Hans52").blockingFirst();

        // check that http error was thrown
        assertNotEquals(Constants.STATUS_ONLINE, httpErrorResponse);
    }

    @Test
    void passwordNotLongEnough() {
        // Check that password validation return false if password is not long enough
        boolean passwordValidation = userService.passwordValidation("1234567");
        assertFalse(passwordValidation);
    }

    @Test
    void passwordLongEnough() {
        // Check that password validation returns true if password is long enough
        boolean passwordValidation = userService.passwordValidation("12345678");
        assertTrue(passwordValidation);
    }

    @Test
    void findAllUser() {

        // on success
        when(userApiService.getUserList(null, null)).thenReturn(Observable.just(List.of(new User("1", "User", "online", null, null),
                new User("2", "User2", "offline", null, null))));
        final List<User> allUser = userService.getAllUsers().blockingFirst();

        assertEquals(allUser.size(), 2);

        //on error
        when(userApiService.getUserList(null, null)).thenReturn(Observable.error(new Throwable()));
        final List<User> errorResponse = userService.getAllUsers().blockingFirst();

        assertEquals(Constants.CUSTOM_ERROR, errorResponse.get(0)._id());
    }

    @Test
    void findAllOnlineUser() {

        // on success
        when(userApiService.getUserList(Constants.STATUS_ONLINE, null)).thenReturn(Observable.just(List.of(new User("1", "User", "online", null, null),
                new User("2", "User2", "online", null, null))));
        final List<User> allUser = userService.findAllOnlineUsers().blockingFirst();

        assertEquals(allUser.size(), 2);

        //on error
        when(userApiService.getUserList(Constants.STATUS_ONLINE, null)).thenReturn(Observable.error(new Throwable()));
        final List<User> errorResponse = userService.findAllOnlineUsers().blockingFirst();

        assertEquals(Constants.CUSTOM_ERROR, errorResponse.get(0)._id());
    }

    @Test
    void patchUser() {
        //on success
        when(loginResult.getLoginResult()).thenReturn(new LoginResult("1", "1", "1", "User", "online", null, null, "1", "2"));
        when(userApiService.patchUser(anyString(), any())).thenReturn(Observable.just(new User("1", "UserPatch", Constants.STATUS_ONLINE, null, null)));
        final User user = userService.patchUser("UserPatch", null, null).blockingFirst();

        assertEquals(user.name(), "UserPatch");

        //on error
        when(userApiService.patchUser(anyString(), any())).thenReturn(Observable.error(new Throwable()));

        final User errorResponse = userService.patchUser("UserPatch", null, null).blockingFirst();

        assertEquals(errorResponse.name(), Constants.CUSTOM_ERROR);

    }

    @Test
    void getUser() {
        //on success
        when(userApiService.getUser(anyString())).thenReturn(Observable.just(new User("1", "UserGet", Constants.STATUS_ONLINE, null, null)));
        final User user = userService.getUser("1").blockingFirst();

        assertEquals(user.name(), "UserGet");

        //on error
        when(userApiService.getUser(anyString())).thenReturn(Observable.error(new Throwable()));

        final User errorResponse = userService.getUser("1").blockingFirst();

        assertEquals(errorResponse._id(), Constants.CUSTOM_ERROR);
    }
}