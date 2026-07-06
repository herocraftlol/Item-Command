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

public class ItemClickListener implements Listener {

    private final ItemCommandPlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public ItemClickListener(ItemCommandPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {

        // ── 1. Clic droit uniquement
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        // ── 2. Main principale uniquement (évite le double déclenchement)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();
        int slot = player.getInventory().getHeldItemSlot();

        // ── 3. Annuler l'action par défaut de l'item immédiatement
        //       (boussole, livre, arc, etc. ont des actions natives indésirables)
        event.setCancelled(true);

        // ── 4. Chercher les TriggerItem correspondants
        ItemConfigManager mgr = plugin.getConfigManager();
        List<TriggerItem> matches = mgr.getMatching(held, slot);
        if (matches.isEmpty()) {
            // Pas un item configuré → on remet l'event comme avant
            event.setCancelled(false);
            return;
        }

        // ── 5. Cooldown anti-spam
        long now = System.currentTimeMillis();
        long cooldown = mgr.getCooldownMs();
        Long lastUse = cooldowns.get(player.getUniqueId());
        if (lastUse != null && now - lastUse < cooldown) {
            player.sendMessage(ItemConfigManager.translateColors(
                    mgr.getMessage("cooldown", null)));
            return;
        }

        // ── 6. Traiter chaque item correspondant
        boolean executed = false;
        for (TriggerItem ti : matches) {

            String perm = ti.getPermission();
            if (perm != null && !perm.isEmpty() && !player.hasPermission(perm)) {
                player.sendMessage(ItemConfigManager.translateColors(
                        mgr.getMessage("no-permission", player.getName())));
                continue;
            }

            // Commande exécutée PAR LE JOUEUR (comme s'il la tapait lui-même)
            String cmd = ti.buildCommand(player.getName());
            plugin.getServer().dispatchCommand(player, cmd);

            plugin.getLogger().info("[ItemCmd] " + player.getName()
                    + " a déclenché '" + ti.getId() + "' → /" + cmd);
            executed = true;
        }

        if (executed) {
            cooldowns.put(player.getUniqueId(), now);
        }
    }
}
