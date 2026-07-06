# ItemCommandPlugin — Paper 1.21

**Version 2.0.0**

Plugin Paper 1.21 qui exécute une commande **console (OP)** lorsqu'un joueur
fait un **clic droit** sur un item avec un nom personnalisé dans sa hotbar.

Fonctionne sur **tous les serveurs Paper de ton réseau BungeeCord**
— dépose simplement le `.jar` dans le dossier `plugins/` de chaque serveur.

---

## 📦 Installation

1. **Compiler** (requiert Java 21 + Maven) :
   ```bash
   mvn clean package
   ```
   Le `.jar` est dans `target/ItemCommandPlugin-2.0.0.jar`.

2. **Déposer** `ItemCommandPlugin-2.0.0.jar` dans le dossier `plugins/`
   de chaque serveur Paper 1.21 du réseau.

3. **Redémarrer** le serveur. Un fichier `plugins/ItemCommandPlugin/config.yml`
   est généré automatiquement.

---

## ⚙️ Configuration (`config.yml`)

```yaml
# Délai anti-spam entre deux utilisations (ms)
cooldown-ms: 500

items:
  menu_principal:                    # Identifiant unique (ton choix)
    name: "&6&lMenu Principal"       # Nom de l'item (codes couleur &)
    material: COMPASS                # Type d'item Bukkit
    slot: -1                         # Slot hotbar 0-8, -1 = tous
    command: "menu open %player%"    # Commande console (%player% = nom du joueur)
    permission: "itemcommand.use"    # Permission requise (laisser vide = aucune)
    only-hotbar: true                # true = seulement si tenu en main

  teleport_spawn:
    name: "&a&lRetour au Spawn"
    material: BLAZE_ROD
    slot: 8                          # Seulement si dans le slot 8 (dernier slot)
    command: "spawn %player%"
    permission: "itemcommand.use"
    only-hotbar: true

messages:
  cooldown: "&cVeuillez patienter avant de réutiliser cet item."
  no-permission: "&cVous n'avez pas la permission d'utiliser cet item."
  reloaded: "&aConfiguration rechargée avec succès."
  item-given: "&aItem donné à &e%player%&a."
```

### Codes couleur disponibles
`&0` noir · `&1` bleu foncé · `&2` vert foncé · `&3` cyan · `&4` rouge foncé
`&5` violet · `&6` or · `&7` gris · `&8` gris foncé · `&9` bleu · `&a` vert
`&b` cyan clair · `&c` rouge · `&d` rose · `&e` jaune · `&f` blanc
`&l` **gras** · `&o` *italique* · `&n` souligné · `&m` barré · `&r` reset

---

## 🎮 Commandes

| Commande | Description |
|---|---|
| `/itemcmd reload` | Recharge la config sans redémarrer |
| `/itemcmd give <id> [joueur]` | Donne un item trigger à un joueur |
| `/itemcmd list` | Liste tous les items configurés |

**Permission admin :** `itemcommand.admin` (OP par défaut)

---

## 🔑 Permissions

| Permission | Description | Défaut |
|---|---|---|
| `itemcommand.admin` | Accès aux commandes `/itemcmd` | OP |
| `itemcommand.use` | Utiliser les items triggers | Tous |

---

## 📡 BungeeCord — Architecture

Le plugin fonctionne côté **Paper** (Spigot), pas côté BungeeCord proxy,
car la détection de clic droit se fait au niveau du serveur de jeu.

**Pour couvrir tout le réseau :**
- Dépose le `.jar` dans `plugins/` de **chaque serveur Paper**
- Chaque serveur a sa propre `config.yml` → personnalisation par serveur
- Utilise `/itemcmd reload` pour recharger sans redémarrer

**Exemple BungeeCord multi-serveur :**
```
proxy/
├── BungeeCord.jar
├── lobbey/plugins/ItemCommandPlugin.jar   ← config lobby
├── survival/plugins/ItemCommandPlugin.jar ← config survival
└── minijeux/plugins/ItemCommandPlugin.jar ← config minijeux
```

---

## 🛠️ Fonctionnement interne

1. Le listener capte `PlayerInteractEvent` (RIGHT_CLICK_AIR / RIGHT_CLICK_BLOCK)
2. Ignore la main secondaire pour éviter le double-déclenchement
3. Compare le material ET le nom affiché de l'item tenu
4. Vérifie le slot hotbar si configuré
5. Applique le cooldown anti-spam
6. Vérifie la permission du joueur
7. Exécute la commande via `ConsoleCommandSender` (droits OP complets)
8. Annule l'interaction pour éviter tout effet de bloc parasite
