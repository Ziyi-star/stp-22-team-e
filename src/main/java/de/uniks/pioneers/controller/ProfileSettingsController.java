package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.service.EncryptionService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.UserService;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ProfileSettingsController implements Controller {
    private final ToggleGroup languages = new ToggleGroup();
    private final App app;
    private final UserService userService;
    private final LoginResultStorage loginResultStorage;
    private final PrefService prefService;
    private final EncryptionService encryptionService;
    private final ResourceBundle resourceBundle;
    private final Provider<LoginController> loginControllerProvider;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    @FXML
    public CheckBox checkbox_name;
    @FXML
    public TextField textfield_name;
    @FXML
    public CheckBox checkbox_password;
    @FXML
    public PasswordField textfield_password;
    @FXML
    public PasswordField textfield_password_repeat;
    @FXML
    public CheckBox checkbox_avatar;
    @FXML
    public Button arrow_left_avatar;
    @FXML
    public ImageView imageview_avatar;
    @FXML
    public Button arrow_right_avatar;
    @FXML
    public RadioButton radio_english;
    @FXML
    public RadioButton radio_german;
    @FXML
    public Button btn_apply;
    @FXML
    public Label error_Label;
    @FXML
    public Label match_label;
    private int avatarAddressIndex;
    private Node blurBox;
    private Boolean rememberPreference;
    private String fxmlPath;

    private Window window;

    @Inject
    public ProfileSettingsController(App app, UserService userService, LoginResultStorage loginResultStorage, PrefService prefService, EncryptionService encryptionService, ResourceBundle resourceBundle, Provider<LoginController> loginControllerProvider, Provider<LobbySelectController> lobbySelectControllerProvider) {
        this.app = app;
        this.userService = userService;
        this.loginResultStorage = loginResultStorage;
        this.prefService = prefService;
        this.encryptionService = encryptionService;
        this.resourceBundle = resourceBundle;
        this.loginControllerProvider = loginControllerProvider;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {
        if (blurBox != null) {
            blurBox.setEffect(null);
        }
    }

    @Override
    public Parent render() {
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlPath), resourceBundle);
        fxmlLoader.setControllerFactory(c -> this);
        Parent parent = null;
        try {
            BoxBlur blur = new BoxBlur(3, 3, 3);
            if (blurBox != null) {
                blurBox.setEffect(blur);
            }

            //load Dialog Window
            DialogPane loader = fxmlLoader.load();
            parent = loader;
            if (blurBox != null) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader);
                if (fxmlPath.equals("views/profileSettings.fxml")) {
                    dialog.setTitle(resourceBundle.getString("profile.settings"));
                } else {
                    dialog.setTitle(resourceBundle.getString("language.settings"));
                }
                dialog.initOwner(app.getPrimaryStage());
                dialog.show();
                window = dialog.getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event -> {
                    dialog.setResult(ButtonType.CLOSE);
                    this.destroy();
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageview_avatar != null) {
            if (loginResultStorage.getLoginResult().avatar() != null) {
                avatarAddressIndex = Constants.AVATAR_LIST.indexOf(loginResultStorage.getLoginResult().avatar());
            } else {
                avatarAddressIndex = 0;
            }
            if (avatarAddressIndex != -1) {
                imageview_avatar.setImage(new Image(Constants.AVATAR_LIST.get(avatarAddressIndex)));
            } else {
                imageview_avatar.setImage(new Image(Constants.AVATAR_LIST.get(0)));
            }

            textfield_name.setDisable(true);
            textfield_password.setDisable(true);
            textfield_password_repeat.setDisable(true);
            arrow_left_avatar.setDisable(true);
            arrow_right_avatar.setDisable(true);

            rememberPreference = prefService.getFlag();

            // show an error when password do not match
            final BooleanBinding match = Bindings.equal(textfield_password.textProperty(), textfield_password_repeat.textProperty());
            match_label.textProperty().bind(
                    Bindings.when(match)
                            .then("")
                            .otherwise(resourceBundle.getString("nomatch.password"))
            );
        }

        //create toggle group
        radio_english.setToggleGroup(languages);
        radio_german.setToggleGroup(languages);

        if (Locale.getDefault() == Locale.GERMAN || Locale.getDefault() == Locale.GERMANY) {
            radio_german.fire();
        } else {
            radio_english.fire();
        }

        return parent;
    }

    public void applySettings(ActionEvent actionEvent) {
        // reset error label
        error_Label.setText("");
        // set the variable if the according checkbox is selected
        String name = !checkbox_name.isSelected() ? null : textfield_name.getText();
        String avatar = !checkbox_avatar.isSelected() ? null : Constants.AVATAR_LIST.get(avatarAddressIndex);
        String password = !checkbox_password.isSelected() ? null : (textfield_password.getText().equals(textfield_password_repeat.getText()) ? textfield_password.getText() : null);

        // only language is changed when no checkbox is selected
        if (!checkbox_name.isSelected() && !checkbox_avatar.isSelected() && !checkbox_password.isSelected()) {
            // change language
            changeLanguage();
            // get a handle to the stage
            Stage stage = (Stage) btn_apply.getScene().getWindow();
            // close profile settings menu
            stage.close();
            this.destroy();
            app.show(lobbySelectControllerProvider.get());
            actionEvent.consume();
            return;
        }

        // True if password is null or check that password has more than 7 characters
        boolean passwordValidation = password == null || this.userService.passwordValidation(password);

        // Check that password is valid
        if (passwordValidation) {
            // check if inputs are valid
            boolean valid;
            // no empty name
            boolean validName = !Objects.equals(name, "");
            valid = validName;
            // matching password
            boolean validPassword = (password != null && checkbox_password.isSelected()) || !checkbox_password.isSelected();
            valid &= validPassword;

            if (valid) {
                // update user with new data
                userService.patchUser(name, avatar, password)
                        .observeOn(Constants.FX_SCHEDULER)
                        .subscribe(result -> {
                            if (result.status().equals(Constants.STATUS_ONLINE)) {
                                LoginResult loginRes = loginResultStorage.getLoginResult();
                                // update LoginResultStorage with new values
                                loginResultStorage.setLoginResult(new LoginResult(loginRes.createdAt(), loginRes.updatedAt(), loginRes._id(), result.name(), result.status(), result.avatar(), loginRes.friends(), loginRes.accessToken(), loginRes.refreshToken()));
                                // update remember me
                                if (rememberPreference) {
                                    if (checkbox_name.isSelected()) {
                                        prefService.setUser(name);
                                    }
                                    if (password != null && checkbox_password.isSelected()) {
                                        prefService.setPassword(encryptionService.encryptMe(password));
                                    }
                                }
                                // success alert
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, resourceBundle.getString("change.to.lobby.select"));
                                alert.initOwner(window);
                                alert.setHeaderText(resourceBundle.getString("settings.changed"));
                                alert.showAndWait().ifPresent(buttonType -> {
                                    this.destroy();
                                    app.show(lobbySelectControllerProvider.get());
                                });
                                // change language
                                changeLanguage();
                                // get a handle to the stage
                                Stage stage = (Stage) btn_apply.getScene().getWindow();
                                // close profile settings menu
                                stage.close();
                            } else {
                                error_Label.setText(result._id());
                            }

                        });
            } else {
                if (!validName) {
                    error_Label.setText(resourceBundle.getString("empty.name"));
                }
            }
        } else {
            error_Label.setText(resourceBundle.getString(Constants.PASSWORD_VALIDATION_ERROR));
        }
        actionEvent.consume();
    }

    public void changeName(ActionEvent actionEvent) {
        // disable name field if checkbox is not selected
        if (((CheckBox) actionEvent.getSource()).isSelected()) {
            textfield_name.setDisable(false);
        } else {
            textfield_name.setDisable(true);
            textfield_name.setText("");
        }
    }

    public void changePassword(ActionEvent actionEvent) {
        // disable password fields if checkbox is not selected
        if (((CheckBox) actionEvent.getSource()).isSelected()) {
            textfield_password.setDisable(false);
            textfield_password_repeat.setDisable(false);
        } else {
            textfield_password.setDisable(true);
            textfield_password.setText("");
            textfield_password_repeat.setDisable(true);
            textfield_password_repeat.setText("");
        }
    }

    public void changeAvatar(ActionEvent actionEvent) {
        // disable the avatar change buttons if checkbox is not selected
        if (((CheckBox) actionEvent.getSource()).isSelected()) {
            imageview_avatar.setDisable(false);
            arrow_right_avatar.setDisable(false);
            arrow_left_avatar.setDisable(false);
        } else {
            imageview_avatar.setDisable(true);
            arrow_right_avatar.setDisable(true);
            arrow_left_avatar.setDisable(true);
        }
    }

    public void nextAvatar(ActionEvent actionEvent) {
        avatarAddressIndex = Math.floorMod(avatarAddressIndex + 1, Constants.AVATAR_LIST.size());
        imageview_avatar.setImage(new Image(Constants.AVATAR_LIST.get(avatarAddressIndex)));
        actionEvent.consume();
    }

    public void prevAvatar(ActionEvent actionEvent) {
        avatarAddressIndex = Math.floorMod(avatarAddressIndex - 1, Constants.AVATAR_LIST.size());
        imageview_avatar.setImage(new Image(Constants.AVATAR_LIST.get(avatarAddressIndex)));
        actionEvent.consume();
    }

    public void setBlurBox(Node blurBox) {
        this.blurBox = blurBox;
    }

    public void setFxml(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public void changeLanguage() {
        if (languages.getSelectedToggle() == radio_english) {
            Locale.setDefault(Locale.ENGLISH);
        } else if (languages.getSelectedToggle() == radio_german) {
            Locale.setDefault(Locale.GERMAN);
        }
        prefService.setLocale(Locale.getDefault());
    }

    public void applyLanguage(ActionEvent actionEvent) {
        changeLanguage();
        // refresh scene
        app.show(loginControllerProvider.get());
        // get a handle to the stage
        Stage stage = (Stage) ((Button) actionEvent.getSource()).getScene().getWindow();
        // close profile settings menu
        stage.close();
        this.destroy();
    }
}
