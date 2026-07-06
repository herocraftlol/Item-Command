package fr.itemcommand.model;

import org.bukkit.Material;

/**
 * Représente un item configuré qui déclenche une commande au clic droit.
 */
public class TriggerItem {

    private final String id;
    private final String displayName;      // Nom avec codes couleur (§)
    private final Material material;
    private final int slot;                // -1 = n'importe quel slot hotbar
    private final String command;          // %player% remplacé par le nom du joueur
    private final String permission;       // null = aucune permission requise
    private final boolean onlyHotbar;

    public TriggerItem(String id, String displayName, Material material,
                       int slot, String command, String permission, boolean onlyHotbar) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.slot = slot;
        this.command = command;
        this.permission = permission;
        this.onlyHotbar = onlyHotbar;
    }

    public String getId()          { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial()  { return material; }
    public int getSlot()           { return slot; }
    public String getCommand()     { return command; }
    public String getPermission()  { return permission; }
    public boolean isOnlyHotbar()  { return onlyHotbar; }

    /**
     * Construit la commande finale en remplaçant %player% par le nom du joueur.
     */
    public String buildCommand(String playerName) {
        return command.replace("%player%", playerName);
    }

    /**
     * Vérifie si le slot correspond (ou si slot == -1 = n'importe quel slot hotbar 0-8).
     */
    public boolean matchesSlot(int heldSlot) {
        return slot == -1 || slot == heldSlot;
    }
}
