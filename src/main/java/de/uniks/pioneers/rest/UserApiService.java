package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateUserDto;
import de.uniks.pioneers.dto.UpdateUserDto;
import de.uniks.pioneers.model.User;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface    UserApiService {

    @POST("users")
    Observable<User> create(@Body CreateUserDto dto);

    @GET("users")
    Observable<List<User>> getUserList(@Query("status") String status, @Query("ids") String ids);

    @GET("users/{id}")
    Observable<User> getUser(@Path("id") String id);

    @PATCH("users/{id}")
    Observable<User> patchUser(@Path("id") String id, @Body UpdateUserDto dto);

    @DELETE("users/{id}")
    Observable<User> deleteUser(@Path("id") String id);
}
