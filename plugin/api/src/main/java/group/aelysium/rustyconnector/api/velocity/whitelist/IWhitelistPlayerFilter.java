package group.aelysium.rustyconnector.api.velocity.whitelist;

import java.util.UUID;

public interface IWhitelistPlayerFilter {
    UUID uuid();
    String username();
    String ip();
}