package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.controller.MessageSubController;
import de.uniks.pioneers.dto.CreateGroupDto;
import de.uniks.pioneers.dto.CreateMessageDto;
import de.uniks.pioneers.dto.UpdateGroupDto;
import de.uniks.pioneers.dto.UpdateMessageDto;
import de.uniks.pioneers.model.Group;
import de.uniks.pioneers.model.Message;
import de.uniks.pioneers.rest.GroupApiService;
import de.uniks.pioneers.rest.MessageApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ChatService {

    private final GroupApiService groupApiService;
    private final ObjectMapper mapper;
    private final LoginResultStorage loginResultStorage;
    private final MessageApiService messageApiService;
    private final ChatStorage chatStorage;

    @Inject
    public ChatService(GroupApiService groupApiService, ObjectMapper mapper, LoginResultStorage loginResultStorage, MessageApiService messageApiService, ChatStorage chatStorage) {

        this.groupApiService = groupApiService;
        this.mapper = mapper;
        this.loginResultStorage = loginResultStorage;
        this.messageApiService = messageApiService;
        this.chatStorage = chatStorage;
    }

    public Observable<Group> createGroup(String name, String id) {
        List<String> members = new ArrayList<>();
        members.add(id);
        if (!loginResultStorage.getLoginResult()._id().equals(id)) {
            members.add(loginResultStorage.getLoginResult()._id());
        }

        String bothNames = name + loginResultStorage.getLoginResult().name();
        if (bothNames.length() > 29) {
            String groupName = bothNames.substring(0, 30) + "..";
            return this.groupApiService.createGroup(new CreateGroupDto(groupName, members))
                    .onErrorReturn(error -> {
                        String resultError = new ErrorHandling().handleError(error, mapper);
                        return new Group(Constants.CUSTOM_ERROR, resultError, resultError, resultError, members);
                    });
        } else {
            return this.groupApiService.createGroup(new CreateGroupDto(bothNames, members))
                    .onErrorReturn(error -> {
                        String resultError = new ErrorHandling().handleError(error, mapper);
                        return new Group(Constants.CUSTOM_ERROR, resultError, resultError, resultError, members);
                    });

        }
    }

    public Observable<List<Group>> getGroups(String members) {
        return this.groupApiService.getGroupsList(members);
    }

    public Observable<Group> getGroup(String id) {
        return this.groupApiService.getGroup(id);
    }

    // Get group with you and a different member
    public Observable<List<Group>> getSpecificGroups(String members) {
        return this.groupApiService.getGroupsList(members + "," + loginResultStorage.getLoginResult()._id());
    }

    public Observable<Group> updateGroup(Group group) {
        return this.groupApiService.setGroup(group._id(), new UpdateGroupDto(group.name(), group.members()));
    }

    // Set private chats in chat storage to take them to the next screen
    public void setPrivateChatToNextScreen(List<MessageSubController> privateChatSubControllers) {
        this.chatStorage.setPrivateChatToNextScreen(privateChatSubControllers);
    }

    // Get your private after screen changed to open tabs with it
    public List<Group> getPrivateChats() {
        return this.chatStorage.getPrivateChats();
    }

    public Observable<List<Message>> getAllMessages(String groupId, String pattern) {
        return this.messageApiService.getAllMessages(pattern, groupId);
    }

    public Observable<Message> sendMessage(String message, String id, String path) {
        return this.messageApiService.postMessage(path, id, new CreateMessageDto(message))
                .onErrorReturn(error -> new Message(Constants.LOBBY_CHAT_ERROR, Constants.LOBBY_CHAT_ERROR, Constants.LOBBY_CHAT_ERROR, Constants.LOBBY_CHAT_ERROR, Constants.LOBBY_CHAT_ERROR));
    }

    public Observable<Message> deleteMessage(String groupId, String messageId, String path) {
        return this.messageApiService.deleteMessage(path, groupId, messageId);
    }

    public Observable<Message> updateMessage(String message, String groupId, String messageId) {
        return this.messageApiService.updateMessage(Constants.GROUPS, groupId, new UpdateMessageDto(message), messageId);
    }

    public LoginResultStorage getLoginResultStorage() {
        return loginResultStorage;
    }
}
