package de.uniks.pioneers.service;

import de.uniks.pioneers.Main;
import de.uniks.pioneers.dto.UpdateMapTemplateDto;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.rest.MapTemplatesApiService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class MapTemplateService {

    private final MapTemplatesApiService mapTemplatesApiService;
    private final ResourceBundle resourceBundle;

    @Inject
    public MapTemplateService(MapTemplatesApiService mapTemplatesApiService, ResourceBundle resourceBundle) {
        this.mapTemplatesApiService = mapTemplatesApiService;
        this.resourceBundle = resourceBundle;
    }

    public Observable<List<MapTemplate>> getAllMaps() {
        return mapTemplatesApiService.getMaps(null);
    }

    public Observable<MapTemplate> getMap(String mapId) {
        return mapTemplatesApiService.getMapById(mapId);
    }

    public Observable<MapTemplate> deleteMap(String id) {
        return this.mapTemplatesApiService.deleteMap(id);
    }

    public Observable<MapTemplate> editMap(String id, UpdateMapTemplateDto updateMapTemplateDto) {
        return this.mapTemplatesApiService.editMap(id, updateMapTemplateDto);
    }

    public boolean checkEmptyString(String string) {
        string = string.replace(" ", "");
        string = string.replace("\n", "");
        return !string.equals("");
    }

    public void addMapTooltip(Node node, MapTemplate map) {
        String image = map.icon();
        boolean isDefaultMap = image != null && image.startsWith("views/images");
        Image mapImage = image != null ? !isDefaultMap ? new Image(image) : new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))) :
                new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/no_map_icon.png")));
        String tooltipText = map.description() != null && checkEmptyString(map.description()) ? map.description() : resourceBundle.getString("no.description");
        Tooltip tooltip = addMapTooltip(mapImage, tooltipText);
        node.getProperties().put("tooltip", tooltip);
        Tooltip.install(node, tooltip);
    }

    public Tooltip addMapTooltip(Image mapImage, String description) {
        Tooltip tooltip = new Tooltip(description);
        tooltip.setShowDelay(new Duration(100));
        tooltip.setShowDuration(Duration.INDEFINITE);
        ImageView imageView = new ImageView(mapImage);
        //this code can be used if a fixed image size is wanted
        //double width = imageView.getImage().getWidth();
        //double height = imageView.getImage().getHeight();
        //imageView.setFitHeight(height / width * 380);
        //imageView.setFitWidth(380);
        tooltip.setGraphic(imageView);
        return tooltip;
    }
}
