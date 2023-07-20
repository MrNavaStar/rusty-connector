package group.aelysium.rustyconnector.plugin.paper.lib.magic_link;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPing;
import group.aelysium.rustyconnector.core.lib.model.ClockService;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.lib.services.RedisMessagerService;

import java.util.concurrent.atomic.AtomicInteger;

import static group.aelysium.rustyconnector.plugin.paper.central.Processor.ValidServices.REDIS_MESSAGER_SERVICE;

public class MagicLinkService extends ClockService {
    private AtomicInteger upcomingPingDelay = new AtomicInteger(5);
    private Status status = Status.SEARCHING;

    public MagicLinkService(int threads) {
        super(threads);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Set the ping delay for this upcoming ping.
     * @param delay The delay to set.
     */
    public void setUpcomingPingDelay(int delay) {
        upcomingPingDelay.set(delay);
    }

    private void scheduleNextPing() {
        RedisMessagerService service = PaperRustyConnector.getAPI().getService(REDIS_MESSAGER_SERVICE).orElseThrow();

        this.scheduleDelayed(() -> {
            try {
                service.pingProxy(RedisMessageServerPing.ConnectionIntent.CONNECT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MagicLinkService.this.scheduleNextPing();
        }, this.upcomingPingDelay.get());
    }

    public void startHeartbeat() {
        this.scheduleNextPing();
    }

    public enum Status {
        CONNECTED,
        SEARCHING,
        DENIED
    }

    public void disconnect() {
        RedisMessagerService service = PaperRustyConnector.getAPI().getService(REDIS_MESSAGER_SERVICE).orElseThrow();
        service.pingProxy(RedisMessageServerPing.ConnectionIntent.DISCONNECT);
    }

    @Override
    public void kill() {
        super.kill();
    }
}
