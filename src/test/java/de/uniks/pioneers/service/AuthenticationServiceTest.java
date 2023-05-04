package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.LoginDto;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.rest.AuthApiService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Spy
    LoginResultStorage loginResultStorage;

    @Spy
    ObjectMapper mapper;

    @Mock
    AuthApiService authApiService;

    @InjectMocks
    AuthenticationService authService;

    @Test
    void login() {
        // ---- Successful login of user ----

        when(authApiService.login(any()))
                .thenReturn(Observable.just(new LoginResult(null, null, "1", "Rainer69", Constants.STATUS_OFFLINE, null, null, "123", "456")));

        // Login of a user
        LoginResult loginResult = authService.login("Rainer69", "Rainer123").blockingFirst();

        // Check for existing token
        assertEquals("123", loginResultStorage.getLoginResult().accessToken());

        // Check for successful login
        assertEquals("Rainer69", loginResult.name());

        verify(authApiService).login(new LoginDto("Rainer69", "Rainer123"));

        // ---- Error ----

        // (1) Create error that is not a http error
        when(authApiService.login(any())).thenReturn(Observable.error(new Throwable()));
        final LoginResult errorResult = authService.login("Rainer69", "Rainer69").blockingFirst();

        // Check for login error
        assertEquals(Constants.LOGIN_ERROR, errorResult.name());

        // (2) Create http error with no message

        // Create error response body with no message
        final String errorResponse = "{" + "\"statusCode\": 400," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoMsg = ResponseBody.create(errorResponse, MediaType.parse("application/json"));

        // authApiService returns http error when invoked by authService
        when(authApiService.login(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoMsg))));
        final LoginResult errorNoMessageResult = authService.login("Rainer69", "Rainer69").blockingFirst();

        // Check for login error
        assertEquals(Constants.LOGIN_ERROR, errorNoMessageResult.name());

        // (3) Create http error where response message is not an array

        // Error message
        final String customErrorMessage = "Custom Error Message";

        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"Custom Error Message\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));

        // authApiService returns http error when invoked by authService
        when(authApiService.login(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyNoArray))));
        final LoginResult errorMessageNoArray = authService.login("Rainer69", "Rainer69").blockingFirst();

        // Check that custom error message was returned
        assertEquals(customErrorMessage, errorMessageNoArray.name());

        // (4) Create an error where response message is an array, check that message extraction from array works

        final String errorResponseArray = "{" + "\"statusCode\": 400," + "\"message\": [\"Custom Error Message\"]," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyArray = ResponseBody.create(errorResponseArray, MediaType.parse("application/json"));

        // authApiService returns http error when invoked by authService
        when(authApiService.login(any())).thenReturn(Observable.error(new HttpException(Response.error(400, responseBodyArray))));
        final LoginResult errorMessageArray = authService.login("Rainer69", "Rainer69").blockingFirst();

        // Check that custom error message was returned from array
        assertEquals(customErrorMessage, errorMessageArray.name());

    }
}