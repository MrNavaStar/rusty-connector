package group.aelysium.rustyconnector.plugin.paper.commands;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.LongArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.parsers.PlayerArgument;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.message.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.message.cache.MessageCache;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.database.Redis;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class CommandRusty {
    public static void create(PaperCommandManager<CommandSender> manager) {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();
        final Command.Builder<CommandSender> builder = manager.commandBuilder("rc","rusty","rustyconnector");

        manager.command(builder.literal("retrieveMessage")
                .senderType(ConsoleCommandSender.class)
                .argument(LongArgument.of("snowflake"), ArgumentDescription.of("Message ID"))
                .handler(context -> manager.taskRecipe().begin(context)
                    .synchronous(commandContext -> {
                        try {
                            final Long snowflake = commandContext.get("snowflake");

                            MessageCache messageCache = PaperRustyConnector.getInstance().getVirtualServer().getMessageCache();

                            CacheableMessage message = messageCache.getMessage(snowflake);

                            (new LangMessage(plugin.logger()))
                                    .insert(Lang.boxedMessage(
                                            "Found message with ID "+snowflake.toString(),
                                            Lang.spacing(),
                                            "ID: "+message.getSnowflake(),
                                            "Contents: "+message.getContents(),
                                            "Date: "+message.getDate().toString()
                                    ))
                                    .print();
                        } catch (NullPointerException e) {
                            plugin.logger().log("That message either doesn't exist or is no-longer available in the cache!");
                        } catch (Exception e) {
                            plugin.logger().log("An error stopped us from getting that message!", e);
                        }
                    }).execute())

        ).command(builder.literal("send")
                .senderType(ConsoleCommandSender.class)
                .argument(PlayerArgument.of("player"), ArgumentDescription.of("Player"))
                .argument(StringArgument.of("family-name"), ArgumentDescription.of("Family Name"))
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                final Player player = commandContext.get("player");
                                final String familyName = commandContext.get("family-name");

                                plugin.getVirtualServer().sendToOtherFamily(player,familyName);
                            } catch (NullPointerException e) {
                                plugin.logger().log("That message either doesn't exist or is no-longer available in the cache!");
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from getting that message!", e);
                            }
                        }).execute())
        ).command(builder.literal("register")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                plugin.getVirtualServer().registerToProxy();
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from sending your request!", e);
                            }
                        }).execute())
        ).command(builder.literal("unregister")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> manager.taskRecipe().begin(context)
                        .synchronous(commandContext -> {
                            try {
                                plugin.getVirtualServer().unregisterFromProxy();
                            } catch (Exception e) {
                                plugin.logger().log("An error stopped us from sending your request!", e);
                            }
                        }).execute())
        );
    }
}