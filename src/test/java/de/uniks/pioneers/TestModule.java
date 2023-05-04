package de.uniks.pioneers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import de.uniks.pioneers.dto.*;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.rest.*;
import de.uniks.pioneers.service.MusicService;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import retrofit2.Response;

import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;

@Module
public class TestModule {

    private Subject<Event<Game>> gameEventSubject;

    private Subject<Event<Member>> memberEventSubject;

    private Subject<Event<Building>> buildingEventSubject;      //For Building Websocket

    private Subject<Event<State>> stateEventSubject;      //For State Websocket

    private Subject<Event<Player>> playerEventSubject;      //For Player Websocket

    private Subject<Event<Move>> moveEventSubject;

    @Provides
    MusicService musicService() {
        return new MusicService(prefService()) {
            @Override
            public void playMusic(Constants.MUSIC_CONTEXT context) {
            }

            @Override
            public void playSoundEffect(String musicFile) {
            }

            @Override
            public void stopMusic() {
            }

            @Override
            public void changeMusicVolume(double currVolume) {
            }

            @Override
            public void changeSoundVolume(double currVolume) {
            }

            @Override
            public void muteMusic(boolean muted) {
            }

            @Override
            public boolean getMusicMuted() {
                return true;
            }

            @Override
            public void muteSound(boolean muted) {
            }

            @Override
            public boolean getSoundMuted() {
                return true;
            }

            @Override
            public double getCurrentMusicVol() {
                return 1f;
            }

            @Override
            public double getCurrentSoundVol() {
                return 1f;
            }

            @Override
            public boolean getMusicPlaying() {
                return false;
            }
        };
    }

    @Provides
    PrefService prefService() {
        return new PrefService(Preferences.userNodeForPackage(Main.class)) {
            @Override
            public Locale getLocale() {
                return Locale.getDefault();
            }

            @Override
            public void setLocale(Locale locale) {
                Locale.setDefault(locale);
            }

            @Override
            public String getCurrentGame() {
                return "42";
            }
        };
    }

