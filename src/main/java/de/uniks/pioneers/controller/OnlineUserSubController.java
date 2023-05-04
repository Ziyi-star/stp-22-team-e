package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.UserService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javax.inject.Inject;

public class OnlineUserSubController implements Controller {

    private final UserService userService;
    private final EventListener eventListener;
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private TableView<User> onlineUserList;
    private TableColumn<User, String> userNameColumn;
    private Disposable disposable;

    @Inject
    public OnlineUserSubController(UserService userService, EventListener eventListener) {
        this.userService = userService;
        this.eventListener = eventListener;
    }

    @Override
    public void init() {
        // Get all online users
        this.userService.findAllOnlineUsers().observeOn(Constants.FX_SCHEDULER).subscribe(this.users::setAll);
        disposable = this.eventListener
                .listen("users.*.*", User.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(resultEvent -> {
                    final User user = resultEvent.data();
                    // Remove user from user list
                    if (resultEvent.event().endsWith(".deleted")) {
                        users.removeIf(u -> u._id().equals(user._id()));
                    } else if (resultEvent.event().endsWith(".updated")) {
                        // If user online add to user list else remove
                        if (user.status().equals(Constants.STATUS_ONLINE)) {
                            if (users.stream().noneMatch(u -> user._id().equals(u._id()))) {
                                users.add(user);
                            } else {
                                users.replaceAll(u -> u._id().equals(user._id()) ? user : u);
                            }
                        } else {
                            users.removeIf(u -> u._id().equals(user._id()));
                        }
                    }
                });
    }

    @Override
    public void destroy() {
        this.disposable.dispose();
    }

    @Override
    public Parent render() {

        // Decide which value you want to show
        this.userNameColumn.setCellValueFactory(name -> new SimpleStringProperty(name.getValue().name()));
        this.userNameColumn.setText("Name");
        this.userNameColumn.setPrefWidth(350);

        this.users.addListener((ListChangeListener<? super User>) c -> onlineUserList.getItems().setAll(c.getList().stream().toList()));

        return onlineUserList;
    }

    public void setView(TableView<User> onlineUserList, TableColumn<User, String> userNameColumn) {
        this.onlineUserList = onlineUserList;
        this.userNameColumn = userNameColumn;
    }

}
