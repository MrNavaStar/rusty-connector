package group.aelysium.rustyconnector.plugin.velocity.lib;

import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;

import java.util.Timer;
import java.util.TimerTask;

public class Clock {
    private final Timer timer = new Timer();
    private long delay = 10;

    public Clock(long delay) {
        this.delay = delay;
    }

    public void start(Callable callback) {
        PluginLogger logger = VelocityRustyConnector.getAPI().getLogger();
        this.timer.schedule( new TimerTask() {
            public void run() {
                try {
                    callback.execute();
                } catch (Exception e) {
                    logger.log(e.getMessage());
                }
            }
        }, 0, this.delay*1000);
    }

    public void end() {
        this.timer.cancel();
        this.timer.purge();
    }
}