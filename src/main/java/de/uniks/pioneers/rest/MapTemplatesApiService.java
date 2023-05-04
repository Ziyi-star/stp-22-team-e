package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateMapTemplateDto;
import de.uniks.pioneers.dto.UpdateMapTemplateDto;
import de.uniks.pioneers.model.MapTemplate;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;

public interface MapTemplatesApiService {

    @POST("maps")
    Observable<MapTemplate> createMap(@Body CreateMapTemplateDto createMapTemplateDto);

    @GET("maps")
    Observable<List<MapTemplate>> getMaps(@Query("createdBy") String createdBy);

    @GET("maps/{id}")
    Observable<MapTemplate> getMapById(@Path("id") String id);

    @PATCH("maps/{id}")
    Observable<MapTemplate> editMap(@Path("id") String id, @Body UpdateMapTemplateDto updateMapTemplateDto);

    @DELETE("maps/{id}")
    Observable<MapTemplate> deleteMap(@Path("id") String id);
}
