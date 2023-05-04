package de.uniks.pioneers;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class Constants {
    // Server Path
    public static final String BASE_URL = "https://pioneers.uniks.de/api/v4/";

    // Websocket
    public static final String WS_URL = "wss://pioneers.uniks.de/ws/v4/events?authToken=";

    // Rx java
    public static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);

    // API path constants
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_OFFLINE = "offline";
    public static final String GROUPS = "groups";
    public static final String MESSAGES = "messages";
    public static final String GAMES = "games";

    // App titles
    public static final String LOGIN_SCREEN_TITLE = "Pioneers-Login";
    public static final String REGISTER_SCREEN_TITLE = "register.screen.title";
    public static final String LOBBY_SCREEN_TITLE = "pioneers.lobby";
    public static final String LOBBY_SELECT_SCREEN_TITLE = "lobby.select.title";
    public static final String VICTORY_SCREEN_TITLE = "victory.title";
    public static final String RULES_SCREEN_TITLE = "rules.title";
    public static final String MAP_MENU_SCREEN_TITLE = "map.menu.title";
    public static final String INGAME_SCREEN_TITLE = "ingame.title";
    public static final String LOBBY_COLOR_SCREEN_TITLE = "color.select.title";
    public static final String MAP_EDITOR_SCREEN_TITLE = "map.editor.title";

    // Error custom messages
    public static final String CUSTOM_ERROR = "Something went terribly wrong";
    public static final String PASSWORD_VALIDATION_ERROR = "password.validation.error";

    // Registration
    public static final String REGISTRATION_SUCCESS = "Registration success";

    // Login
    public static final String LOGIN_ERROR = "Login Failed";
    public static final String LOGOUT_ERROR = "Logout Failed";
    public static final String LOGOUT_SUCCESS = "Logout Success";
    public static final String STATUS_ONLINE_FAILED = "status.online.failed";
    public static final String INVALID_USERNAME = "Invalid username or password";

    // Lobby select
    public static final String LOBBY_CREATION_SUCCESS = "Lobby Created";
    public static final String JOIN_LOBBY_SUCCESS = "Join Lobby success";
    public static final String JOIN_LOBBY_ERROR = "Something got terrible wrong";

    // Lobby
    public static final String LOBBY_EXIT_ERROR = "exit.lobby.failure";
    public static final String LOBBY_GET_MEMBER_INFO_ERROR = "Cannot get the member information";
    public static final String CHANGE_MEMBER_SHIP_ERROR = "Change MemberShip failure";
    public static final String CHANGE_MEMBER_SHIP_SUCCESS = "Success";
    public static final String GET_GAME_ERROR = "Failed to get game from server";
    public static final String LOBBY_DELETE_ERROR = "Delete Lobby failed";
    public static final String UPDATE_GAME_SUCCESS = "Successfully updated Game";
    public static final String UPDATE_GAME_ERROR = "update.game.failed";

    // Chat
    public static final String LOBBY_CHAT_NAME = "Lobby";
    public static final String LOBBY_CHAT_ERROR = "rate.limit.";
    public static final String REPLACEMENT_CHAR = "ÏÏ";

    public static final String REPLACEMENT_CHAR_SINGLE = "Ï";

    // Color Controller
    public static final String NO_COLOR_ERROR = "no.color.error";

    // Game constants
    public static final String ROBBER_IMAGE_PATH = "views/images/map assets/elements/robber.png";
    public static final int SCREEN_CENTER_XOFFSET = 800;
    public static final int SCREEN_CENTER_YOFFSET = 450;
    public static final int TILE_CENTER_XOFFSET = 60;
    public static final int TILE_CENTER_YOFFSET = 70;

    //credits Controller
    public static final String CREDITS_SCREEN_TITLE = "credits.title";

    public static final double NUM_BG_CENTER_XOFFSET = 16.5;
    public static final double NUM_BG_CENTER_YOFFSET = 16.5;
    public static final double TILE_RADIUS = 69.25;
    public static final String MAP_GET_ERROR = "map.get.error";
    public static final String TILE_TYPE_DUMMY = "DUMMY";
    public static final String TILE_TYPE_HARBOR = "HARBOR";
    public static final String TILE_TYPE_RANDOM = "RANDOM";

    public static final String SETTLEMENT = "settlement";

    public static final String ROAD = "road";

    public static final String CITY = "city";

    public static final int MAP_DRAG_MINIMUM_DIST = 16;

    public static final String CREATE_BUILDING_ERROR = "create.building.error";

    public static final String SETTLEMENT_TWO_ROADS = "label.error.settlement.two.roads";

    public static final String NO_CITIES_TO_UPGRADE = "label.error.no.cities.to.upgrade";

    public static final String DEVELOPMENT_CARD_ERROR = "No development cards available";

    public static final String MUSIC_MENU = "music/ancient-wind.mp3";


    public static final int MAX_ZOOM_IN = 3;
    public static final double MAX_ZOOM_OUT = 0.15;

    //Avatar Address
    public static final List<String> AVATAR_LIST = List.of("https://i.imgur.com/mUZ5eNr.png",
            "https://i.imgur.com/Ki0xjnd.png",
            "https://i.imgur.com/RgGU1Sw.png",
            "https://i.imgur.com/vGtP7pa.png",
            "https://i.imgur.com/UKbzuiW.png",
            "https://i.imgur.com/anwDbtE.png",
            "https://i.imgur.com/KXvf6a3.png",
            "https://i.imgur.com/OTSF22S.png",
            "https://i.imgur.com/dkPTxcu.png",
            "https://i.imgur.com/ZqVfKjq.png");

    public enum FIELD_MODE {
        PLACE_FOUNDING_SETTLEMENT,
        PLACE_FOUNDING_ROAD,
        PLACE_ROAD,
        PLACE_SETTLEMENT,
        PLACE_CITY,
        PLACE_ROBBER,
        OFF
    }

    public enum MUSIC {
        LOBBY("music/ancient-wind.mp3"),
        INGAME1("music/Event Music 2.mp3"),
        INGAME2("music/Event Music 1.mp3"),
        INGAME3("music/Event Music 3.mp3"),
        INGAME4("music/Event Music 4.mp3"),
        EDITOR1("music/Town-Village Theme 1.mp3"),
        EDITOR2("music/Town-Village Theme 2.mp3"),
        EDITOR3("music/Town-Village Theme 3.mp3");

        private final String text;

        MUSIC(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static final String[] inGameMusicPool = {MUSIC.INGAME1.toString(), MUSIC.INGAME2.toString(), MUSIC.INGAME3.toString(), MUSIC.INGAME4.toString()};
    public static final String[] editorMusicPool = {MUSIC.EDITOR1.toString(), MUSIC.EDITOR2.toString(), MUSIC.EDITOR3.toString()};

    public enum MUSIC_CONTEXT{
        LOBBY,
        INGAME,
        EDITOR
    }

    public enum MAP_ELEMENTS {
        DESERT("desert"),
        FIELDS("fields"),
        HILLS("hills"),
        MOUNTAINS("mountains"),
        FOREST("forest"),
        PASTURE("pasture"),
        RANDOM_TILE("RANDOM"),
        NUMBER_2("2"),
        NUMBER_3("3"),
        NUMBER_4("4"),
        NUMBER_5("5"),
        NUMBER_6("6"),
        NUMBER_8("8"),
        NUMBER_9("9"),
        NUMBER_10("10"),
        NUMBER_11("11"),
        NUMBER_12("12"),
        RANDOM_NUMBER("-2"),
        NUMBER_BG_NEUTRAL(""),
        NUMBER_BG_FIELDS("fields"),
        NUMBER_BG_HILLS("hills"),
        NUMBER_BG_MOUNTAINS("mountains"),
        NUMBER_BG_FOREST("forest"),
        NUMBER_BG_PASTURE("pasture"),
        HARBOR_GENERIC(null),
        HARBOR_RANDOM("random"),
        HARBOR_GRAIN("grain"),
        HARBOR_BRICK("brick"),
        HARBOR_ORE("ore"),
        HARBOR_LUMBER("lumber"),
        HARBOR_WOOL("wool"),
        HARBOR_PLANKS("");

        private final String text;

        MAP_ELEMENTS(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum MAP_ELEMENT_TYPE {
        TILE,
        NUMBER,
        HARBOR
    }


    public enum DRAW_LAYER {
        HARBOR_PLANKS,
        HEXAGON_TILES,
        HEXAGON_TILE_NUMBERS,
        ROADS,
        BUILDINGS,
        SELECT_CIRCLES,
        HARBOR,
        ROBBER
    }

    public enum HEX_SUBCON_UI_ELEMENT {
        SELECT_CIRCLE,
        BUILDING
    }

    public enum HEX_SUBCON_TYPE {
        POINT,
        ROAD,
        ROBBER
    }

    public enum HARBOR_TYPE {
        GRAIN("grain"),
        BRICK("brick"),
        ORE("ore"),
        LUMBER("lumber"),
        WOOL("wool"),
        GENERIC("generic"),
        RANDOM("random");

        private final String text;

        HARBOR_TYPE(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum GAME_NOTICE {
        NOT_ENOUGH_FIGURES("label.error.insufficient.figures"),
        NOT_ENOUGH_RESOURCES("label.error.insufficient.resources"),
        NO_DEVELOPMENT_CARDS_LEFT("no.development.cards.available");
        private final String text;

        GAME_NOTICE(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum ACTION {
        FOUNDING_ROLL("founding-roll"),
        FOUNDING_SETTLEMENT1("founding-settlement-1"),
        FOUNDING_SETTLEMENT2("founding-settlement-2"),
        FOUNDING_ROAD1("founding-road-1"),
        FOUNDING_ROAD2("founding-road-2"),
        BUILD("build"),
        BUILD_ROAD("build-road"),
        ROB("rob"),
        ROLL("roll"),
        DROP("drop"),
        OFFER("offer"),
        ACCEPT("accept"),
        MONOPOLY("monopoly"),
        YEAR_OF_PLENTY("year-of-plenty");

        private final String text;

        ACTION(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum DEVELOPMENT_CARDS {
        NEW_CARD("new"),
        KNIGHT("knight"),
        ROAD_BUILDING("road-building"),
        MONOPOLY("monopoly"),
        YEAR_OF_PLENTY("year-of-plenty"),
        VICTORY_POINT("victory-point");

        private final String text;

        DEVELOPMENT_CARDS(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public enum DEVELOPMENT_CARDS_IMAGE_PATH {

        KNIGHT("views/images/development_cards/development_Card_knight.png"),
        ROAD_BUILDING("views/images/development_cards/development_Card_road.png"),
        MONOPOLY("views/images/development_cards/development_Card_monopoly.png"),
        YEAR_OF_PLENTY("views/images/development_cards/development_Card_plenty.png");

        private final String text;
        DEVELOPMENT_CARDS_IMAGE_PATH(final String text){
            this.text = text;
        }
        @Override
        public String toString(){
            return text;
        }
    }
}
