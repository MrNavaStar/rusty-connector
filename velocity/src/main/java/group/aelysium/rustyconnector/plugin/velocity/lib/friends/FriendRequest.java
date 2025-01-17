package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;

public class FriendRequest {
    private long id;
    private FakePlayer sender;
    private FakePlayer target;
    private Boolean isAcknowledged = null;

    public FriendRequest(long id, FakePlayer sender, FakePlayer target) {
        this.sender = sender;
        this.target = target;
    }
    public FriendRequest(long id, Player sender, Player target) {
        this.sender = FakePlayer.from(sender);
        this.target = FakePlayer.from(target);
    }

    public long id() {
        return this.id;
    }
    public FakePlayer sender() {
        return this.sender;
    }
    public FakePlayer target() {
        return this.target;
    }


    /**
     * Accept the party invite.
     * This will subsequently connect the player to the party's server and then decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void accept() {
        VelocityAPI api = VelocityAPI.get();
        if(api.services().friendsService().orElse(null) == null)
            throw new IllegalStateException("The friends module is disabled!");
        FriendsService friendsService = api.services().friendsService().orElseThrow();

        try {
            if (friendsService.friendCount(this.target).orElseThrow() > friendsService.settings().maxFriends())
                throw new IllegalStateException("You've already maxed out the number of friends you can have.");
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("There was a fatal error accepting your friend request!");
        }

        if(this.isAcknowledged != null)
            throw new IllegalStateException("This invite has already been acknowledged! You should close it using `PartyService#closeInvite`");

        try {
            friendsService.services().dataEnclave().addFriend(this.sender, this.target);

            try {
                Player resolved = this.target.resolve().orElseThrow();
                resolved.sendMessage(Component.text("You and " + this.sender().username() + " are now friends!", NamedTextColor.GREEN));
            } catch (NoSuchElementException ignore) {}
            try {
                Player resolved = this.sender.resolve().orElseThrow();
                resolved.sendMessage(Component.text("You and " + this.target().username() + " are now friends!", NamedTextColor.GREEN));
            } catch (NoSuchElementException ignore) {}

            friendsService.closeInvite(this);
            this.isAcknowledged = true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("There was a fatal error accepting this friend request!");
        }
    }

    /**
     * Deny the party invite.
     * This will subsequently decompose the invite and remove it from the PartyService that it belongs to.
     */
    public synchronized void ignore() {
        VelocityAPI api = VelocityAPI.get();
        if(api.services().friendsService().orElse(null) == null)
            throw new IllegalStateException("The friends module is disabled!");
        FriendsService friendsService = api.services().friendsService().orElseThrow();

        try {
            friendsService.closeInvite(this);
            this.isAcknowledged = true;
        } catch (Exception ignore) {
            throw new IllegalStateException("There was a fatal error ignoring this friend request!");
        }
    }

    public synchronized void decompose() {
        this.id = 0;
        this.sender = null;
        this.target = null;
    }
}