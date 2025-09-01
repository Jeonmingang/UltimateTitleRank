
package com.minkang.ultimate.managers;

import com.minkang.ultimate.UltimateTitleRank;
import com.minkang.ultimate.util.Texts;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TitleManager {

    private final UltimateTitleRank plugin;
    private final File playersFile;
    private final File titlesFile;
    private YamlConfiguration players;
    private YamlConfiguration titles;
    private final NamespacedKey pdcKey;
    private final NamespacedKey voucherKey;
    private final java.util.Set<java.util.UUID> awaiting = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private final java.util.Map<java.util.UUID, Integer> awaitingTimeoutTask = new java.util.HashMap<>();
    private int saveTaskId = -1;

    public TitleManager(UltimateTitleRank plugin) {
        this.plugin = plugin;
        this.playersFile = new File(plugin.getDataFolder(), "data/players.yml");
        this.titlesFile = new File(plugin.getDataFolder(), "data/titles.yml");
        this.pdcKey = new NamespacedKey(plugin, "title_name");
        this.voucherKey = new NamespacedKey(plugin, "title_voucher");
    }

    public void load() {
        try {
            if (!playersFile.getParentFile().exists()) playersFile.getParentFile().mkdirs();
            if (!playersFile.exists()) playersFile.createNewFile();
            if (!titlesFile.getParentFile().exists()) titlesFile.getParentFile().mkdirs();
            if (!titlesFile.exists()) titlesFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        players = YamlConfiguration.loadConfiguration(playersFile);
        titles = YamlConfiguration.loadConfiguration(titlesFile);
        save(); // ensure structure
    }

    public void save() {
        try {
            if (players != null) players.save(playersFile);
            if (titles != null) titles.save(titlesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startAutoSave() {
        int interval = plugin.getConfig().getInt("storage.save_interval_seconds", 120);
        saveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::save, interval * 20L, interval * 20L);
    }

    public void stopAutoSave() {
        if (saveTaskId != -1) Bukkit.getScheduler().cancelTask(saveTaskId);
    }

    // ===== Title book creation / parsing =====
    public ItemStack createTitleBook(String rawTitle) {
        FileConfiguration cfg = plugin.getConfig();
        String name = cfg.getString("title_book.name", "&d칭호북: &f%title%").replace("%title%", rawTitle);
        List<String> lore = cfg.getStringList("title_book.lore").stream()
                .map(s -> s.replace("%title%", rawTitle)).collect(Collectors.toList());
        String materialStr = cfg.getString("title_book.material", "ENCHANTED_BOOK");
        boolean glow = cfg.getBoolean("title_book.glow", true);

        Material mat = Material.getMaterial(materialStr);
        if (mat == null) mat = Material.ENCHANTED_BOOK;

        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Texts.color(name));
        meta.setLore(lore.stream().map(Texts::color).collect(Collectors.toList()));
        if (glow) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, rawTitle);
        item.setItemMeta(meta);
        if (glow) {
            item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        }
        return item;
    }

    public String parseTitleFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String t = meta.getPersistentDataContainer().get(pdcKey, PersistentDataType.STRING);
        return t;
    }

    
    public void addGlobalTitle(String title) {
        Set<String> all = new LinkedHashSet<>(titles.getStringList("all"));
        if (all.add(title)) titles.set("all", new ArrayList<>(all));
    }
    // ===== Players / Titles storage =====
    public Set<String> getOwnedTitles(UUID uuid) {
        List<String> list = players.getStringList(uuid.toString() + ".owned");
        return new LinkedHashSet<>(list);
    }

    public void addOwnedTitle(UUID uuid, String title) {
        Set<String> set = getOwnedTitles(uuid);
        if (set.add(title)) {
            players.set(uuid.toString() + ".owned", new ArrayList<>(set));
        }
        // add to global titles registry
        Set<String> all = new LinkedHashSet<>(titles.getStringList("all"));
        if (all.add(title)) titles.set("all", new ArrayList<>(all));
    }

    public String getSelectedTitle(UUID uuid) {
        return players.getString(uuid.toString() + ".selected", null);
    }

    public void setSelectedTitle(UUID uuid, String title) {
        players.set(uuid.toString() + ".selected", title);
    }

    public List<String> getAllTitles() {
        return titles.getStringList("all");
    }

    // ===== GUI =====
    public void openTitleGUI(Player player) {
        FileConfiguration cfg = plugin.getConfig();
        int rows = Math.max(1, Math.min(6, cfg.getInt("gui.rows", 3)));
        int size = rows * 9;
        String title = Texts.color(cfg.getString("gui.title", "&8보유한 칭호 선택"));

        Inventory inv = Bukkit.createInventory(player, size, title);

        Material fillerMat = Material.getMaterial(cfg.getString("gui.filler", "BLACK_STAINED_GLASS_PANE"));
        ItemStack filler = new ItemStack(fillerMat == null ? Material.BLACK_STAINED_GLASS_PANE : fillerMat);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < size; i++) inv.setItem(i, filler);

        Set<String> owned = getOwnedTitles(player.getUniqueId());
        int idx = 0;
        for (String t : owned) {
            if (idx >= size) break;
            ItemStack paper = new ItemStack(Material.NAME_TAG);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(Texts.color("&f" + t));
            List<String> lore = new ArrayList<>();
            lore.add(Texts.color("&7좌클릭: 적용"));
            meta.setLore(lore);
            paper.setItemMeta(meta);
            inv.setItem(idx, paper);
            idx++;
        }
        player.openInventory(inv);
    

    public org.bukkit.inventory.ItemStack createCustomTitleVoucher() {
        org.bukkit.configuration.file.FileConfiguration cfg = plugin.getConfig();
        String name = cfg.getString("custom_title_voucher.name", "&b칭호권");
        java.util.List<String> lore = cfg.getStringList("custom_title_voucher.lore");
        String materialStr = cfg.getString("custom_title_voucher.material", "NAME_TAG");
        boolean glow = cfg.getBoolean("custom_title_voucher.glow", true);

        org.bukkit.Material mat = org.bukkit.Material.getMaterial(materialStr);
        if (mat == null) mat = org.bukkit.Material.NAME_TAG;

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(mat, 1);
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(com.minkang.ultimate.util.Texts.color(name));
        java.util.List<String> clore = new java.util.ArrayList<>();
        for (String l : lore) clore.add(com.minkang.ultimate.util.Texts.color(l));
        meta.setLore(clore);
        if (glow) meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS, org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(voucherKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte)1);
        item.setItemMeta(meta);
        if (glow) item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        return item;
    }

    public boolean isVoucher(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte b = item.getItemMeta().getPersistentDataContainer().get(voucherKey, org.bukkit.persistence.PersistentDataType.BYTE);
        return b != null && b == (byte)1;
    }

    public void beginAwaiting(java.util.UUID uuid) {
        awaiting.add(uuid);
        int sec = plugin.getConfig().getInt("custom_title_voucher.input_timeout_seconds", 30);
        Integer old = awaitingTimeoutTask.get(uuid);
        if (old != null) org.bukkit.Bukkit.getScheduler().cancelTask(old);
        int task = org.bukkit.Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (awaiting.remove(uuid)) {
                awaitingTimeoutTask.remove(uuid);
                org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    String msg = plugin.getConfig().getString("custom_title_voucher.timeout_message","&c시간 초과로 취소되었습니다.");
                    p.sendMessage(com.minkang.ultimate.util.Texts.color(msg));
                }
            }
        }, sec * 20L);
        awaitingTimeoutTask.put(uuid, task);
    }

    public boolean isAwaiting(java.util.UUID uuid) {
        return awaiting.contains(uuid);
    }

    public void endAwaiting(java.util.UUID uuid) {
        awaiting.remove(uuid);
        Integer old = awaitingTimeoutTask.remove(uuid);
        if (old != null) org.bukkit.Bukkit.getScheduler().cancelTask(old);
    }

}
