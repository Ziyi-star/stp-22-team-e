package de.uniks.pioneers.rest;

import de.uniks.pioneers.dto.CreateMemberDto;
import de.uniks.pioneers.dto.UpdateMemberDto;
import de.uniks.pioneers.model.Member;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.*;

import java.util.List;


public interface GameMemberApiService {

    @POST("games/{gameId}/members")
    Observable<Member> joinLobby(@Path("gameId") String gameId, @Body CreateMemberDto createMemberDto);

    @GET("games/{gameId}/members")
    Observable<List<Member>> getGameMembers(@Path("gameId") String gameId);

    @GET("games/{gameId}/members/{userId}")
    Observable<Member> getGameMember(@Path("gameId") String gameId, @Path("userId") String userId);

    @PATCH("games/{gameId}/members/{userId}")
    Observable<Member> updateMember(@Path("gameId") String gameId, @Path("userId") String userId, @Body UpdateMemberDto updateMemberDto);

    @DELETE("games/{gameId}/members/{userId}")
    Observable<Member> deleteGameMember(@Path("gameId") String gameId, @Path("userId") String userId);


}
