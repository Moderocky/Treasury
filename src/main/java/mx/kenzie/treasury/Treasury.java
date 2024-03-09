package mx.kenzie.treasury;

import mx.kenzie.treasury.command.MoneyArgument;
import mx.kenzie.treasury.command.MoneyCommand;
import mx.kenzie.treasury.manager.SimpleBankManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class Treasury extends JavaPlugin {

    private Money money;

    public void loadDefaultManager() {
        final File folder = this.getDataFolder();
        folder.mkdirs();
        final File storage = new File(folder, "balances.bin");
        this.money = new SimpleBankManager(storage);
        new MoneyCommand().register(this);
        Bukkit.getServicesManager().register(Money.class, money, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        try {
            this.money.ensureSaved();
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to save money data.", e);
        }
        Bukkit.getServicesManager().unregisterAll(this);
        super.onDisable();
    }

    @Override
    public void onEnable() {
        this.loadDefaultManager();
        super.onEnable();
        Bukkit.getScheduler().runTaskLater(this, () -> {
            try {
                Bukkit.getServicesManager().getRegistration(Money.class).getProvider().ensureLoaded();
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unable to load money data.", e);
            }
        }, 1);
    }

}
