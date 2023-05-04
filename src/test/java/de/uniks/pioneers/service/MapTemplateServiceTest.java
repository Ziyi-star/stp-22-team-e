package de.uniks.pioneers.service;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.UpdateMapTemplateDto;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.rest.MapTemplatesApiService;
import io.reactivex.rxjava3.core.Observable;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MapTemplateServiceTest {

    @Mock
    MapTemplatesApiService mapTemplatesApiService;

    @InjectMocks
    MapTemplateService mapTemplateService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Test
    void getAllMaps() {
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "Lobby", "Owen", "1", 1, null, null);
        when(mapTemplatesApiService.getMaps(any())).thenReturn(Observable.just(List.of(mapTemplate)));
        final List<MapTemplate> result = mapTemplateService.getAllMaps().blockingFirst();
        assertEquals(result, List.of(mapTemplate));
    }

    @Test
    void getMap() {
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "Lobby", "Owen", "1", 1, null, null);
        when(mapTemplatesApiService.getMapById(any())).thenReturn(Observable.just(mapTemplate));
        final MapTemplate result = mapTemplateService.getMap("3").blockingFirst();
        assertEquals(result, mapTemplate);
    }

    @Test
    void deleteMap() {
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "Lobby", "Owen", "1", 1, null, null);
        when(mapTemplatesApiService.deleteMap(any())).thenReturn(Observable.just(mapTemplate));
        final MapTemplate result = mapTemplateService.deleteMap("3").blockingFirst();
        assertEquals(result, mapTemplate);
    }

    @Test
    void editMap() {
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "New Name", "Owen", "1", 1, null, null);
        when(mapTemplatesApiService.editMap(any(), any())).thenReturn(Observable.just(mapTemplate));
        final MapTemplate result = mapTemplateService.editMap("3", new UpdateMapTemplateDto("New Name", null, null, null, null)).blockingFirst();
        assertEquals(result, mapTemplate);
    }

    @Test
    void checkEmptyString() {
        String string = "test";
        boolean stringNotEmpty = mapTemplateService.checkEmptyString(string);
        assertTrue(stringNotEmpty);

        String string1 = "    ";
        boolean stringNotEmpty1 = mapTemplateService.checkEmptyString(string1);
        assertFalse(stringNotEmpty1);

        String string2 = "";
        boolean stringNotEmpty2 = mapTemplateService.checkEmptyString(string2);
        assertFalse(stringNotEmpty2);
    }

    @Test
    void addMapTooltip() {
        //required to load images without a scene which is done in this methode
        JFXPanel panel = new JFXPanel();
        Node node = new ImageView(new Image(Constants.AVATAR_LIST.get(1)));
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "New Name", Constants.AVATAR_LIST.get(0), "1", 1, null, null);
        mapTemplateService.addMapTooltip(node, mapTemplate);
        Tooltip tooltip = (Tooltip) node.getProperties().get("tooltip");
        assertNotNull(tooltip);
    }
}