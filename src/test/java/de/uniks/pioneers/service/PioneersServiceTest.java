package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.rest.PioneersApiService;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PioneersServiceTest {

    @Spy
    ObjectMapper mapper;

    @Spy
    GameStorage gameStorage;

    @Mock
    PioneersApiService pioneersApiService;

    @InjectMocks
    PioneersService pioneersService;

    @Test
    void getStoredGame() {
        Game expectedGame = new Game("1", "1", "123", "Game", "1964", 1, true, null);
        gameStorage.setGame(expectedGame);
        Game game = pioneersService.getStoredGame();
        assertEquals(game, expectedGame);
    }

    @Test
    void checkRoadResources() {
        //enough resources
        Player enough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 1, 1, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkRoad = pioneersService.checkRoadResources(enough);
        assertEquals(checkRoad, true);

        //not enough resources
        Player notEnough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 0, 1, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkRoad1 = pioneersService.checkRoadResources(notEnough);
        assertEquals(checkRoad1, false);

        //not enough roads
        Player notEnoughRoad = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 1, 1, 1, 1), new RemainingBuildings(2, 2, 0), 0, 0, null);
        Boolean checkRoad2 = pioneersService.checkRoadResources(notEnoughRoad);
        assertEquals(checkRoad2, false);
    }

    @Test
    void checkSettlementResources() {
        //enough resources
        Player enough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 1, 1, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkRoad = pioneersService.checkSettlementResources(enough);
        assertEquals(checkRoad, true);

        //not enough resources
        Player notEnough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 0, 1, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkSettlement1 = pioneersService.checkSettlementResources(notEnough);
        assertEquals(checkSettlement1, false);

        //not enough settlements
        Player notEnoughRoad = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 1, 1, 1, 1), new RemainingBuildings(0, 2, 2), 0, 0, null);
        Boolean checkSettlement2 = pioneersService.checkSettlementResources(notEnoughRoad);
        assertEquals(checkSettlement2, false);
    }

    @Test
    void checkCityResources() {
        //enough resources
        Player enough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 2, 1, 3, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkRoad = pioneersService.checkCityResources(enough);
        assertEquals(checkRoad, true);

        //not enough resources
        Player notEnough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 0, 1, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkCity1 = pioneersService.checkCityResources(notEnough);
        assertEquals(checkCity1, false);

        //not enough settlements
        Player notEnoughRoad = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 2, 1, 3, 1, 1), new RemainingBuildings(2, 0, 2), 0, 0, null);
        Boolean checkCity2 = pioneersService.checkCityResources(notEnoughRoad);
        assertEquals(checkCity2, false);
    }

    @Test
    void checkDevelopmentCardResources() {
        // Enough resources
        Player enough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 2, 1, 3, 1, 1), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkDevelopmentResources = pioneersService.checkDevelopmentResources(enough);
        assertEquals(checkDevelopmentResources, true);

        // Not enough resources
        Player notEnough = new Player("3", "123", "#FFFFFF", false, 8, new Resources(0, 1, 0, 1, 1, 0), new RemainingBuildings(2, 2, 2), 0, 0, null);
        Boolean checkDevelopmentResources1 = pioneersService.checkDevelopmentResources(notEnough);
        assertEquals(checkDevelopmentResources1, false);
    }

    @Test
    void getMap() {
        when(pioneersApiService.getMap(anyString())).thenReturn(Observable.just(new Map("3", List.of(new Tile(0, 0, 0, "desert", 1)), null)));
        //success case
        Map map = pioneersService.getMap("3").blockingFirst();
        assertEquals(map.gameId(), "3");

        when(pioneersApiService.getMap(anyString())).thenReturn(Observable.error(new Throwable()));
        //error case
        Map map1 = pioneersService.getMap("3").blockingFirst();
        assertEquals(map1.gameId(), Constants.CUSTOM_ERROR);
    }

    @Test
    void makeMove() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, true, null);
        gameStorage.setGame(expectedGame);

        when(pioneersApiService.createMove(anyString(), any())).thenReturn(Observable.just(new Move("1", "123", "3", "1", "roll", 6, null, null, null, null)));
        //success case
        Move move = pioneersService.makeMoveAction("roll").blockingFirst();
        assertEquals(move._id(), "123");

        when(pioneersApiService.createMove(anyString(), any())).thenReturn(Observable.just(new Move("1", "123", "3", "1", "build", 6, null, null, null, null)));
        Move move1 = pioneersService.makeMoveBuilding("build", 0, 0, 0, 0, "settlement").blockingFirst();
        assertEquals(move1._id(), "123");

        when(pioneersApiService.createMove(anyString(), any())).thenReturn(Observable.error(new Throwable()));

        //error case
        Move move2 = pioneersService.makeMoveAction("roll").blockingFirst();
        assertEquals(move2.createdAt(), Constants.CUSTOM_ERROR);

        Move move3 = pioneersService.makeMoveBuilding("build", 0, 0, 0, 0, "settlement").blockingFirst();
        assertEquals(move3.createdAt(), Constants.CUSTOM_ERROR);
    }

    @Test
    void getGameState() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, true, null);
        gameStorage.setGame(expectedGame);

        when(pioneersApiService.getState(anyString())).thenReturn(Observable.just(new State("1", "3", null, null)));
        //success case
        State state = pioneersService.getGameState().blockingFirst();
        assertEquals(state.gameId(), "3");

        when(pioneersApiService.getState(anyString())).thenReturn(Observable.error(new Throwable()));
        //error case
        State state1 = pioneersService.getGameState().blockingFirst();
        assertEquals(state1.gameId(), Constants.CUSTOM_ERROR);
    }

    @Test
    void getGamePlayers() {
        Game expectedGame = new Game("1", "1", "3", "Game", "1964", 1, true, null);
        gameStorage.setGame(expectedGame);

        when(pioneersApiService.getPlayers(anyString())).thenReturn(Observable.just(List.of(new Player("3", "123", "#FFFFFF", false, 6, null, null, 0, 0, null))));
        //success case
        List<Player> players = pioneersService.getGamePlayers().blockingFirst();
        assertEquals(players.get(0).gameId(), "3");

        when(pioneersApiService.getPlayers(anyString())).thenReturn(Observable.error(new Throwable()));
        //error case
        List<Player> players1 = pioneersService.getGamePlayers().blockingFirst();
        assertEquals(players1.get(0).gameId(), Constants.CUSTOM_ERROR);
    }
}
