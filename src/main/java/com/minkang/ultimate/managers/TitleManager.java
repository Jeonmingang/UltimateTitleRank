
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
import org.bukkit.persistence.PersistentDataContainer;
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

    // Awaiting chat input for custom title via voucher
    private final Set<UUID> awaiting = Collections.synchronizedSet(new HashSet<>());
    private final Map<UUID, Integer> awaitingTimeoutTask = new HashMap<>();

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
        // Store raw title in PDC
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
        // also ensure it's in global titles
        addGlobalTitle(title);
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

    public void addGlobalTitle(String title) {
        Set<String> all = new LinkedHashSet<>(titles.getStringList("all"));
        if (all.add(title)) titles.set("all", new ArrayList<>(all));
    }

    // ===== GUI =====
    public void openTitleGUI(Player player) {
        FileConfiguration cfg = plugin.getConfig();
        int rows = Math.max(1, Math.min(6, cfg.getInt("gui.rows", 3)));
        int size = rows * 9;
        String invTitle = Texts.color(cfg.getString("gui.title", "&8보유한 칭호 선택"));

        Inventory inv = Bukkit.createInventory(player, size, invTitle);

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
            // show with colors as-is
            meta.setDisplayName(Texts.color(t));
            // store raw title in PDC so we don't lose color codes when picking back
            meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, t);
            List<String> lore = new ArrayList<>();
            lore.add(Texts.color("&7좌클릭: 적용"));
            meta.setLore(lore);
            paper.setItemMeta(meta);
            inv.setItem(idx, paper);
            idx++;
        }
        player.openInventory(inv);
    }

    // ===== Custom Title Voucher =====
    public ItemStack createCustomTitleVoucher() {
        FileConfiguration cfg = plugin.getConfig();
        String name = cfg.getString("custom_title_voucher.name", "&b칭호권");
        List<String> lore = cfg.getStringList("custom_title_voucher.lore");
        String materialStr = cfg.getString("custom_title_voucher.material", "NAME_TAG");
        boolean glow = cfg.getBoolean("custom_title_voucher.glow", true);

        Material mat = Material.getMaterial(materialStr);
        if (mat == null) mat = Material.NAME_TAG;

        ItemStack item = new ItemStack(mat, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Texts.color(name));
        List<String> clore = new ArrayList<>();
        for (String l : lore) clore.add(Texts.color(l));
        meta.setLore(clore);
        if (glow) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        meta.getPersistentDataContainer().set(voucherKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        if (glow) item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        return item;
    }

    public boolean isVoucher(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        Byte b = item.getItemMeta().getPersistentDataContainer().get(voucherKey, PersistentDataType.BYTE);
        return b != null && b == (byte)1;
    }

    public void beginAwaiting(UUID uuid) {
        awaiting.add(uuid);
        int sec = plugin.getConfig().getInt("custom_title_voucher.input_timeout_seconds", 30);
        Integer old = awaitingTimeoutTask.get(uuid);
        if (old != null) Bukkit.getScheduler().cancelTask(old);
        int task = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (awaiting.remove(uuid)) {
                awaitingTimeoutTask.remove(uuid);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    String msg = plugin.getConfig().getString("custom_title_voucher.timeout_message","&c시간 초과로 취소되었습니다.");
                    p.sendMessage(Texts.color(msg));
                }
            }
        }, sec * 20L);
        awaitingTimeoutTask.put(uuid, task);
    }

    public boolean isAwaiting(UUID uuid) {
        return awaiting.contains(uuid);
    }

    public void endAwaiting(UUID uuid) {
        awaiting.remove(uuid);
        Integer old = awaitingTimeoutTask.remove(uuid);
        if (old != null) Bukkit.getScheduler().cancelTask(old);
    }

    public int removeTitleGlobal(String title) {
        java.util.List<String> all = titles.getStringList("all");
        boolean removed = all.removeIf(t -> t.equals(title));
        if (removed) titles.set("all", all);
        int affected = 0;
        for (String key : players.getKeys(false)) {
            java.util.List<String> owned = players.getStringList(key + ".owned");
            if (owned.removeIf(t -> t.equals(title))) {
                players.set(key + ".owned", owned);
                affected++;
            }
            String sel = players.getString(key + ".selected", null);
            if (sel != null && sel.equals(title)) {
                players.set(key + ".selected", null);
            }
        }
        return removed ? affected : 0;
    }
}