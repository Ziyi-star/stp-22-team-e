package de.uniks.pioneers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.pioneers.dto.CreateMapTemplateDto;
import de.uniks.pioneers.dto.UpdateMapTemplateDto;
import de.uniks.pioneers.model.HarborTemplate;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.model.TileTemplate;
import de.uniks.pioneers.rest.MapTemplatesApiService;
import de.uniks.pioneers.util.ErrorHandling;
import io.reactivex.rxjava3.core.Observable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public class MapEditorService {

    private final MapTemplatesApiService mapTemplatesApiService;
    private final LoginResultStorage loginResultStorage;
    private final ObjectMapper mapper;

    @Inject
    public MapEditorService(MapTemplatesApiService mapTemplatesApiService, LoginResultStorage loginResultStorage, ObjectMapper mapper){

        this.mapTemplatesApiService = mapTemplatesApiService;
        this.loginResultStorage = loginResultStorage;
        this.mapper = mapper;
    }

    public Observable<MapTemplate> uploadMap(String name, String icon, String description, List<TileTemplate> tiles, List<HarborTemplate> harbors){
        //Simple upload map call that uploads the passed elements as a map to the server.
        CreateMapTemplateDto payload = new CreateMapTemplateDto(name,icon,description,tiles,harbors);
        return mapTemplatesApiService.createMap(payload).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new MapTemplate(errorResult, null, null, null,null,null, null,null,null,null);
        });
    }

    public Observable<MapTemplate> updateMap(String mapId, String name, String icon, String description, List<TileTemplate> tiles, List<HarborTemplate> harbors){
        //Update map call. Pass null for params you dont want to  update.
        UpdateMapTemplateDto payload = new UpdateMapTemplateDto(name,icon,description,tiles,harbors);
        return mapTemplatesApiService.editMap(mapId, payload).onErrorReturn(error -> {
            String errorResult = new ErrorHandling().handleError(error, mapper);
            return new MapTemplate(errorResult, null, null, null,null,null, null,null,null,null);
        });
    }

    public String encodeImageToString(BufferedImage image, String type) {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();
            imageString = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }

    public String generateMapThumbnail(Pane mapPane){
        //Generates a Map Thumbnail and returns it as a DATA URI
        WritableImage snapshot = mapPane.snapshot(null,null);
        BufferedImage thumbnail = null;
        try {
            thumbnail = Thumbnails.of(SwingFXUtils.fromFXImage(snapshot, null))
                    .size(180,180).keepAspectRatio(true).outputQuality(1.0).asBufferedImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image temp = makeColorTransparent(thumbnail, Color.WHITE);
        thumbnail = new BufferedImage(180, 180, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = thumbnail.createGraphics();
        g2.drawImage(temp, 0,0,null);
        g2.dispose();
        return encodeImageToString(thumbnail, "png");
    }

    public static Image makeColorTransparent(BufferedImage im, final Color color) {
        ImageFilter filter = new RGBImageFilter() {

            // the color we are looking for... Alpha bits are set to opaque
            public int markerRGB = color.getRGB() | 0xFF000000;
            public final int filterRGB(int x, int y, int rgb) {
                if ((rgb | 0xFF000000) == markerRGB) {
                    // Mark the alpha bits as zero - transparent
                    return 0x00FFFFFF & rgb;
                } else {
                    // nothing to do
                    return rgb;
                }
            }
        };

        ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
        return Toolkit.getDefaultToolkit().createImage(ip);
    }
}

