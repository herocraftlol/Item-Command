package fr.itemcommand.manager;

import fr.itemcommand.ItemCommandPlugin;
import fr.itemcommand.model.TriggerItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemConfigManager {

    private final ItemCommandPlugin plugin;
    private final List<TriggerItem> items = new ArrayList<>();
    private long cooldownMs;

    public ItemConfigManager(ItemCommandPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        items.clear();

        cooldownMs = plugin.getConfig().getLong("cooldown-ms", 500L);

        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("items");
        if (itemsSection == null) {
            plugin.getLogger().warning("Aucune section 'items' trouvée dans config.yml !");
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection sec = itemsSection.getConfigurationSection(key);
            if (sec == null) continue;

            String rawName = sec.getString("name", "&fItem");
            String matStr  = sec.getString("material", "PAPER");
            int slot       = sec.getInt("slot", -1);
            String command = sec.getString("command", "");
            String perm    = sec.getString("permission", null);
            boolean hotbar = sec.getBoolean("only-hotbar", true);

            Material mat = Material.matchMaterial(matStr);
            if (mat == null) {
                plugin.getLogger().warning("Material inconnu pour '" + key + "' : " + matStr);
                continue;
            }

            String coloredName = translateColors(rawName);
            items.add(new TriggerItem(key, coloredName, mat, slot, command, perm, hotbar));
            plugin.getLogger().info("  ✔ Item chargé : " + key + " (" + mat + ")");
        }
    }

    public List<TriggerItem> getMatching(ItemStack held, int slot) {
        if (held == null || held.getType().isAir()) return Collections.emptyList();

        List<TriggerItem> matches = new ArrayList<>();
        for (TriggerItem ti : items) {
            if (ti.getMaterial() != held.getType()) continue;
            if (!ti.matchesSlot(slot)) continue;
            if (!matchesName(held, ti.getDisplayName())) continue;
            matches.add(ti);
        }
        return matches;
    }

    /**
     * Compare les noms en plain text pour éviter tout problème
     * d'encodage §/& entre Paper et Adventure API.
     */
    private boolean matchesName(ItemStack item, String expectedLegacy) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        // Nom de l'item en jeu → plain text
        String itemPlain = PlainTextComponentSerializer.plainText()
                .serialize(meta.displayName());

        // Nom configuré (§x) → Component → plain text
        String expectedPlain = PlainTextComponentSerializer.plainText()
                .serialize(LegacyComponentSerializer.legacySection()
                        .deserialize(expectedLegacy));

        return itemPlain.equals(expectedPlain);
    }

    public ItemStack createItemStack(TriggerItem ti) {
        ItemStack stack = new ItemStack(ti.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection()
                    .deserialize(ti.getDisplayName()));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public static String translateColors(String raw) {
        return raw.replace("&", "§");
    }

    public String getMessage(String key, String playerName) {
        String msg = plugin.getConfig().getString("messages." + key,
                "&c[ItemCmd] Message manquant : " + key);
        return translateColors(msg).replace("%player%", playerName != null ? playerName : "");
    }

    public long getCooldownMs()       { return cooldownMs; }
    public int  getItemCount()        { return items.size(); }
    public List<TriggerItem> getAll() { return Collections.unmodifiableList(items); }
}
