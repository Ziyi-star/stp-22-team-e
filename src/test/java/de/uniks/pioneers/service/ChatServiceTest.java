package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateGroupDto;
import de.uniks.pioneers.dto.CreateMessageDto;
import de.uniks.pioneers.model.Group;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.Message;
import de.uniks.pioneers.rest.GroupApiService;
import de.uniks.pioneers.rest.MessageApiService;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import retrofit2.HttpException;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {
    @Spy
    LoginResultStorage loginResultStorage;

    @Spy
    ObjectMapper mapper;

    @Mock
    MessageApiService messageApiService;

    @Mock
    GroupApiService groupApiService;

    @InjectMocks
    ChatService chatService;

    @Test
    void createGroup() {
        // ---- Successful Group Creation ----
        // (1) Normal user names
        List<String> memberList = new ArrayList<>();
        memberList.add("1234");
        memberList.add("custom_id");
        String name = "Rainer69";
        String otherName = "custom_name";
        String expectedGroupName = "Rainer69custom_name";

        // login user custom_name
        loginResultStorage.setLoginResult(
                new LoginResult("1", "2", "custom_id", otherName, "5", "6", null, "7", "8"));

        // group name is name + : + custom_name
        when(groupApiService.createGroup(ArgumentMatchers.any())).thenReturn(Observable
                .just(new Group("12.03", "13.03", "1234", "Rainer69custom_name", memberList)));

        // get group
        Group group = chatService.createGroup(name, "1234").blockingFirst();
        // assert that return value group name and expected group name are the same
        assertEquals(group.name(), expectedGroupName);
        // verify that groupApiService method is called with provided arguments
        verify(groupApiService).createGroup(new CreateGroupDto(expectedGroupName, memberList));

        // (2) Case with two long usernames

        String otherName2 = "custom_nameLongerLongerLonger";
        String expectedGroupName2 = "Rainer69custom_nameLongerLong..";

        // login user custom_name
        loginResultStorage.setLoginResult(
                new LoginResult("1", "2", "custom_id", otherName2, "5", "6", null, "7", "8"));

        // group name is name + : + custom_name
        when(groupApiService.createGroup(ArgumentMatchers.any())).thenReturn(Observable
                .just(new Group("12.03", "13.03", "1234", "Rainer69custom_nameLongerLong..", memberList)));

        // get group
        Group group2 = chatService.createGroup(name, "1234").blockingFirst();
        // assert that return value group name and expected group name are the same
        assertEquals(group2.name(), expectedGroupName2);
        // verify that groupApiService method is called with provided arguments
        verify(groupApiService).createGroup(new CreateGroupDto(expectedGroupName, memberList));

        // ---- Error case ----
        // Error message
        final String customErrorMessage = "Custom Error Message";

        final String errorResponseNoArray = "{" + "\"statusCode\": 400," + "\"message\": \"Custom Error Message\"," + "\"error\": \"Bad Request\"" + "}";
        final ResponseBody responseBodyNoArray = ResponseBody.create(errorResponseNoArray, MediaType.parse("application/json"));

        // throw http error with Custom Error Message
        when(groupApiService.createGroup(any())).thenReturn(Observable
                .error(new HttpException((Response.error(400, responseBodyNoArray)))));

        final Group errorGroup = chatService.createGroup("ErrorName", "ErrorId").blockingFirst();

        // assert that error message is provided
        assertEquals(customErrorMessage, errorGroup.name());
    }

    @Test
    void getGroups() {
        List<String> memberList = new ArrayList<>();
        memberList.add("1234");
        memberList.add("custom_id");
        Group group = new Group("12.03", "13.03", "1234", "Rainer69:custom_name", memberList);
        List<Group> groupList = new ArrayList<>();
        groupList.add(group);
        when(groupApiService.getGroupsList(anyString())).thenReturn(Observable.just(groupList));

        List<Group> expectedGroupList = chatService.getGroups("1234").blockingFirst();
        assertEquals(expectedGroupList.get(0), group);

        verify(groupApiService).getGroupsList("1234");
    }

    @Test
    void getGroup() {
        List<String> memberList = new ArrayList<>();
        memberList.add("1234");
        memberList.add("custom_id");
        Group group = new Group("12.03", "13.03", "1234", "Rainer69:custom_name", memberList);

        when(groupApiService.getGroup(anyString())).thenReturn(Observable.just(group));

        Group expectedGroup = chatService.getGroup("1234").blockingFirst();
        assertEquals(group, expectedGroup);
        verify(groupApiService).getGroup("1234");
    }

    @Test
    void getSpecificGroups() {
        // create two groups and add them to groups list
        List<String> memberList1 = new ArrayList<>();
        memberList1.add("1234");
        memberList1.add("custom_id1");
        Group group1 = new Group("12.03", "13.03", "123", "Rainer69:custom_name1", memberList1);

        List<String> memberList2 = new ArrayList<>();
        memberList2.add("1234");
        memberList2.add("custom_id2");
        Group group2 = new Group("12.04", "13.04", "234", "Rainer69:custom_name2", memberList2);
        List<Group> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);

        String loginId = "12345";
        loginResultStorage.setLoginResult(
                new LoginResult("1", "2", loginId, "custom_name", "5", "6", null, "7", "8"));

        // api service returns groups list
        when(groupApiService.getGroupsList(anyString())).thenReturn(Observable.just(groups));

        // assert that groups list and expectedGroups list are identical
        List<Group> expectedGroups = chatService.getSpecificGroups("custom_id1").blockingFirst();
        assertEquals(groups, expectedGroups);
        // verify that groupApiService method was called with provided arguments
        verify(groupApiService).getGroupsList("custom_id1," + loginId);
    }

    @Test
    void getAllMessages() {
        // create messages and add to list
        Message msg1 = new Message("01.01", "02.01", "123", "sender_id", "Hallo");
        Message msg2 = new Message("01.01", "03.01", "124", "sender_id", "Wie");
        Message msg3 = new Message("01.01", "04.01", "125", "sender_id", "gehts");
        List<Message> messageList = new ArrayList<>();
        messageList.add(msg1);
        messageList.add(msg2);
        messageList.add(msg3);

        when(messageApiService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(messageList));

        List<Message> expectedMessages = chatService.getAllMessages("999", Constants.GROUPS).blockingFirst();

        // assert that messageList and expectedMessages are the same
        assertEquals(messageList, expectedMessages);
        // verify that get messages is called with provided arguments
        verify(messageApiService).getAllMessages(Constants.GROUPS, "999");
    }

    @Test
    void sendMessage() {
        String myMessageBody = "jojo";
        String groupId = "groupId";
        Message msg1 = new Message("01.01", "02.01", "123", "sender_id", myMessageBody);

        // messageApiService returns custom message msg1
        when(messageApiService.postMessage(anyString(), anyString(), any()))
                .thenReturn(Observable.just(msg1));

        Message expectedMsg = chatService.sendMessage(myMessageBody, groupId, Constants.GROUPS).blockingFirst();

        // assert that both message bodies are the same
        assertEquals(myMessageBody, expectedMsg.body());
        // verify that post method is called with provided arguments
        verify(messageApiService).postMessage(Constants.GROUPS, groupId, new CreateMessageDto(myMessageBody));
    }

    @Test
    void deleteMessage() {
        String myMessageBody = "jojo";
        String groupId = "999";
        String msgId = "123";
        Message msg1 = new Message("01.01", "02.01", msgId, "sender_id", myMessageBody);

        List<Message> messageList = new ArrayList<>();
        messageList.add(msg1);

        // messageApiService returns custom message msg1
        when(messageApiService.deleteMessage(anyString(), anyString(), anyString())).thenReturn(Observable.just(msg1));

        Message deletedMessage = chatService.deleteMessage(groupId, msgId, Constants.GROUPS).blockingFirst();
        // assert that  first item in messageList and deletedMessages are the same
        assertEquals(messageList.get(0), deletedMessage);

        // verify that deleted method is called with provided arguments
        verify(messageApiService).deleteMessage(Constants.GROUPS, groupId, msgId);
    }
}
