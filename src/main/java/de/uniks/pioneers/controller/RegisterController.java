package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.UserService;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class RegisterController implements Controller {

    public final SimpleStringProperty username = new SimpleStringProperty();
    public final SimpleStringProperty password = new SimpleStringProperty();
    private final App app;
    private final UserService userService;
    private final Provider<LoginController> loginController;
    private final PrefService prefService;
    private final ResourceBundle resourceBundle;
    @FXML
    public ImageView registerGameLogoImageView;
    @FXML
    public TextField registerUsernameInput;
    @FXML
    public PasswordField registerPasswordInput;
    @FXML
    public PasswordField registerRepeatPasswordInput;
    @FXML
    public Label registerErrorLabel;
    @FXML
    public Label registerResponseErrorLabel;
    @FXML
    public Button registerUserRegisterButton;
    @FXML
    public Button registerBackToLoginButton;
    @FXML
    public ImageView avatarImageView;
    @FXML
    public Button beforeButton;
    @FXML
    public Button nextButton;
    private int avatarAddressIndex = 0;

    @Inject
    public RegisterController(App app, UserService userService, Provider<LoginController> loginController, PrefService prefService, ResourceBundle resourceBundle) {
        this.app = app;
        this.userService = userService;
        this.loginController = loginController;
        this.prefService = prefService;
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {

        // Load registration screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/registration.fxml"), resourceBundle);
        loader.setControllerFactory(c -> this);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Set title for screen
        app.setScreenTitle(resourceBundle.getString(Constants.REGISTER_SCREEN_TITLE));

        //Avatar add
        avatarImageView.setImage(new Image(Constants.AVATAR_LIST.get(avatarAddressIndex)));

        Image iconLeft = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/arrow_back.png")));
        Image iconRight = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/arrow_forward.png")));
        ImageView imgLeft = new ImageView(iconLeft);
        ImageView imgRight = new ImageView(iconRight);
        imgLeft.setFitHeight(30);
        imgLeft.setFitWidth(50);
        imgRight.setFitHeight(30);
        imgRight.setFitWidth(50);
        beforeButton.setGraphic(imgLeft);
        nextButton.setGraphic(imgRight);

        // Bind username and password
        registerUsernameInput.textProperty().bindBidirectional(username);
        registerPasswordInput.textProperty().bindBidirectional(password);

        // Binding password fields, true if both inputs are equal
        final BooleanBinding match = Bindings.equal(registerPasswordInput.textProperty(), registerRepeatPasswordInput.textProperty());
        registerErrorLabel.textProperty().bind(
                Bindings.when(match)
                        .then("")
                        .otherwise(resourceBundle.getString("nomatch.password"))
        );

        // Disable register button if password fields do not match or username field is empty or password field is empty
        final BooleanBinding empty = Bindings.isEmpty(registerUsernameInput.textProperty())
                .or(Bindings.isEmpty(registerPasswordInput.textProperty()));
        registerUserRegisterButton.disableProperty().bind(empty.or(match.not()));

        registerRepeatPasswordInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                register(new ActionEvent());
            }
        });

        return parent;
    }

    // On register button clicked
    public void register(ActionEvent actionevent) {
        final LoginController controller = loginController.get();
        String username = registerUsernameInput.getText();
        String password = registerPasswordInput.getText();
        //String avatarAddress = avatarAddressList.get(avatarAddressIndex);
        String avatarAddress = Constants.AVATAR_LIST.get(avatarAddressIndex);

        // Check that password is valid
        if (this.userService.passwordValidation(password)) {
            // Register user on server
            userService.registerUser(username, password, avatarAddress)
                    .observeOn(Constants.FX_SCHEDULER)
                    .subscribe(result -> {
                        // If registration is successful show an alert to inform the user
                        if (result.equals(Constants.REGISTRATION_SUCCESS)) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, resourceBundle.getString("change.to.login"));
                            alert.initOwner(app.getPrimaryStage());
                            alert.setHeaderText(result);
                            alert.showAndWait()
                                    .ifPresent(buttonType -> {
                                        // Set username and password and take it to login screen
                                        prefService.setUser("");
                                        prefService.setPassword("");
                                        prefService.setFlag(false);

                                        controller.username.set(username);
                                        controller.password.set(password);
                                        // Change to login screen
                                        app.show(controller);
                                    });
                        } else {
                            // Show error message in label
                            registerResponseErrorLabel.setText(result);
                        }
                    });
        } else {
            this.registerResponseErrorLabel.setText(resourceBundle.getString(Constants.PASSWORD_VALIDATION_ERROR));
        }
        actionevent.consume();
    }

    // On return button clicked
    public void backToLogin() {
        // Change view to log in screen
        app.show(loginController.get());
    }

    public void beforeAvatar() {
        avatarAddressIndex = Math.floorMod(avatarAddressIndex - 1, 7);
        avatarImageView.setImage(new Image(Constants.AVATAR_LIST.get(avatarAddressIndex)));
    }

    public void nextAvatar() {
        avatarAddressIndex = Math.floorMod(avatarAddressIndex + 1, 7);
        avatarImageView.setImage(new Image(Constants.AVATAR_LIST.get(avatarAddressIndex)));
    }
}
