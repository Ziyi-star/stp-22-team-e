package de.uniks.pioneers;

import dagger.BindsInstance;
import dagger.Component;
import de.uniks.pioneers.controller.LoginController;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.StringToKeyCodeService;

import javax.inject.Singleton;

@Component(modules = {MainModule.class, HttpModule.class, PrefModule.class})
@Singleton
public interface MainComponent {

    PrefService prefService();

    StringToKeyCodeService stringToKeyCodeService();

    LoginController loginController();


    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder mainApp(App app);

        MainComponent build();
    }
}
