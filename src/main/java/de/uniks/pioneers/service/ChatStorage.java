package de.uniks.pioneers.service;

import de.uniks.pioneers.controller.MessageSubController;
import de.uniks.pioneers.model.Group;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ChatStorage {

    private final List<Group> privateChats = new ArrayList<>();

    @Inject
    public ChatStorage() {

    }

    public void setPrivateChatToNextScreen(List<MessageSubController> privateChatSubControllers) {
        this.privateChats.clear();
        privateChatSubControllers.forEach(con -> this.privateChats.add(con.getGroup()));
    }

    public List<Group> getPrivateChats() {
        return this.privateChats;
    }
}
