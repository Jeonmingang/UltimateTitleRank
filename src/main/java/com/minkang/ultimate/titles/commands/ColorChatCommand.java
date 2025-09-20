package com.minkang.ultimate.titles.commands;

import com.minkang.ultimate.titles.UltimateTitleRank;
import com.minkang.ultimate.titles.storage.PlayerData;
import com.minkang.ultimate.titles.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ColorChatCommand implements CommandExecutor, TabCompleter {
    private final UltimateTitleRank plugin;

    public ColorChatCommand(UltimateTitleRank plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("열기")) {
            if (!sender.hasPermission("utr.user.color.open")) { sender.sendMessage(ColorUtil.colorize("&c권한이 없습니다.")); return true; }
            if (!(sender instanceof Player)) {
                sender.sendMessage("플레이어만 사용 가능합니다.");
                return true;
            }
            openGui((Player)sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("지급")) {
            if (!sender.hasPermission("utr.admin.color.give")) { sender.sendMessage(ColorUtil.colorize("&c권한이 없습니다.")); return true; }
            // 기본: 아이템(색깔권) 지급. 옵션으로 direct 주면 즉시 권한 부여.
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c사용법: /색깔채팅 지급 <색깔> <플레이어> [direct]"));
                return true;
            }
            String color = args[1];
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
            String key = resolveColorKey(color);
            if (key == null) {
                sender.sendMessage(ColorUtil.colorize("&c알 수 없는 색입니다. /색깔채팅 목록 으로 확인하세요."));
                return true;
            }
            boolean direct = args.length >= 4 && ("direct".equalsIgnoreCase(args[3]) || "즉시".equals(args[3]));
            PlayerData pd = plugin.getStorage().get(target.getUniqueId());
            if (direct) {
                if (!pd.getColorPermissions().contains(key)) pd.getColorPermissions().add(key);
                if (target.isOnline()) ((Player)target).sendMessage(ColorUtil.colorize("&a색깔채팅 권한이 지급되었습니다: &f" + key));
                sender.sendMessage(ColorUtil.colorize("&a즉시 지급 완료: " + target.getName() + " ← " + key));
                return true;
            }
            // Voucher item
            if (!target.isOnline()) {
                sender.sendMessage(ColorUtil.colorize("&c아이템 지급은 대상이 접속 중이어야 합니다. (또는 direct 사용)"));
                return true;
            }
            Player tp = (Player)target;
            ItemStack it = makeColorVoucherItem(key);
            java.util.HashMap<Integer, ItemStack> remained = tp.getInventory().addItem(it);
            if (!remained.isEmpty()) {
                tp.getWorld().dropItemNaturally(tp.getLocation(), it);
            }
            sender.sendMessage(ColorUtil.colorize("&a색깔권 지급 완료: " + target.getName() + " ← " + key));
            tp.sendMessage(ColorUtil.colorize("&a색깔채팅 권을 받았습니다! 우클릭하여 획득하세요."));
            return true;
        }
        if (args[0].equalsIgnoreCase("제거")) {
            if (!sender.hasPermission("utr.admin.color.remove")) { sender.sendMessage(ColorUtil.colorize("&c권한이 없습니다.")); return true; }
            if (args.length < 3) {
                sender.sendMessage(ColorUtil.colorize("&c사용법: /색깔채팅 제거 <플레이어> <색깔>"));
                return true;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
            String color = args[2];
            PlayerData pd = plugin.getStorage().get(target.getUniqueId());
            String key = resolveColorKey(color);
            if (key == null) {
                sender.sendMessage(ColorUtil.colorize("&c알 수 없는 색입니다. /색깔채팅 목록 으로 확인하세요."));
                return true;
            }
            pd.getColorPermissions().remove(key);
            if (key.equals(pd.getSelectedColor())) pd.setSelectedColor("WHITE");
            sender.sendMessage(ColorUtil.colorize("&a제거 완료: " + target.getName() + " 의 " + key));
            return true;
        }
        if (args[0].equalsIgnoreCase("목록")) {
            if (!sender.hasPermission("utr.user.color.list")) { sender.sendMessage(ColorUtil.colorize("&c권한이 없습니다.")); return true; }
            sender.sendMessage(ColorUtil.colorize("&e사용 가능한 색 목록:"));
            org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
            for (String k : sec.getKeys(false)) {
                String name = sec.getConfigurationSection(k).getString("name");
                sender.sendMessage(ColorUtil.colorize("&7- " + k + ": " + name));
            }
            return true;
        }
        sender.sendMessage(ColorUtil.colorize("&c알 수 없는 하위 명령입니다."));
        return true;
    }

    private ItemStack makeColorVoucherItem(String key) {
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
        String displayName = sec.getConfigurationSection(key).getString("name");
        String voucherName = plugin.getConfig().getString("colorchat.voucher.name_template", "&b색깔채팅 권: {name}")
                .replace("{name}", displayName);
        java.util.List<String> loreT = plugin.getConfig().getStringList("colorchat.voucher.lore");

        Material mat = pickMaterialForKey(key);
        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(ColorUtil.colorize(voucherName));
        java.util.List<String> lore = new java.util.ArrayList<String>();
        for (String s : loreT) lore.add(ColorUtil.colorize(s));
        lore.add(ColorUtil.colorize("&7KEY:" + key));
        im.setLore(lore);
        it.setItemMeta(im);
        return it;
    }

    private String resolveColorKey(String input) {
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
        for (String k : sec.getKeys(false)) {
            String name = sec.getConfigurationSection(k).getString("name");
            String stripped = ChatColor.stripColor(ColorUtil.colorize(name));
            if (stripped.equalsIgnoreCase(input) || k.equalsIgnoreCase(input)) return k;
        }
        if ("빨강".equalsIgnoreCase(input)) return "RED";
        if ("주황".equalsIgnoreCase(input)) return "GOLD";
        if ("노랑".equalsIgnoreCase(input)) return "YELLOW";
        if ("초록".equalsIgnoreCase(input)) return "DARK_GREEN";
        if ("연두".equalsIgnoreCase(input)) return "GREEN";
        if ("파랑".equalsIgnoreCase(input)) return "BLUE";
        if ("하늘".equalsIgnoreCase(input)) return "AQUA";
        if ("청록".equalsIgnoreCase(input)) return "DARK_AQUA";
        if ("분홍".equalsIgnoreCase(input)) return "LIGHT_PURPLE";
        if ("보라".equalsIgnoreCase(input)) return "DARK_PURPLE";
        if ("하양".equalsIgnoreCase(input) || "흰색".equalsIgnoreCase(input)) return "WHITE";
        if ("회색".equalsIgnoreCase(input)) return "GRAY";
        if ("진회색".equalsIgnoreCase(input)) return "DARK_GRAY";
        if ("검정".equalsIgnoreCase(input)) return "BLACK";
        return null;
    }

    public void openGui(Player p) {
        String title = ColorUtil.colorize(plugin.getConfig().getString("colorchat.gui_title", "&8색깔채팅 선택"));
        Inventory inv = Bukkit.createInventory(null, 27, title);
        org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
        com.minkang.ultimate.titles.storage.PlayerData pd = plugin.getStorage().get(p.getUniqueId());
        int i = 0;
        for (String k : sec.getKeys(false)) {
            String display = sec.getConfigurationSection(k).getString("name");
            boolean owned = pd.getColorPermissions().contains(k);
            Material mat = pickMaterialForKey(k);
            ItemStack it = new ItemStack(mat);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(ColorUtil.colorize(display + (owned ? " &7[보유]" : " &8[미보유]")));
            java.util.List<String> lore = new java.util.ArrayList<String>();
            lore.add(ColorUtil.colorize("&7KEY:" + k));
            lore.add(ColorUtil.colorize(owned ? "&a보유 중 - 클릭하여 선택" : "&c미보유 - 권한 필요"));
            im.setLore(lore);
            it.setItemMeta(im);
            if (i < inv.getSize()) inv.setItem(i, it);
            i++;
        }
        ItemStack cancel = new ItemStack(Material.BARRIER);
        ItemMeta cm = cancel.getItemMeta();
        cm.setDisplayName(ColorUtil.colorize(plugin.getConfig().getString("colorchat.cancel_button_name", "&7원래 색으로")));
        cancel.setItemMeta(cm);
        inv.setItem(inv.getSize()-1, cancel);
        p.openInventory(inv);
    }

    private Material pickMaterialForKey(String key) {
        if ("RED".equalsIgnoreCase(key)) return Material.RED_DYE;
        if ("GOLD".equalsIgnoreCase(key)) return Material.ORANGE_DYE;
        if ("YELLOW".equalsIgnoreCase(key)) return Material.YELLOW_DYE;
        if ("GREEN".equalsIgnoreCase(key)) return Material.LIME_DYE;
        if ("DARK_GREEN".equalsIgnoreCase(key)) return Material.GREEN_DYE;
        if ("BLUE".equalsIgnoreCase(key)) return Material.BLUE_DYE;
        if ("AQUA".equalsIgnoreCase(key)) return Material.LIGHT_BLUE_DYE;
        if ("DARK_AQUA".equalsIgnoreCase(key)) return Material.CYAN_DYE;
        if ("LIGHT_PURPLE".equalsIgnoreCase(key)) return Material.MAGENTA_DYE;
        if ("DARK_PURPLE".equalsIgnoreCase(key)) return Material.PURPLE_DYE;
        if ("WHITE".equalsIgnoreCase(key)) return Material.WHITE_DYE;
        if ("GRAY".equalsIgnoreCase(key)) return Material.GRAY_DYE;
        if ("DARK_GRAY".equalsIgnoreCase(key)) return Material.LIGHT_GRAY_DYE;
        if ("BLACK".equalsIgnoreCase(key)) return Material.BLACK_DYE;
        return Material.PAPER;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        java.util.List<String> out = new java.util.ArrayList<String>();
        if (args.length == 1) {
            out.add("열기"); out.add("지급"); out.add("제거"); out.add("목록");
        } else if ("지급".equals(args[0])) {
            if (args.length == 2) {
                // suggest colors
                org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
                for (String k : sec.getKeys(false)) {
                    out.add(k);
                    String name = sec.getConfigurationSection(k).getString("name");
                    out.add(ChatColor.stripColor(ColorUtil.colorize(name)));
                }
            } else if (args.length == 3) {
                // suggest players
                for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) out.add(p.getName());
            } else if (args.length == 4) {
                out.add("direct"); out.add("즉시");
            }
        } else if ("제거".equals(args[0])) {
            if (args.length == 2) {
                // suggest players
                for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) out.add(p.getName());
            } else if (args.length == 3) {
                // suggest colors
                org.bukkit.configuration.ConfigurationSection sec = plugin.getConfig().getConfigurationSection("colorchat.colors");
                for (String k : sec.getKeys(false)) {
                    out.add(k);
                    String name = sec.getConfigurationSection(k).getString("name");
                    out.add(ChatColor.stripColor(ColorUtil.colorize(name)));
                }
            }
        }
        return out;
    }
}
