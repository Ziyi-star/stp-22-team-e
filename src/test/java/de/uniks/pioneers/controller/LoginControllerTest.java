package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.service.AuthenticationService;
import de.uniks.pioneers.service.PrefService;
import io.reactivex.rxjava3.core.Observable;
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
import org.testfx.matcher.control.LabeledMatchers;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class LoginControllerTest extends ApplicationTest {
    @Mock
    App app;

    @Mock
    AuthenticationService authService;

    @Mock
    PrefService prefService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    LoginController loginController;

    @Override
    public void start(Stage stage) {
        when(prefService.getLocale()).thenReturn(Locale.GERMAN);
        new App(loginController).start(stage);
        stage.setHeight(500);
        stage.setWidth(500);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        loginController = null;
    }

    @Test
    void login() {
        // (1) Try to log in with non-existing user

        when(authService.login(anyString(), anyString())).thenReturn(Observable.just(new LoginResult(
                Constants.INVALID_USERNAME,
                Constants.INVALID_USERNAME,
                Constants.LOGIN_ERROR,
                Constants.INVALID_USERNAME,
                Constants.INVALID_USERNAME,
                Constants.INVALID_USERNAME,
                null,
                Constants.INVALID_USERNAME,
                Constants.INVALID_USERNAME)));

        write("\t");
        write("Joe\t");
        write("NonExisting\t");
        write("\t");

        // Check that error label is still empty at this moment
        FxAssert.verifyThat("#label_LoginAndTitleScreen_Password_Incorrect", LabeledMatchers.hasText(""));

        // Click on login button
        type(KeyCode.ENTER);

        // Check that error label has invalid username text
        FxAssert.verifyThat("#label_LoginAndTitleScreen_Password_Incorrect", LabeledMatchers.hasText(Constants.INVALID_USERNAME));
    }

    @Test
    void leave() {
        clickOn("#leave_button");
        verify(app).stop();
    }
}