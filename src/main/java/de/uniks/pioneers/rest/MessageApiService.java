package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateMessageDto;
import de.uniks.pioneers.dto.UpdateMessageDto;
import de.uniks.pioneers.model.Message;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface MessageApiService {

    @POST("{namespace}/{parent}/messages")
    Observable<Message> postMessage(@Path("namespace") String namespace, @Path("parent") String parent, @Body CreateMessageDto dto);

    @GET("{namespace}/{parent}/messages")
    Observable<List<Message>> getAllMessages(@Path("namespace") String namespace, @Path("parent") String parent);

    @GET("{namespace}/{parent}/messages/{id}")
    Observable<Message> getMessage(@Path("namespace") String namespace, @Path("parent") String parent, @Path("id") String id);

    @PATCH("{namespace}/{parent}/messages/{id}")
    Observable<Message> updateMessage(@Path("namespace") String namespace, @Path("parent") String parent, @Body UpdateMessageDto dto, @Path("id") String id);

    @DELETE("{namespace}/{parent}/messages/{id}")
    Observable<Message> deleteMessage(@Path("namespace") String namespace, @Path("parent") String parent, @Path("id") String id);
}
