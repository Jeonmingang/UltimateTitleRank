package com.minkang.ultimate.playtimetitles.managers;

import com.minkang.ultimate.playtimetitles.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ColorChatManager implements Listener {
    private final Main plugin;
    private File file;
    private FileConfiguration data;

    public ColorChatManager(Main plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "colors.yml");
        if (!file.exists()) {
            try { file.getParentFile().mkdirs(); file.createNewFile(); }
            catch (IOException ignored) {}
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public void giveColor(Player target, String krName) {
        Set<String> set = new HashSet<>(data.getStringList(target.getUniqueId().toString()+".colors"));
        set.add(krName);
        data.set(target.getUniqueId().toString()+".colors", new ArrayList<>(set));
        save();
    }

    public void removeColor(Player target, String krName) {
        Set<String> set = new HashSet<>(data.getStringList(target.getUniqueId().toString()+".colors"));
        set.remove(krName);
        data.set(target.getUniqueId().toString()+".colors", new ArrayList<>(set));
        save();
    }

    public List<String> getColors(Player target) {
        return data.getStringList(target.getUniqueId().toString()+".colors");
    }

    public String translateAllowed(Player p, String input) {
        // only allow &x where x is in player's allowed set
        List<String> allowed = getColors(p);
        Map<String,Object> map = plugin.getConfig().getConfigurationSection("colors").getValues(false);
        String out = input;
        for (Map.Entry<String,Object> e : map.entrySet()) {
            String kr = e.getKey();
            String code = String.valueOf(e.getValue());
            boolean has = allowed.contains(kr);
            String token = "&" + code;
            if (has) {
                out = out.replace(token, "§" + code);
            } else {
                // strip unauthorized codes
                out = out.replace(token, "");
            }
        }
        return ChatColor.translateAlternateColorCodes('&', out);
    }

    public void openGui(Player p) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.colorchat_title","&b색깔채팅 GUI"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        Map<String,Object> map = plugin.getConfig().getConfigurationSection("colors").getValues(false);
        int slot = 0;
        for (Map.Entry<String,Object> e : map.entrySet()) {
            String kr = e.getKey();
            String code = String.valueOf(e.getValue());
            ItemStack wool = new ItemStack(Material.WHITE_WOOL, 1);
            ItemMeta meta = wool.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&" + code + kr));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "클릭하여 이 색을 허용/해제");
            meta.setLore(lore);
            wool.setItemMeta(meta);
            inv.setItem(slot, wool);
            slot++;
            if (slot >= 53) break;
        }
        // cancel button
        ItemStack barrier = new ItemStack(Material.BARRIER, 1);
        ItemMeta bm = barrier.getItemMeta();
        bm.setDisplayName(ChatColor.RED + "색깔채팅 취소");
        barrier.setItemMeta(bm);
        inv.setItem(53, barrier);
        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem()==null || e.getView()==null) return;
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.colorchat_title","&b색깔채팅 GUI"));
        if (!e.getView().getTitle().equals(title)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player)e.getWhoClicked();
        ItemStack it = e.getCurrentItem();
        if (it.getType() == Material.BARRIER) {
            p.closeInventory();
            return;
        }
        if (!it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        // name contains KR text, extract
        String kr = name.replaceAll("\\[[^\\]]*\\]", "").trim(); // safe fallback
        // better approach: remove color codes only
        kr = kr.replaceAll("(?i)§[0-9A-FK-OR]", "");
        kr = ChatColor.stripColor(name);
        List<String> cur = getColors(p);
        if (cur.contains(kr)) {
            removeColor(p, kr);
            p.sendMessage("§7색깔 해제: " + name);
        } else {
            giveColor(p, kr);
            p.sendMessage("§a색깔 허용: " + name);
        }
    }

    public void saveAll() { save(); }
    private void save() {
        try { data.save(file); }
        catch (IOException e) { plugin.getLogger().warning("colors.yml 저장 실패: " + e.getMessage()); }
    }
}