    @Provides
    ObjectMapper mapper() {

        return new ObjectMapper()
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Provides
    EventListener eventListener() {

        return new EventListener(null, null) {

            @Override
            @SuppressWarnings("unchecked")
            public <T> Observable<Event<T>> listen(String pattern, Class<T> type) {
                Subject<Event<T>> eventSubject;
                Subject<Event<T>> eventSubject2;
                Subject<Event<T>> eventSubject3;
                Subject<Event<T>> eventSubject4;
                Subject<Event<T>> eventSubject5;
                Subject<Event<T>> eventSubject6;
                switch (pattern) {
                    case "games." + "3" + ".members.*.*" -> {
                        eventSubject = PublishSubject.create();
                        memberEventSubject = (Subject<Event<Member>>) (Object) eventSubject;
                        return eventSubject;
                    }
                    case "games." + "3" + ".buildings.*.*" -> {
                        eventSubject2 = PublishSubject.create();
                        buildingEventSubject = (Subject<Event<Building>>) (Object) eventSubject2;
                        return eventSubject2;
                    }
                    case Constants.GAMES + "." + "3" + ".state" + ".*" -> {
                        eventSubject3 = PublishSubject.create();
                        stateEventSubject = (Subject<Event<State>>) (Object) eventSubject3;
                        return eventSubject3;
                    }
                    case Constants.GAMES + "." + "3" + ".players.*.*" -> {
                        eventSubject4 = PublishSubject.create();
                        playerEventSubject = (Subject<Event<Player>>) (Object) eventSubject4;
                        return eventSubject4;
                    }
                    case Constants.GAMES + "." + "3" + ".*" -> {
                        eventSubject5 = PublishSubject.create();
                        gameEventSubject = (Subject<Event<Game>>) (Object) eventSubject5;
                        return eventSubject5;
                    }
                    case Constants.GAMES + "." + "3" + ".moves.*.*" -> {
                        eventSubject6 = PublishSubject.create();
                        moveEventSubject = (Subject<Event<Move>>) (Object) eventSubject6;
                        return eventSubject6;
                    }
                    default -> {
                        return Observable.empty();
                    }
                }
            }

            @Override
            public void send(Object message) {
            }
        };
    }

    @Provides
    UserApiService userApiService() {
        return new UserApiService() {
            @Override
            public Observable<User> create(CreateUserDto dto) {
                return Observable.just(new User("42", "Rick", "offline", null, null));
            }

            @Override
            public Observable<User> patchUser(String _id, UpdateUserDto dto) {
                if (dto.status().equals(Constants.STATUS_ONLINE)) {
                    return Observable.just(new User("42", "Rick", "online", null, null));
                } else {
                    return Observable.just(new User("42", "Rick", "offline", null, null));
                }
            }

            @Override
            public Observable<User> getUser(String id) {
                if (id.equals("0815")) {
                    return Observable.just(new User("0815", "Morty", "online", null, null));
                } else {
                    return Observable.just(new User("42", "Rick", "online", null, null));
                }
            }

            @Override
            public Observable<List<User>> getUserList(String status, String ids) {
                return Observable.just(List.of(
                        new User("0815", "Morty", "online", null, null),
                        new User("Owen", "Morty", "online", null, null),
                        new User("2", "Morty", "online", null, null)
                ));
            }

            @Override
            public Observable<User> deleteUser(String id) {
                return Observable.just(new User("42", "Rick", "offline", null, null));
            }
        };
    }

    @Provides
    AuthApiService authApiService() {
        return new AuthApiService() {
            @Override
            public Observable<LoginResult> login(LoginDto dto) {
                return Observable.just(new LoginResult("42", "42", "Owen", "Rick", "offline", null, null, "123", "456"));
            }

            @Override
            public Observable<LoginResult> refresh(RefreshDto dto) {
                return Observable.just(new LoginResult("42", "42", "Owen", "Rick", "offline", null, null, "123", "456"));
            }

            @Override
            public Observable<Response<Void>> logout() {
                return Observable.just(Response.success(null));
            }
        };
    }

    @Provides
    GameApiService gameApiService() {
        return new GameApiService() {
            @Override
            public Observable<Game> create(CreateGameDto dto) {
                return Observable.just(new Game("1", "2", "123", "Rick", "Rick", 1, false, new GameSettings(5, 3)));
            }

            @Override
            public Observable<Game> getGame(String gameId) {
                return Observable.just(new Game("1", "2", "3", "a", "Owen", 1, false, new GameSettings(5, 3)));
            }

            @Override
            public Observable<List<Game>> findAllGames() {
                return Observable.just((List.of(new Game("1", "2", "3", "a", "Owen", 1, false, new GameSettings(5, 3)))));
                //return null;
            }

            @Override
            public Observable<Game> delete(String id) {
                return Observable.just(new Game("1", "2", "123", "Rick", "Rick", 1, false, new GameSettings(5, 3)));
            }

            @Override
            public Observable<Game> updateGame(String id, UpdateGameDto dto) {
                gameEventSubject.onNext(new Event<>(".updated", new Game("1", "2", "3", "a", "Owen", 2, true, new GameSettings(5, 3))));
                return Observable.just(new Game("1", "2", "3", "a", "Owen", 2, true, null));
            }
        };
    }

    @Provides
    GameMemberApiService gameMemberApiService() {
        return new GameMemberApiService() {
            @Override
            public Observable<List<Member>> getGameMembers(String gameId) {
                if (gameId.equals("3")) {
                    return Observable.just(List.of(new Member("3", "3", "3", "0815", true, "#00FFFF", false), new Member("3", "3", "3", "Owen", false, null, false)));
                } else {
                    return Observable.just(List.of(new Member("3", "3", "123", "42", false, null, false)));
                }
            }

            @Override
            public Observable<Member> joinLobby(String gameId, CreateMemberDto dto) {
                return Observable.just(new Member("3", "3", "3", "42", false, null, false));
            }

            @Override
            public Observable<Member> getGameMember(String gameId, String userId) {
                return Observable.just(new Member("3", "3", "3", "Owen", false, "#000000", false));
            }

            @Override
            public Observable<Member> updateMember(String gameId, String userId, UpdateMemberDto updateMemberDto) {
                if (updateMemberDto.ready()) {
                    memberEventSubject.onNext(new Event<>(".updated", new Member("3", "3", "3", "Owen", true, "#000000", false)));
                } else {
                    memberEventSubject.onNext(new Event<>(".updated", new Member("3", "3", "3", "Owen", false, "#000000", false)));
                }
                return Observable.just(new Member("3", "3", "123", "42", false, "#000000", false));
            }


            @Override
            public Observable<Member> deleteGameMember(String gameId, String userId) {
                return Observable.just(new Member("3", "3", "3", "42", false, "#000000", false));
            }
        };
    }

    @Provides
    GroupApiService groupApiService() {
        return new GroupApiService() {
            @Override
            public Observable<Group> createGroup(CreateGroupDto dto) {
                return Observable.just(new Group("3", "4", "42", "Rick:Morty", List.of("0815", "42")));
            }

            @Override
            public Observable<Group> setGroup(String _id, UpdateGroupDto dto) {
                return null;
            }

            @Override
            public Observable<Group> getGroup(String id) {
                return null;
            }

            @Override
            public Observable<List<Group>> getGroupsList(String members) {
                return Observable.just(List.of());
            }

            @Override
            public Observable<Group> deleteGroup(String groupId) {
                return null;
            }
        };


    }

    @Provides
    MessageApiService messageApiService() {
        return new MessageApiService() {
            @Override
            public Observable<List<Message>> getAllMessages(String namespace, String parent) {
                return Observable.empty();
            }

            @Override
            public Observable<Message> postMessage(String namespace, String parent, CreateMessageDto dto) {
                // Needed to trigger a move event for the trade
                // Player 2 accept the trade and is shown in accept offer list
                if (dto.body().equals("accept")) {
                    moveEventSubject.onNext(new Event<>(".created", new Move("8", "4", "3", "2", Constants.ACTION.OFFER.toString(), 0,
                            null, null, new ResourcesDto(1, -1, 0, 0, 0), null, null)));
                    stateEventSubject.onNext(new Event<>(".updated", new State("8", "3",
                            List.of(new ExpectedMove("accept", List.of("Owen"))), null)));
                    return Observable.just(new Message("1", "2", "3", "Owen", "accept"));
                } else {
                    return null;
                }
            }

            @Override
            public Observable<Message> getMessage(String namespace, String parent, String id) {
                return null;
            }

            @Override
            public Observable<Message> updateMessage(String namespace, String parent, UpdateMessageDto dto, String id) {
                return null;
            }

            @Override
            public Observable<Message> deleteMessage(String namespace, String parent, String id) {
                return null;
            }
        };
    }

    @Provides
    PioneersApiService pioneersApiService() {
        return new PioneersApiService() {
            @Override
            public Observable<Map> getMap(String gameId) {
                return Observable.just(new Map("3", List.of(
                        new Tile(-2, 0, 2, "hills", 9), new Tile(-2, 1, 1, "forest", 4), new Tile(-2, 2, 0, "desert", 7),
                        new Tile(-1, -1, 2, "mountains", 2), new Tile(-1, 0, 1, "forest", 3),
                        new Tile(-1, 1, 0, "fields", 8), new Tile(-1, 2, -1, "pasture", 9), new Tile(0, -2, 2, "hills", 6),
                        new Tile(0, -1, 1, "forest", 10), new Tile(0, 0, 0, "fields", 11), new Tile(0, 1, -1, "mountains", 5),
                        new Tile(0, 2, -2, "pasture", 5), new Tile(1, -2, 1, "pasture", 10), new Tile(1, -1, 0, "fields", 4),
                        new Tile(1, 0, -1, "pasture", 8), new Tile(1, 1, -2, "hills", 11), new Tile(2, -2, 0, "forest", 3),
                        new Tile(2, -1, -1, "hills", 12), new Tile(2, 0, -2, "mountains", 6)
                ), null));
            }

            @Override
            public Observable<List<Player>> getPlayers(String gameId) {
                return Observable.just(List.of(
                        new Player("3", "Owen", "#000000", true, 2, new Resources(0, 2, 2, 3, 2, 2), new RemainingBuildings(5, 4, 15), 0, null, false, false, null, List.of()),
                        new Player("3", "2", "#ffffffff", true, 7, new Resources(4, 1, 0, 3, 2, 1), new RemainingBuildings(5, 4, 15), 0, null, false, false, null, List.of())));
            }

            @Override
            public Observable<Player> getUser(String gameId, String userId) {
                return null;
            }

            @Override
            public Observable<Player> setUserActive(String gameId, String userId, UpdatePlayerDto dto) {
                return Observable.just(new Player("3", "Owen", "#000000", false, 2, new Resources(0, 2, 2, 3, 2, 2), new RemainingBuildings(5, 4, 15), 0, null, false, false, null, List.of()));
            }

            @Override
            public Observable<State> getState(String gameId) {
                return Observable.just(new State("0", "3", List.of(new ExpectedMove(Constants.ACTION.FOUNDING_ROLL.toString(), List.of("1"))), null));
            }

            @Override
            public Observable<List<Building>> getBuildings(String gameId) {
                return Observable.empty();
            }

            @Override
            public Observable<Building> getBuilding(String gameId, String buildingId) {
                return null;
            }

            @Override
            public Observable<Move> createMove(String gameId, CreateMoveDto dto) {
                switch (dto.action()) {
                    case "founding-roll" -> stateEventSubject.onNext(new Event<>(".updated", new State("1", "3", List.of(new ExpectedMove("founding-settlement-1", List.of("Owen"))), null)));
                    case "founding-settlement-1" -> {
                        // Build settlement in founding phase 1
                        buildingEventSubject.onNext(new Event<>(".created", new Building(0, 0, 0, "1", 0, "settlement", "3", "Owen")));
                        stateEventSubject.onNext(new Event<>(".updated", new State("2", "3", List.of(new ExpectedMove("founding-road-1", List.of("Owen"))), null)));

                        // Return value for setting settlement in make move result
                        return Observable.just(new Move("2", "1", "3", "Owen", "founding-settlement-1", 0, "settlement", null, null, null));
                    }
                    case "founding-road-1" -> {
                        // Build road in founding phase 1
                        buildingEventSubject.onNext(new Event<>(".created", new Building(0, 0, 0, "2", 11, "road", "3", "Owen")));

                        // Opponent founding phase
                        buildingEventSubject.onNext(new Event<>(".created", new Building(1, -1, 0, "3", 0, "settlement", "3", "2")));
                        buildingEventSubject.onNext(new Event<>(".created", new Building(1, -1, 0, "4", 11, "road", "3", "2")));
                        buildingEventSubject.onNext(new Event<>(".created", new Building(1, -1, 0, "5", 6, "settlement", "3", "2")));
                        buildingEventSubject.onNext(new Event<>(".created", new Building(1, -1, 0, "6", 7, "road", "3", "2")));

                        // Set state to founding settlement 2
                        stateEventSubject.onNext(new Event<>(".updated", new State("3", "3", List.of(new ExpectedMove("founding-settlement-2", List.of("Owen"))), null)));
                    }
                    case "founding-settlement-2" -> {
                        // Build settlement in founding phase 2
                        buildingEventSubject.onNext(new Event<>(".created", new Building(0, 0, 0, "7", 6, "settlement", "3", "Owen")));
                        stateEventSubject.onNext(new Event<>(".updated", new State("4", "3", List.of(new ExpectedMove("founding-road-2", List.of("Owen"))), null)));

                        // Return value for setting settlement in make move result
                        return Observable.just(new Move("4", "2", "3", "Owen", "founding-settlement-2", 0, "settlement", null, null, null));
                    }
                    case "founding-road-2" -> {
                        // Build road in founding phase 2
                        buildingEventSubject.onNext(new Event<>(".created", new Building(0, 0, 0, "8", 7, "road", "3", "Owen")));
                        stateEventSubject.onNext(new Event<>(".updated", new State("5", "3", List.of(new ExpectedMove("roll", List.of("Owen"))), null)));
                    }
                    case "roll" -> // Roll a 7 to trigger the robber
                            stateEventSubject.onNext(new Event<>(".updated", new State("6", "3", List.of(new ExpectedMove("rob", List.of("Owen"))), null)));
                    case "rob" -> // Set robber on another field
                            stateEventSubject.onNext(new Event<>(".updated", new State("7", "3", List.of(new ExpectedMove("build", List.of("Owen"))), new Point3D(1, -1, 0))));
                    case "accept" -> {
                        // Accept trade offer from another player
                        playerEventSubject.onNext(new Event<>(".updated", new Player("3", "Owen", "#000000", true, 2,
                                new Resources(0, 1, 3, 3, 2, 2),
                                new RemainingBuildings(5, 4, 15), 3, null, false, false, null, List.of())));
                        return Observable.just(new Move("8", "5", "3", "Owen", "accept", 0, null, null, null, null));
                    }
                    case "build" -> {
                        // Make an offer
                        if (dto.building() == null && dto.developmentCard() == null && dto.partner() == null) {
                            return Observable.just(new Move("7", "3", "3", "Owen", "offer", 0, null, null, null, null));
                        }
                        // Update player after you buy a development card
                        if (dto.developmentCard() != null && dto.developmentCard().equals("new")) {
                            playerEventSubject.onNext(new Event<>(".updated", new Player("3", "Owen", "#000000", true, 2,
                                    new Resources(0, 2, 3, 3, 2, 2),
                                    new RemainingBuildings(5, 4, 15), 3, null, false, false, null,
                                    List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString(), false, false),
                                            new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true)))));
                        }
                        // Update player and set state after you used the road building card
                        if (dto.developmentCard() != null && dto.developmentCard().equals(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString())) {
                            playerEventSubject.onNext(new Event<>(".updated", new Player("3", "Owen", "#000000", true, 2,
                                    new Resources(0, 2, 3, 3, 2, 2),
                                    new RemainingBuildings(5, 4, 15), 3, null, false, false, null,
                                    List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString(), true, false),
                                            new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true)))));
                            stateEventSubject.onNext(new Event<>(".updated", new State("9", "3", List.of(new ExpectedMove(Constants.ACTION.BUILD_ROAD.toString(), List.of("Owen"))), null)));
                            return Observable.just(new Move("9", "6", "3", "Owen", "road_building", 0, null, null, null, null));
                        }
                        // Upgrade settlement to city and set a winner
                        if (dto.building() != null && dto.building().type().equals("city")) {
                            buildingEventSubject.onNext(new Event<>(".created", new Building(0, 0, 0, "11", 0, "city", "3", "Owen")));
                            // Send a state event to set a winner
                            stateEventSubject.onNext(new Event<>(".updated", new State("12", "3", List.of(new ExpectedMove("build", List.of("Owen"))), null, "Owen")));
                            // Return value for setting city in make move result
                            return Observable.just(new Move("12", "6", "3", "Owen", "build", 0, "city", null, null, null));
                        }
                    }
                    case "build-road" -> {
                        // Set road after clicked on the first red circle from card event
                        if (dto.building() != null && dto.building().x().equals(0) && dto.building().z().equals(-1)) {
                            buildingEventSubject.onNext(new Event<>(".created", new Building(0, 1, -1, "9", 7, "road", "3", "Owen")));
                            stateEventSubject.onNext(new Event<>(".updated", new State("10", "3", List.of(new ExpectedMove(Constants.ACTION.BUILD_ROAD.toString(), List.of("Owen"))), null)));
                        }
                        // Set road after clicked on the second red circle and set state back to build
                        if (dto.building() != null && dto.building().x().equals(-1) && dto.building().z().equals(0)) {
                            buildingEventSubject.onNext(new Event<>(".created", new Building(-1, 1, 0, "10", 11, "road", "3", "Owen")));
                            stateEventSubject.onNext(new Event<>(".updated", new State("11", "3", List.of(new ExpectedMove("build", List.of("Owen"))), null)));
                        }
                    }
                }
                return Observable.empty();
            }

            @Override
            public Observable<Move> getMoveThroughUserId(String gameId, String userId) {
                return null;
            }

            @Override
            public Observable<Move> getMoveThroughMoveId(String gameId, String moveId) {
                return null;
            }
        };
    }

    @Provides
    MapTemplatesApiService mapTemplatesApiService() {
        return new MapTemplatesApiService() {
            @Override
            public Observable<MapTemplate> createMap(CreateMapTemplateDto createMapTemplateDto) {
                return null;
            }

            @Override
            public Observable<List<MapTemplate>> getMaps(String createdBy) {
                return Observable.just(List.of());
            }

            @Override
            public Observable<MapTemplate> getMapById(String id) {
                return null;
            }

            @Override
            public Observable<MapTemplate> editMap(String id, UpdateMapTemplateDto updateMapTemplateDto) {
                return null;
            }

            @Override
            public Observable<MapTemplate> deleteMap(String id) {
                return null;
            }
        };
    }

    @Provides
    MapVotesApiService mapVotesApiService() {
        return new MapVotesApiService() {
            @Override
            public Observable<Vote> vote(String mapId, CreateVoteDto createVoteDto) {
                return Observable.empty();
            }

            @Override
            public Observable<List<Vote>> getVotes(String mapId) {
                return Observable.empty();
            }

            @Override
            public Observable<List<Vote>> getVotesByUser(String userId) {
                return Observable.empty();
            }

            @Override
            public Observable<Vote> getVote(String mapId, String userId) {
                return Observable.empty();
            }

            @Override
            public Observable<Vote> updateVote(String mapId, String userId, CreateVoteDto createVoteDto) {
                return Observable.empty();
            }

            @Override
            public Observable<Vote> deleteVote(String mapId, String userId) {
                return Observable.empty();
            }
        };
    }
}
