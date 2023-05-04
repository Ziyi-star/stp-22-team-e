package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.service.UserService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;

import javax.inject.Provider;
import java.util.ResourceBundle;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterControllerTest extends ApplicationTest {

    @Spy
    Provider<LoginController> loginControllerProvider;

    @Mock
    App app;

    @Mock
    UserService userService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    RegisterController registerController;

    @Override
    public void start(Stage stage) {
        new App(registerController).start(stage);
        // Stage width and height change for test
        stage.setWidth(700);
        stage.setHeight(700);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        registerController = null;
    }

    @Test
    void register() {
        // Error case
        when(userService.registerUser(anyString(), anyString(), anyString())).thenReturn(Observable.just(Constants.CUSTOM_ERROR));
        when(userService.passwordValidation(anyString())).thenReturn(true);

        // Check that register button is disabled
        FxAssert.verifyThat("#registerUserRegisterButton", NodeMatchers.isDisabled());

        // Write name and password
        write("\t");
        write("\t");
        write("Rick\t");
        write("12345678\t");

        // Check that register button is still disabled and error label has text
        FxAssert.verifyThat("#registerErrorLabel", LabeledMatchers.hasText(resourceBundle.getString("nomatch.password")));
        FxAssert.verifyThat("#registerUserRegisterButton", NodeMatchers.isDisabled());

        // Repeat password
        write("12345678\t");

        // Check that register button is enabled and error labels are empty
        FxAssert.verifyThat("#registerErrorLabel", LabeledMatchers.hasText(""));
        FxAssert.verifyThat("#registerResponseErrorLabel", LabeledMatchers.hasText(""));
        FxAssert.verifyThat("#registerUserRegisterButton", NodeMatchers.isEnabled());

        // Click register button
        type(KeyCode.ENTER);

        // Check that response error label has text
        FxAssert.verifyThat("#registerResponseErrorLabel", LabeledMatchers.hasText(Constants.CUSTOM_ERROR));

        // Verify that register user is used and password validation
        verify(userService).registerUser("Rick", "12345678", Constants.AVATAR_LIST.get(0));
        verify(userService).passwordValidation("12345678");

        // Success case
        when(userService.registerUser(anyString(), anyString(), anyString())).thenReturn(Observable.just(Constants.REGISTRATION_SUCCESS));

        // Click register button
        type(KeyCode.ENTER);

        // Check Alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(resourceBundle.getString("change.to.login")), LabeledMatchers.hasText(resourceBundle.getString("change.to.login")));

        // Check that register is disabled if there is no username but correct password
        TextField username = lookup("#registerUsernameInput").query();
        username.clear();
        FxAssert.verifyThat("#registerUserRegisterButton", NodeMatchers.isDisabled());
    }

    @Test
    void passwordValidationError() {
        // Password validation returns false
        when(userService.passwordValidation(anyString())).thenReturn(false);

        // Write name and password
        write("\t");
        write("\t");
        write("Rick\t");
        write("123\t");
        write("123\t");

        // Click register button
        type(KeyCode.ENTER);

        // Check that response error label has password validation error text
        FxAssert.verifyThat("#registerResponseErrorLabel", LabeledMatchers.hasText(resourceBundle.getString(Constants.PASSWORD_VALIDATION_ERROR)));

        // Verify that password validation was used
        verify(userService).passwordValidation("123");
    }
}