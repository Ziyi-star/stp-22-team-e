package de.uniks.pioneers;

import dagger.Module;
import dagger.Provides;
import de.uniks.pioneers.service.PrefService;

import java.util.ResourceBundle;

@Module
public class MainModule {

    // Used for language changes
    @Provides
    ResourceBundle bundle(PrefService prefService) {
        return ResourceBundle.getBundle("de/uniks/pioneers/language", prefService.getLocale());
    }
}
