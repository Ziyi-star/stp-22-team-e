package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.LoginDto;
import de.uniks.pioneers.dto.RefreshDto;
import de.uniks.pioneers.model.LoginResult;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("auth/login")
    Observable<LoginResult> login(@Body LoginDto dto);

    @POST("auth/refresh")
    Observable<LoginResult> refresh(@Body RefreshDto dto);

    @POST("auth/logout")
    Observable<Response<Void>> logout();
}
