package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateGroupDto;
import de.uniks.pioneers.dto.UpdateGroupDto;
import de.uniks.pioneers.model.Group;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface GroupApiService {
    @POST("groups")
    Observable<Group> createGroup(@Body CreateGroupDto dto);

    @GET("groups")
    Observable<List<Group>> getGroupsList(@Query("members") String members);

    @GET("groups/{id}")
    Observable<Group> getGroup(@Path("id") String id);

    @PATCH("groups/{id}")
    Observable<Group> setGroup(@Path("id") String id, @Body UpdateGroupDto dto);

    @DELETE("groups/{id}")
    Observable<Group> deleteGroup(@Path("id") String groupId);
}
