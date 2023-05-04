package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.AuthenticationService;
import de.uniks.pioneers.service.EncryptionService;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.UserService;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class LoginController implements Controller {

    public final SimpleStringProperty username = new SimpleStringProperty();
    public final SimpleStringProperty password = new SimpleStringProperty();
    public final EncryptionService encryptionService;
    public final PrefService prefService;
    private final App app;
    private final AuthenticationService authService;
    private final UserService userService;
    private final Provider<RegisterController> registerController;
    private final Provider<LobbySelectController> lobbySelectController;
    private final Provider<ProfileSettingsController> profileSettingsControllerProvider;
    private final Provider<CreditsController> creditsController;
    private final ResourceBundle resourceBundle;
    @FXML
    public Button credits_button;
    @FXML
    public Slider slider;
    public ImageView imageview_settings;
    @FXML
    public TextField textField_LoginAndTitleScreen_Username_Input;
    @FXML
    public PasswordField passwordField_LoginAndTitleScreen_Password_Input;
    @FXML
    public Label label_LoginAndTitleScreen_Password_Incorrect;
    @FXML
    public CheckBox checkBox_LoginAndTitleScreen_RememberMe;
    @FXML
    public Button button_LoginAndTitleScreen_Login;
    @FXML
    public Button button_LoginAndTitleScreen_Register;
    @FXML
    public VBox root;
    private boolean rememberPreference;

    @Inject
    public LoginController(App app, AuthenticationService authService, UserService userService, Provider<RegisterController> registerController
            , Provider<LobbySelectController> lobbySelectController, EncryptionService encryptionService, PrefService prefService,
                           Provider<ProfileSettingsController> profileSettingsControllerProvider, Provider<CreditsController> creditsController,
                           ResourceBundle resourceBundle) {

        this.app = app;
        this.authService = authService;
        this.userService = userService;
        this.registerController = registerController;
        this.lobbySelectController = lobbySelectController;
        this.encryptionService = encryptionService;
        this.prefService = prefService;
        this.profileSettingsControllerProvider = profileSettingsControllerProvider;
        this.creditsController = creditsController;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void init() {
        Locale.setDefault(prefService.getLocale());
    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {

        // load login screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/loginAndTitleScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> this);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Bind username and password
        textField_LoginAndTitleScreen_Username_Input.textProperty().bindBidirectional(username);
        passwordField_LoginAndTitleScreen_Password_Input.textProperty().bindBidirectional(password);

        // Set login screen title
        app.setScreenTitle(resourceBundle.getString(Constants.LOGIN_SCREEN_TITLE));

        rememberPreference = prefService.getFlag();


        // load config if rememberMe preference is set
        if (rememberPreference) {
            textField_LoginAndTitleScreen_Username_Input.textProperty().set(prefService.getUser());
            passwordField_LoginAndTitleScreen_Password_Input.textProperty().set(encryptionService.decryptMe(prefService.getPassword()));
            checkBox_LoginAndTitleScreen_RememberMe.setSelected(true);
        }


        textField_LoginAndTitleScreen_Username_Input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login(new ActionEvent());
            }
        });

        passwordField_LoginAndTitleScreen_Password_Input.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                login(new ActionEvent());
            }
        });

        imageview_settings.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            openLanguageSettings();
            event.consume();
        });

        imageview_settings.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(800), imageview_settings);
            rotateTransition.setByAngle(180);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();
            event.consume();
        });
        return parent;
    }

    // login button is clicked
    public void login(ActionEvent actionEvent) {

        String username = textField_LoginAndTitleScreen_Username_Input.getText();
        String password = passwordField_LoginAndTitleScreen_Password_Input.getText();

        authService.login(username, password)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(result -> {

                    // login was not successful
                    if (result._id().equals(Constants.LOGIN_ERROR)) {

                        // show error on label
                        label_LoginAndTitleScreen_Password_Incorrect.setText(result.name());

                    } else {

                        // Login was successful
                        String id = result._id();

                        userService.setUserOnline(id, username, password)
                                .observeOn(Constants.FX_SCHEDULER)
                                .subscribe(res -> {

                                    // could not set user status to online
                                    if (!res.equals(Constants.STATUS_ONLINE)) {
                                        label_LoginAndTitleScreen_Password_Incorrect.setText(resourceBundle.getString(Constants.STATUS_ONLINE_FAILED));
                                    } else {

                                        // if Checkbox is set, write encrypted data to Preferences
                                        if (checkBox_LoginAndTitleScreen_RememberMe.isSelected()) {
                                            prefService.setUser(textField_LoginAndTitleScreen_Username_Input.getText());
                                            prefService.setPassword(encryptionService.encryptMe(passwordField_LoginAndTitleScreen_Password_Input.getText()));
                                            prefService.setFlag(true);

                                            // if Checkbox is not set, write default data to Preferences
                                        } else if (!checkBox_LoginAndTitleScreen_RememberMe.isSelected() && rememberPreference) {
                                            prefService.setUser("");
                                            prefService.setPassword("");
                                            prefService.setFlag(false);
                                        }

                                        // User status set to online, go to LobbySelectScreen
                                        final LobbySelectController controller = lobbySelectController.get();
                                        app.show(controller);
                                    }
                                });
                    }
                });
        actionEvent.consume();
    }

    // register button is clicked
    public void register() {

        final RegisterController controller = registerController.get();

        // Take username and password to register screen
        String usernameInputText = textField_LoginAndTitleScreen_Username_Input.getText();
        String passwordInputText = passwordField_LoginAndTitleScreen_Password_Input.getText();

        controller.username.set(usernameInputText);
        controller.password.set(passwordInputText);

        // Change view to register screen
        app.show(controller);

    }

    public void openLanguageSettings() {
        ProfileSettingsController profileSettingsController = profileSettingsControllerProvider.get();
        profileSettingsController.setFxml("views/settings.fxml");
        profileSettingsController.setBlurBox(root);
        profileSettingsController.render();
    }

    public void leave(ActionEvent actionEvent) {
        app.stop();
        actionEvent.consume();
    }

    public void credits(ActionEvent actionEvent) {
        final CreditsController controller = creditsController.get();
        controller.render();
        app.show(controller);
        actionEvent.consume();
    }
}
