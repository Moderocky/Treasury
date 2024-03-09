package mx.kenzie.treasury;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface Money {

    default Price get(Player player) {
        return this.get(player.getUniqueId());
    }

    default void set(Player player, double amount) {
        this.set(player.getUniqueId(), amount);
    }

    default double add(Player player, double amount) {
        return this.add(player.getUniqueId(), amount);
    }

    default boolean subtract(Player player, double amount) {
        return this.subtract(player.getUniqueId(), amount);
    }

    default boolean transfer(Player source, Player target, double amount) {
        return this.transfer(source.getUniqueId(), target.getUniqueId(), amount);
    }

    default void reset(Player player) {
        this.reset(player.getUniqueId());
    }

    Price get(UUID uuid);

    void set(UUID uuid, double amount);

    default double add(UUID uuid, double amount) {
        final double total = this.get(uuid).doubleValue() + amount;
        this.set(uuid, total);
        return total;
    }

    default boolean transfer(UUID source, UUID target, double amount) {
        final boolean paid = this.subtract(source, amount);
        if (paid) this.add(target, amount);
        return paid;
    }

    default boolean subtract(UUID source, double amount) {
        final double value = this.get(source).doubleValue();
        if (value < amount) return false;
        this.set(source, value - amount);
        return true;
    }

    default void reset(UUID uuid) {
        this.set(uuid, 0);
    }

    /**
     * Make sure everything is saved to disk, blocking if necessary.
     * This is usually called during some kind of shut-down or force save task.
     */
    void ensureSaved() throws Exception;

    /**
     * Make sure everything is loaded from disk, blocking if necessary.
     * This will be called on start-up or during a forced reload of the data.
     */
    void ensureLoaded() throws Exception;

}
