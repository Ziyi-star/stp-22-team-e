package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.dto.CreateVoteDto;
import de.uniks.pioneers.model.Vote;
import de.uniks.pioneers.rest.MapVotesApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;

import javax.inject.Inject;
import java.util.List;

public class MapVoteService {

    private final MapVotesApiService mapVotesApiService;
    private final ObjectMapper mapper;
    private final LoginResultStorage loginResultStorage;

    @Inject
    public MapVoteService(MapVotesApiService mapVotesApiService, ObjectMapper mapper, LoginResultStorage loginResultStorage) {
        this.mapVotesApiService = mapVotesApiService;
        this.mapper = mapper;
        this.loginResultStorage = loginResultStorage;
    }


    public Observable<Vote> sendVote(String mapId, Number score) {
        return mapVotesApiService.vote(mapId, new CreateVoteDto(score)).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Vote(errorResult, null, null, null, null);
        });
    }

    public Observable<Vote> getVoteFromUser(String mapId, String userId) {
        return mapVotesApiService.getVote(mapId, userId).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Vote(errorResult, null, null, null, null);
        });
    }

    public Observable<Vote> getVote(String mapId) {
        return getVoteFromUser(mapId, loginResultStorage.getLoginResult()._id());
    }

    public Observable<List<Vote>> getVotes(String mapId) {
        return mapVotesApiService.getVotes(mapId)
                .onErrorReturn(error -> {
                    String errorResult = new ErrorHandling().handleError(error, mapper);
                    return List.of(new Vote(errorResult, null, null, null, null));
                });
    }

    public Observable<List<Vote>> getAllVotesFromUser(String userId) {
        return mapVotesApiService.getVotesByUser(userId).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return List.of(new Vote(errorResult, null, null, null, null));
        });
    }

    public Observable<Vote> updateVote(String mapId, String userId, Number score) {
        return mapVotesApiService.updateVote(mapId, userId, new CreateVoteDto(score)).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Vote(errorResult, null, null, null, null);
        });
    }

    public Observable<Vote> updateMyVote(String mapId, Number score) {
        return updateVote(mapId, loginResultStorage.getLoginResult()._id(), score);
    }

    public Observable<Vote> deleteVote(String mapId, String userId) {
        return mapVotesApiService.deleteVote(mapId, userId).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new Vote(errorResult, null, null, null, null);
        });
    }

    public Observable<Vote> deleteMyVote(String mapId) {
        return deleteVote(mapId, loginResultStorage.getLoginResult()._id());
    }
}
