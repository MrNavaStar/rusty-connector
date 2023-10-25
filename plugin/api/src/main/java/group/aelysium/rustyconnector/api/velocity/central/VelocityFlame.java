package group.aelysium.rustyconnector.api.velocity.central;

import group.aelysium.rustyconnector.api.velocity.util.Version;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceableService;
import net.kyori.adventure.text.Component;

import java.util.*;

/**
 * The core RustyConnector kernel.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link VelocityTinder}.
 */
public abstract class VelocityFlame<TCoreServiceHandler extends ICoreServiceHandler> extends ServiceableService<TCoreServiceHandler> {
    public VelocityFlame(TCoreServiceHandler services) {
        super(services);
    }

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    public abstract Version version();

    /**
     * Gets the current version being used by RustyConnector's config manager.
     * @return {@link Integer}
     */
    public abstract int configVersion();

    /**
     * Gets RustyConnector's boot log.
     * The log represents all the debug messages sent during the boot or reboot of RustyConnector.
     * The log is in the same order of when the logs were sent.
     * @return {@link List<Component>}
     */
    public abstract List<Component> bootLog();
}