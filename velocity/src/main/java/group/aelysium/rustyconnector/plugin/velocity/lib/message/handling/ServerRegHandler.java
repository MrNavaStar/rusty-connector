package group.aelysium.rustyconnector.plugin.velocity.lib.message.handling;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.data_messaging.MessageHandler;
import group.aelysium.rustyconnector.core.lib.data_messaging.RedisMessage;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.net.InetSocketAddress;

public class ServerRegHandler implements MessageHandler {
    private final RedisMessage message;

    public ServerRegHandler(RedisMessage message) {
        this.message = message;
    }

    @Override
    public void execute() throws Exception {
        String familyName = message.getParameter("family");

        InetSocketAddress address = message.getAddress();

        String serverName = message.getParameter("name");

        ServerInfo serverInfo = new ServerInfo(
                serverName,
                address
        );

        PlayerServer server = new PlayerServer(
                serverInfo,
                Integer.parseInt(message.getParameter("soft-cap")),
                Integer.parseInt(message.getParameter("hard-cap")),
                Integer.parseInt(message.getParameter("weight"))
        );

        server.register(familyName);
    }
}