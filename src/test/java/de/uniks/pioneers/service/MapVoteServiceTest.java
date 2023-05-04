package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.CreateVoteDto;
import de.uniks.pioneers.model.Vote;
import de.uniks.pioneers.rest.MapVotesApiService;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapVoteServiceTest {

    @Spy
    ObjectMapper mapper;

    @Mock
    MapVotesApiService mapVotesApiService;

    @InjectMocks
    MapVoteService mapVoteService;

    @Test
    void sendVote() {
        // Success case
        when(mapVotesApiService.vote(anyString(), any())).thenReturn(Observable.just(new Vote("1", "1", "42", "42", 1)));

        Vote successVote = mapVoteService.sendVote("42", 1).blockingFirst();
        assertEquals(successVote.score(), 1);

        // Error case
        when(mapVotesApiService.vote(anyString(), any())).thenReturn(Observable.error(new Throwable()));

        Vote errorVote = mapVoteService.sendVote("42", -1).blockingFirst();
        assertNull(errorVote.score());
        assertEquals(Constants.CUSTOM_ERROR, errorVote.createdAt());

        // Verify methods
        verify(mapVotesApiService).vote("42", new CreateVoteDto(1));
        verify(mapVotesApiService).vote("42", new CreateVoteDto(-1));
    }

    @Test
    void getVoteFromUser() {
        // Success case
        when(mapVotesApiService.getVote(anyString(), anyString())).thenReturn(Observable.just(new Vote("2", "2", "42", "42", 1)));

        Vote successVote = mapVoteService.getVoteFromUser("42", "42").blockingFirst();
        assertEquals(successVote.score(), 1);

        // Error case
        when(mapVotesApiService.getVote(anyString(), any())).thenReturn(Observable.error(new Throwable()));

        Vote errorVote = mapVoteService.getVoteFromUser("42", "0815").blockingFirst();
        assertNull(errorVote.score());
        assertEquals(Constants.CUSTOM_ERROR, errorVote.createdAt());

        // Verify methods
        verify(mapVotesApiService).getVote("42", "42");
        verify(mapVotesApiService).getVote("42", "0815");
    }

    @Test
    void getAllVotesFromUser() {
        // Success case
        when(mapVotesApiService.getVotesByUser(anyString())).thenReturn(Observable.just(List.of(new Vote("2", "2", "42", "42", 1))));

        List<Vote> successVote = mapVoteService.getAllVotesFromUser("42").blockingFirst();
        assertEquals(successVote.get(0).score(), 1);
        assertEquals(successVote.size(), 1);

        // Error case
        when(mapVotesApiService.getVotesByUser(anyString())).thenReturn(Observable.error(new Throwable()));

        List<Vote> errorVote = mapVoteService.getAllVotesFromUser("0815").blockingFirst();
        assertNull(errorVote.get(0).score());
        assertEquals(Constants.CUSTOM_ERROR, errorVote.get(0).createdAt());

        // Verify methods
        verify(mapVotesApiService).getVotesByUser("42");
        verify(mapVotesApiService).getVotesByUser("0815");
    }

    @Test
    void updateVote() {
        // Success case
        when(mapVotesApiService.updateVote(anyString(), anyString(), any())).thenReturn(Observable.just(new Vote("3", "3", "001", "0815", -1)));

        Vote successVote = mapVoteService.updateVote("001", "0815", -1).blockingFirst();
        assertEquals(successVote.score(), -1);

        // Error case
        when(mapVotesApiService.updateVote(anyString(), anyString(), any())).thenReturn(Observable.error(new Throwable()));

        Vote errorVote = mapVoteService.updateVote("001", "42", 1).blockingFirst();
        assertNull(errorVote.score());
        assertEquals(Constants.CUSTOM_ERROR, errorVote.createdAt());

        // Verify methods
        verify(mapVotesApiService).updateVote("001", "0815", new CreateVoteDto(-1));
        verify(mapVotesApiService).updateVote("001", "42", new CreateVoteDto(1));
    }

    @Test
    void deleteVote() {
        // Success case
        when(mapVotesApiService.deleteVote(anyString(), anyString())).thenReturn(Observable.just(new Vote("4", "4", "42", "0815", -1)));

        Vote successVote = mapVoteService.deleteVote("42", "0815").blockingFirst();
        assertEquals(successVote.score(), -1);

        // Error case
        when(mapVotesApiService.deleteVote(anyString(), anyString())).thenReturn(Observable.error(new Throwable()));

        Vote errorVote = mapVoteService.deleteVote("42", "42").blockingFirst();
        assertNull(errorVote.score());
        assertEquals(Constants.CUSTOM_ERROR, errorVote.createdAt());

        // Verify methods
        verify(mapVotesApiService).deleteVote("42", "0815");
        verify(mapVotesApiService).deleteVote("42", "42");

    }

    @Test
    void findAllVotes() {
        // Success case
        when(mapVotesApiService.getVotes(anyString())).thenReturn(Observable.just(List.of(new Vote("4", "4", "42", "0815", -1),
                new Vote("4", "4", "42", "123", 1))));

        List<Vote> votesList = mapVoteService.getVotes("42").blockingFirst();
        assertEquals(votesList.size(), 2);

        // Error case
        when(mapVotesApiService.getVotes(anyString())).thenReturn(Observable.error(new Throwable()));

        List<Vote> errorVotesList = mapVoteService.getVotes("43").blockingFirst();
        assertNull(errorVotesList.get(0).userId());
        assertEquals(Constants.CUSTOM_ERROR, errorVotesList.get(0).createdAt());

    }

    @Test
    void getVotes() {
        // Success case
        when(mapVotesApiService.getVotes(anyString())).thenReturn(Observable.just(List.of(new Vote("4", "4", "42", "0815", -1))));

        List<Vote> successVote = mapVoteService.getVotes("42").blockingFirst();
        assertEquals(successVote.get(0).score(), -1);

        // Error case
        when(mapVotesApiService.getVotes(anyString())).thenReturn(Observable.error(new Throwable()));

        List<Vote> errorVote = mapVoteService.getVotes("42").blockingFirst();
        assertNull(errorVote.get(0).score());
        assertEquals(Constants.CUSTOM_ERROR, errorVote.get(0).createdAt());

        // Verify methods
        verify(mapVotesApiService, times(2)).getVotes("42");
    }

}