package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.EncryptionService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.UserService;
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

import javax.inject.Provider;
import java.util.ResourceBundle;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProfileSettingsControllerTest extends ApplicationTest {

    @Mock
    App app;

    @Mock
    UserService userService;

    @Mock
    PrefService prefService;

    @Mock
    Provider<LobbySelectController> lobbySelectControllerProvider;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    ProfileSettingsController profileSettingsController;

    @Mock
    LoginResultStorage loginResultStorage;

    @Mock
    EncryptionService encryptionService;

    @Override
    public void start(Stage stage) {
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "sender_id", "Rainer", "5", Constants.AVATAR_LIST.get(0), null, "7", "8"));

        when(prefService.getFlag()).thenReturn(true);

        profileSettingsController.setFxml("views/profileSettings.fxml");

        new App(profileSettingsController).start(stage);
        // Stage width and height change for test
        stage.setWidth(400);
        stage.setHeight(700);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        profileSettingsController = null;
        lobbySelectControllerProvider = null;
    }


    @Test
    void changeName() {
        // Check that user lobby list has a lobby
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        write("\t");
        write("Testus");

        when(userService.patchUser("Testus", null, null)).thenReturn(Observable.just(new User("sender_id", "Testus", "online", Constants.AVATAR_LIST.get(0), null)));
        clickOn("#btn_apply");
        type(KeyCode.ENTER);

        verify(userService).patchUser("Testus", null, null);
    }

    @Test
    void changePassword() {
        write("\t");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        write("\t");
        write("Passwordus");
        write("\t");
        write("Passwordus");

        when(userService.patchUser(null, null, "Passwordus")).thenReturn(Observable.just(new User("sender_id", "Rainer", "online", Constants.AVATAR_LIST.get(0), null)));
        when(userService.passwordValidation(anyString())).thenReturn(true);
        clickOn("#btn_apply");
        type(KeyCode.ENTER);

        verify(userService).patchUser(null, null, "Passwordus");
        verify(userService).passwordValidation("Passwordus");
    }

    @Test
    void changePasswordFailed() {
        write("\t");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        write("\t");
        write("Passwor");
        write("\t");
        write("Passwor");
        clickOn("#btn_apply");

        FxAssert.verifyThat("#error_Label", LabeledMatchers.hasText(resourceBundle.getString(Constants.PASSWORD_VALIDATION_ERROR)));
    }

    @Test
    void changeAvatar() {
        write("\t");
        write("\t");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        write("\t");
        type(KeyCode.ENTER);
        write("\t");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);

        when(userService.patchUser(null, Constants.AVATAR_LIST.get(3), null)).thenReturn(Observable.just(new User("sender_id", "Testus", "online", Constants.AVATAR_LIST.get(3), null)));
        clickOn("#btn_apply");
        type(KeyCode.ENTER);

        verify(userService).patchUser(null, Constants.AVATAR_LIST.get(3), null);
    }
}
