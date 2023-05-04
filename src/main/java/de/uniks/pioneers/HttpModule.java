package de.uniks.pioneers;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import de.uniks.pioneers.rest.*;
import de.uniks.pioneers.service.LoginResultStorage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.inject.Singleton;

@Module
public class HttpModule {

    @Provides
    @Singleton
    ObjectMapper mapper() {  // used to convert JAVA objects to JSON

        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    // Used to add a header for a request
    @Provides
    @Singleton
    OkHttpClient client(LoginResultStorage loginResultStorage) {
        return new OkHttpClient.Builder().addInterceptor(chain -> {
            final String token;
            if (loginResultStorage.getLoginResult() == null) {
                return chain.proceed(chain.request());
            } else {

                token = loginResultStorage.getLoginResult().accessToken();
            }
            final Request newRequest = chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }).build();
    }

    @Provides
    @Singleton
    Retrofit retrofit(OkHttpClient client, ObjectMapper mapper) { // used for HTTP requests to a server

        return new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    // Necessary for all API services (user, group, ... ) :
    @Provides
    @Singleton
    UserApiService userApiService(Retrofit retrofit) {

        return retrofit.create(UserApiService.class);
    }

    @Provides
    @Singleton
    AuthApiService authApiService(Retrofit retrofit) {

        return retrofit.create(AuthApiService.class);
    }

    @Provides
    @Singleton
    GameApiService gameApiService(Retrofit retrofit) {

        return retrofit.create(GameApiService.class);
    }

    @Provides
    @Singleton
    GroupApiService groupApiService(Retrofit retrofit) {
        return retrofit.create(GroupApiService.class);
    }

    @Provides
    @Singleton
    MessageApiService messageApiService(Retrofit retrofit) {
        return retrofit.create(MessageApiService.class);
    }

    @Provides
    @Singleton
    GameMemberApiService gameMemberApiService(Retrofit retrofit) {

        return retrofit.create(GameMemberApiService.class);
    }

    @Provides
    @Singleton
    PioneersApiService pioneersApiService(Retrofit retrofit) {

        return retrofit.create(PioneersApiService.class);
    }

    @Provides
    @Singleton
    MapTemplatesApiService mapTemplatesApiService(Retrofit retrofit) {
        return retrofit.create(MapTemplatesApiService.class);
    }

    @Provides
    @Singleton
    MapVotesApiService mapVotesApiService(Retrofit retrofit) {
        return retrofit.create(MapVotesApiService.class);
    }

}
