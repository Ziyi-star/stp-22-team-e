package de.uniks.pioneers;

import dagger.Module;
import dagger.Provides;

import java.util.prefs.Preferences;

@Module
public class PrefModule {

    @Provides
    Preferences preferences() {
        return Preferences.userNodeForPackage(Main.class);
    }
}
