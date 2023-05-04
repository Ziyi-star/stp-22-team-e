package de.uniks.pioneers.service;

import de.uniks.pioneers.Constants;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Timer;
import java.util.TimerTask;

@Singleton
public class TimerService {
    private final AuthenticationService authenticationService;
    private Timer timer;
    private boolean isRunning = false;

    @Inject
    public TimerService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void startTimer() {
        timer = new Timer();
        isRunning = true;
        try {
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    authenticationService.refresh().observeOn(Constants.FX_SCHEDULER).subscribe();
                }
            };
            // triggers every hour
            timer.scheduleAtFixedRate(timerTask, 0, 3600000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stopTimer() {
        isRunning = false;
        timer.cancel();
    }

    public boolean isRunning() {
        return isRunning;
    }
}
