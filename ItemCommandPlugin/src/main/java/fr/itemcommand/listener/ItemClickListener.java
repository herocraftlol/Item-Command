package fr.itemcommand.listener;

import fr.itemcommand.ItemCommandPlugin;
import fr.itemcommand.manager.ItemConfigManager;
import fr.itemcommand.model.TriggerItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Écoute le clic droit d'un joueur et exécute la commande configurée
 * si l'item tenu correspond à un TriggerItem.
 */
public class ItemClickListener implements Listener {

    private final ItemCommandPlugin plugin;
    // Cooldown : UUID joueur -> timestamp dernier usage
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ItemClickListener(ItemCommandPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {

        // ── 1. Filtrer : clic droit uniquement (air ou bloc)
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        // ── 2. Ignorer la main secondaire (évite le double déclenchement)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        int slot = player.getInventory().getHeldItemSlot(); // 0-8

        // ── 3. Chercher les TriggerItem correspondants
        ItemConfigManager mgr = plugin.getConfigManager();
        List<TriggerItem> matches = mgr.getMatching(held, slot);
        if (matches.isEmpty()) return;

        // ── 4. Cooldown global (anti-spam)
        long now = System.currentTimeMillis();
        long cooldown = mgr.getCooldownMs();
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse != null && now - lastUse < cooldown) {
            player.sendMessage(ItemConfigManager.translateColors(
                    mgr.getMessage("cooldown", null)));
            event.setCancelled(true);
            return;
        }

        // ── 5. Traiter chaque item correspondant
        boolean executed = false;
        for (TriggerItem ti : matches) {

            // Vérification de permission
            String perm = ti.getPermission();
            if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
                player.sendMessage(ItemConfigManager.translateColors(
                        mgr.getMessage("no-permission", player.getName())));
                continue;
            }

            // Exécution de la commande en console
            String cmd = ti.buildCommand(player.getName());
            plugin.getServer().dispatchCommand(
                    plugin.getServer().getConsoleSender(), cmd);

            plugin.getLogger().info("[ItemCmd] " + player.getName()
                    + " a déclenché '" + ti.getId() + "' → " + cmd);
            executed = true;
        }

        if (executed) {
            // Annuler l'interaction pour éviter des effets de bloc indésirables
            event.setCancelled(true);
            cooldowns.put(player.getUniqueId(), now);
        }
    }
}
