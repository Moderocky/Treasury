package mx.kenzie.treasury.command;

import mx.kenzie.centurion.*;
import mx.kenzie.treasury.Money;
import mx.kenzie.treasury.Price;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.ServicesManager;

import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class MoneyCommand extends MinecraftCommand {

    public static final TypedArgument<Price> AMOUNT = new AnyArgument<>(Price.class)
        .with(new MoneyArgument(true)).with(Arguments.DOUBLE, Price::valueOf).labelled("amount");

    private final ServicesManager manager;

    public MoneyCommand() {
        super("Check and exchange money with other players.");
        this.manager = Bukkit.getServicesManager();
        assert manager.getRegistration(Money.class) != null;
    }

    @Override
    public MinecraftBehaviour create() {
        return this.command("money", "economy", "eco", "balance", "bal")
            .permission("treasury.command.money", PermissionDefault.TRUE)
            .arg("check", this::check)
            .description("Check your account balance.")
            .arg("check", KNOWN_PLAYER, this::check)
            .permission("treasury.command.money.admin", PermissionDefault.OP)
            .description("Check another player's account balance.")
            .arg("pay", KNOWN_PLAYER, AMOUNT, this::pay)
            .description("Pay money to another player.")
            .arg("top", this::top)
            .description("View the rich list.")
            .arg("set", KNOWN_PLAYER, AMOUNT, this::set)
            .permission("treasury.command.money.admin", PermissionDefault.OP)
            .description("Edit a player's balance.");
    }

    private CommandResult top(CommandSender sender, Arguments arguments) {
        // todo
        return CommandResult.PASSED;
    }

    private CommandResult check(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player source)) return CommandResult.LAPSE;
        final boolean other = !arguments.isEmpty();
        final Price money = this.economy().get(source);
        final ColorProfile profile = this.getProfile();
        //<editor-fold desc="Message" defaultstate="collapsed">
        if (other) sender.sendMessage(Component.textOfChildren(
            text("Their balance is ", profile.dark()),
            money,
            text(".", profile.dark())
        ));
        else sender.sendMessage(Component.textOfChildren(
            text("Your balance is ", profile.dark()),
            money,
            text(".", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private CommandResult pay(CommandSender sender, Arguments arguments) {
        if (!(sender instanceof Player source)) return CommandResult.LAPSE;
        final OfflinePlayer target = arguments.get(0);
        final Price amount = arguments.get(1);
        final ColorProfile profile = this.getProfile();
        if (amount.isNegative()) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You must pay a positive amount.", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        final Money money = this.economy();
        if (!money.transfer(source.getUniqueId(), target.getUniqueId(), amount.doubleValue())) {
            //<editor-fold desc="Message" defaultstate="collapsed">
            sender.sendMessage(Component.textOfChildren(
                text("You cannot afford to send ", profile.dark()),
                amount,
                text(".", profile.dark())
            ));
            //</editor-fold>
            return CommandResult.PASSED;
        }
        //<editor-fold desc="Message" defaultstate="collapsed">
        sender.sendMessage(Component.textOfChildren(
            text("You have sent ", profile.dark()),
            amount,
            text(" to ", profile.dark()),
            this.displayName(target).color(profile.highlight()),
            text(".", profile.dark())
        ));
        if (target.getPlayer() != null) target.getPlayer().sendMessage(Component.textOfChildren(
            source.displayName().color(profile.highlight()),
            text(" has sent you ", profile.dark()),
            amount,
            text(".", profile.dark())
        ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private CommandResult set(CommandSender sender, Arguments arguments) {
        final Money money = this.economy();
        final OfflinePlayer target = arguments.get(0);
        final Price amount = arguments.get(1);
        final ColorProfile profile = this.getProfile();
        final Price original = money.get(target.getUniqueId());
        money.set(target.getUniqueId(), amount.doubleValue());
        //<editor-fold desc="Message" defaultstate="collapsed">
        if (!Objects.equals(target, sender)) {
            sender.sendMessage(Component.textOfChildren(
                this.displayName(target).color(profile.highlight()),
                text("'s balance was changed from ", profile.dark()),
                original,
                text(" to ", profile.dark()),
                amount,
                text(".", profile.dark())
            ));
        }
        if (target.getPlayer() != null)
            target.getPlayer().sendMessage(Component.textOfChildren(
                text("Your balance was changed from ", profile.dark()),
                original,
                text(" to ", profile.dark()),
                amount,
                text(".", profile.dark())
            ));
        //</editor-fold>
        return CommandResult.PASSED;
    }

    private Money economy() {
        return manager.getRegistration(Money.class).getProvider();
    }

    protected Component displayName(OfflinePlayer player) {
        if (player == null || player.getName() == null) return Component.text("Unknown Player");
        if (player.isOnline()) return player.getPlayer().displayName();
        else return Component.text(player.getName());
    }

}
