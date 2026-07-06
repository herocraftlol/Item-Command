package fr.itemcommand.command;

import fr.itemcommand.ItemCommandPlugin;
import fr.itemcommand.manager.ItemConfigManager;
import fr.itemcommand.model.TriggerItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /itemcmd reload           → recharge la config
 * /itemcmd give <id> [joueur] → donne un item trigger à un joueur
 */
public class ItemCmdCommand implements CommandExecutor, TabCompleter {

    private final ItemCommandPlugin plugin;

    public ItemCmdCommand(ItemCommandPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("itemcommand.admin")) {
            sender.sendMessage(ItemConfigManager.translateColors("&cPermission refusée."));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // ── /itemcmd reload
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(ItemConfigManager.translateColors(
                        plugin.getConfigManager().getMessage("reloaded", null)));
            }

            // ── /itemcmd give <id> [joueur]
            case "give" -> {
                if (args.length < 2) {
                    sender.sendMessage(ItemConfigManager.translateColors(
                            "&eUsage : /" + label + " give <id> [joueur]"));
                    return true;
                }

                String itemId = args[1];
                TriggerItem ti = plugin.getConfigManager().getAll().stream()
                        .filter(i -> i.getId().equalsIgnoreCase(itemId))
                        .findFirst().orElse(null);

                if (ti == null) {
                    sender.sendMessage(ItemConfigManager.translateColors(
                            "&cItem inconnu : &e" + itemId));
                    return true;
                }

                // Cible : arg[2] ou l'émetteur si joueur
                Player target;
                if (args.length >= 3) {
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) {
                        sender.sendMessage(ItemConfigManager.translateColors(
                                "&cJoueur introuvable : &e" + args[2]));
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                } else {
                    sender.sendMessage(ItemConfigManager.translateColors(
                            "&cPrécisez un joueur : /" + label + " give " + itemId + " <joueur>"));
                    return true;
                }

                ItemStack stack = plugin.getConfigManager().createItemStack(ti);
                target.getInventory().addItem(stack);
                sender.sendMessage(ItemConfigManager.translateColors(
                        plugin.getConfigManager().getMessage("item-given", target.getName())));
            }

            // ── /itemcmd list
            case "list" -> {
                List<TriggerItem> all = plugin.getConfigManager().getAll();
                sender.sendMessage(ItemConfigManager.translateColors(
                        "&6&l[ItemCmd] &eItems configurés (" + all.size() + ") :"));
                for (TriggerItem ti : all) {
                    sender.sendMessage(ItemConfigManager.translateColors(
                            "  &7- &f" + ti.getId()
                            + " &8| &7" + ti.getMaterial()
                            + " &8| &a→ &f" + ti.getCommand()));
                }
            }

            default -> sendHelp(sender, label);
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ItemConfigManager.translateColors("&6&l[ItemCommandPlugin] &eCommandes :"));
        sender.sendMessage(ItemConfigManager.translateColors("  &f/" + label + " reload &7- Recharge la config"));
        sender.sendMessage(ItemConfigManager.translateColors("  &f/" + label + " give <id> [joueur] &7- Donne un item"));
        sender.sendMessage(ItemConfigManager.translateColors("  &f/" + label + " list &7- Liste les items"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(List.of("reload", "give", "list"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(plugin.getConfigManager().getAll().stream()
                    .map(TriggerItem::getId)
                    .collect(Collectors.toList()));
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
