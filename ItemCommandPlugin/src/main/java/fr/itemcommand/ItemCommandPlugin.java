package fr.itemcommand;

import fr.itemcommand.command.ItemCmdCommand;
import fr.itemcommand.listener.ItemClickListener;
import fr.itemcommand.manager.ItemConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemCommandPlugin extends JavaPlugin {

    private static ItemCommandPlugin instance;
    private ItemConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        // Sauvegarde la config par défaut si absente
        saveDefaultConfig();

        // Charge les items configurés
        configManager = new ItemConfigManager(this);
        configManager.load();

        // Enregistre le listener de clic
        getServer().getPluginManager().registerEvents(new ItemClickListener(this), this);

        // Enregistre la commande /itemcmd
        ItemCmdCommand cmdExecutor = new ItemCmdCommand(this);
        getCommand("itemcmd").setExecutor(cmdExecutor);
        getCommand("itemcmd").setTabCompleter(cmdExecutor);

        getLogger().info("ItemCommandPlugin activé — " + configManager.getItemCount() + " item(s) chargé(s).");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItemCommandPlugin désactivé.");
    }

    public static ItemCommandPlugin getInstance() {
        return instance;
    }

    public ItemConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Recharge la config et les items sans redémarrer le serveur.
     */
    public void reload() {
        reloadConfig();
        configManager.load();
        getLogger().info("Configuration rechargée — " + configManager.getItemCount() + " item(s).");
    }
}
