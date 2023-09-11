package group.aelysium.rustyconnector.core.lib.connectors.config;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket.WebSocketConnector;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import io.lettuce.core.protocol.ProtocolVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.List;

public class ConnectorsConfig extends YAML {
    private static ConnectorsConfig config;

    private ConnectorsConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public static ConnectorsConfig getConfig() {
        return config;
    }

    public static ConnectorsConfig newConfig(File configPointer, String template) {
        config = new ConnectorsConfig(configPointer, template);
        return ConnectorsConfig.getConfig();
    }

    @SuppressWarnings("unchecked")
    public ConnectorsService register(char[] privateKey, boolean loadMessengers, boolean loadStorage) throws IllegalStateException {
        ConnectorsService connectorsService = new ConnectorsService();

        if(loadMessengers)
            YAML.get(this.data,"messengers").getChildrenList().forEach(node -> {
                String name = this.getNode(node, "name", String.class);
                ConnectorsService.MessengerConnectors type = ConnectorsService.MessengerConnectors.valueOf(this.getNode(node, "type", String.class).toUpperCase());
                String host = this.getNode(node, "host", String.class);
                if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
                int port = this.getNode(node, "port", Integer.class);
                InetSocketAddress address = new InetSocketAddress(host, port);

                String user = this.getNode(node, "user", String.class);
                if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
                char[] password = this.getNode(node, "password", String.class).toCharArray();
                UserPass userPass = new UserPass(user, password);

                switch (type) {
                    case REDIS -> {
                        ProtocolVersion protocol = ProtocolVersion.RESP2;
                        try {
                            protocol = ProtocolVersion.valueOf(this.getNode(node, "protocol", String.class));
                        } catch (Exception ignore) {}

                        String dataChannel = this.getNode(node, "data-channel", String.class);
                        if (dataChannel.equals("")) throw new IllegalStateException("Please configure your connector settings. `dataChannel` cannot be empty for Redis connectors.");

                        connectorsService.add(name, (Connector) RedisConnector.create(address, userPass, protocol, dataChannel, privateKey));
                    }
                    case RABBITMQ -> {
                    }
                    case WEBSOCKET -> {
                        char[] connectKey = null;
                        try {
                            connectKey = this.getNode(node, "key", String.class).toCharArray();
                        } catch (Exception ignore) {}

                        connectorsService.add(name, (Connector) WebSocketConnector.create(address, connectKey, privateKey));
                    }
                }
            });

        if(loadStorage)
            get(this.data,"storage").getChildrenList().forEach(node -> {
                String name = this.getNode(node, "name", String.class);

                ConnectorsService.StorageConnectors type = ConnectorsService.StorageConnectors.valueOf(this.getNode(node, "type", String.class).toUpperCase());
                String host = this.getNode(node, "host", String.class);
                if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
                int port = this.getNode(node, "port", Integer.class);
                InetSocketAddress address = new InetSocketAddress(host, port);

                String user = this.getNode(node, "user", String.class);
                if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
                char[] password = this.getNode(node, "password", String.class).toCharArray();
                UserPass userPass = new UserPass(user, password);

                switch (type) {
                    case MYSQL -> {

                    }
                }
            });

        return connectorsService;
    }
}
