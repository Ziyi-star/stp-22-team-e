package de.uniks.pioneers.service;

import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.rest.MapTemplatesApiService;
import io.reactivex.rxjava3.core.Observable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapEditorServiceTest {

    @Mock
    MapTemplatesApiService mapTemplatesApiService;

    @InjectMocks
    MapEditorService mapEditorService;

    @Test
    void uploadMap() {
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "Lobby", "Owen", "1", 1, null, null);
        when(mapTemplatesApiService.createMap(any())).thenReturn(Observable.just(mapTemplate));
        final MapTemplate result = mapEditorService.uploadMap("Lobby", "Owen", null, null, null).blockingFirst();
        assertEquals(result, mapTemplate);
    }

    @Test
    void updateMap() {
        MapTemplate mapTemplate = new MapTemplate("1", "2", "3", "Lobby", "Owen", "1", 1, null, null);
        when(mapTemplatesApiService.editMap(any(), any())).thenReturn(Observable.just(mapTemplate));
        final MapTemplate result = mapEditorService.updateMap("3", "Lobby", "Owen", null, null, null).blockingFirst();
        assertEquals(result, mapTemplate);
    }

    @Test
    void encodeImageToString() {
        String result = mapEditorService.encodeImageToString(new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB), ".png");
        assertEquals(result, "data:image/png;base64,");
    }
}
