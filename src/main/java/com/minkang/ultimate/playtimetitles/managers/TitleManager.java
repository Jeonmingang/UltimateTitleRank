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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TitleManager implements Listener {
    private final Main plugin;
    private final LuckPermsBridge lp;
    private final ColorChatManager colorChat;
    private File filePlayers;
    private FileConfiguration dataPlayers;
    private File fileGlobal;
    private FileConfiguration dataGlobal;

    private final Map<UUID, Boolean> awaitingInput = new HashMap<>();

    public TitleManager(Main plugin, LuckPermsBridge lp, ColorChatManager colorChat) {
        this.plugin = plugin;
        this.lp = lp;
        this.colorChat = colorChat;
        filePlayers = new File(plugin.getDataFolder(), "players.yml"); // share with playtime but separate sections
        if (!filePlayers.exists()) try { filePlayers.getParentFile().mkdirs(); filePlayers.createNewFile(); } catch (IOException ignored) {}
        dataPlayers = YamlConfiguration.loadConfiguration(filePlayers);

        fileGlobal = new File(plugin.getDataFolder(), "titles.yml");
        if (!fileGlobal.exists()) try { fileGlobal.getParentFile().mkdirs(); fileGlobal.createNewFile(); } catch (IOException ignored) {}
        dataGlobal = YamlConfiguration.loadConfiguration(fileGlobal);
    }

    public void openGui(Player p) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.titles_title","&a칭호 GUI"));
        Inventory inv = Bukkit.createInventory(null, 54, title);
        List<String> owned = dataPlayers.getStringList(p.getUniqueId().toString()+".titles");
        int slot = 0;
        for (String t : owned) {
            ItemStack nameTag = new ItemStack(Material.NAME_TAG, 1);
            ItemMeta meta = nameTag.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', t));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "클릭: 이 칭호를 접미사로 적용");
            meta.setLore(lore);
            nameTag.setItemMeta(meta);
            inv.setItem(slot++, nameTag);
            if (slot >= 53) break;
        }
        // Clear button
        ItemStack clear = new ItemStack(Material.BARRIER, 1);
        ItemMeta cm = clear.getItemMeta();
        cm.setDisplayName(ChatColor.RED + "접미사 비우기");
        clear.setItemMeta(cm);
        inv.setItem(53, clear);
        p.openInventory(inv);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem()==null || !e.getItem().hasItemMeta()) return;
        ItemStack it = e.getItem();
        ItemMeta im = it.getItemMeta();
        String confName = plugin.getConfig().getString("custom_ticket.name","&d칭호 커스텀권");
        List<String> confLore = plugin.getConfig().getStringList("custom_ticket.lore");
        String name = ChatColor.translateAlternateColorCodes('&', confName);
        boolean nameMatch = im.hasDisplayName() && name.equals(im.getDisplayName());
        boolean loreMatch = true;
        if (confLore != null && confLore.size() > 0) {
            if (!im.hasLore()) loreMatch = false;
            else {
                List<String> lo = im.getLore();
                if (lo == null) loreMatch = false;
                else {
                    if (lo.size() != confLore.size()) loreMatch = false;
                    else {
                        for (int i=0;i<confLore.size();i++) {
                            String a = ChatColor.translateAlternateColorCodes('&', confLore.get(i));
                            String b = ChatColor.translateAlternateColorCodes('&', lo.get(i));
                            if (!a.equals(b)) { loreMatch = false; break; }
                        }
                    }
                }
            }
        }
        if (nameMatch && loreMatch) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            if (awaitingInput.containsKey(p.getUniqueId()) && awaitingInput.get(p.getUniqueId())) {
                p.sendMessage("§c이미 제목 입력 대기중입니다.");
                return;
            }
            // consume one
            it.setAmount(it.getAmount()-1);
            if (it.getAmount() <= 0) e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            awaitingInput.put(p.getUniqueId(), true);
            p.sendMessage("§a채팅에 원하는 칭호를 입력하세요. & 색코드 사용 가능 (허용된 색만 적용). 30초 제한.");
            // timeout
            Bukkit.getScheduler().runTaskLater(plugin, ()->{
                Boolean b = awaitingInput.get(p.getUniqueId());
                if (b!=null && b) {
                    awaitingInput.put(p.getUniqueId(), false);
                    p.sendMessage("§7시간 초과로 취소되었습니다.");
                }
            }, 20L*30);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        Boolean wait = awaitingInput.get(p.getUniqueId());
        if (wait==null || !wait) return;
        e.setCancelled(true);
        awaitingInput.put(p.getUniqueId(), false);
        String raw = e.getMessage();
        // translate allowed colors
        String colored = colorChat.translateAllowed(p, raw);
        List<String> owned = dataPlayers.getStringList(p.getUniqueId().toString()+".titles");
        if (!owned.contains(colored)) {
            owned.add(colored);
            dataPlayers.set(p.getUniqueId().toString()+".titles", owned);
            savePlayers();
        }
        p.sendMessage("§a새 칭호 등록: " + colored);
        // auto-apply as suffix
        if (lp.isAvailable()) lp.setSuffix(p, colored);
    }

    public void setCustomTicketFromItem(Player op, ItemStack inHand) {
        if (inHand==null || !inHand.hasItemMeta()) {
            op.sendMessage("§c손에 아이템을 들고 실행하세요.");
            return;
        }
        Map<String,Object> node = new LinkedHashMap<>();
        node.put("name", inHand.hasItemMeta() && inHand.getItemMeta().hasDisplayName() ? inHand.getItemMeta().getDisplayName() : "&d칭호 커스텀권");
        List<String> lore = inHand.getItemMeta().hasLore() ? inHand.getItemMeta().getLore() : Arrays.asList("&7우클릭하여 원하는 칭호를 등록합니다.");
        node.put("lore", lore);
        plugin.getConfig().set("custom_ticket", node);
        plugin.saveConfig();
        op.sendMessage("§a현재 손에 든 아이템을 칭호 커스텀권 템플릿으로 저장했습니다.");
    }

    public void createGlobalTitle(String name, int number) {
        dataGlobal.set("slots."+number+".name", name);
        saveGlobal();
    }

    public void deleteGlobalTitle(int number) {
        dataGlobal.set("slots."+number, null);
        saveGlobal();
    }

    public List<String> listGlobalTitles() {
        List<String> out = new ArrayList<>();
        if (dataGlobal.getConfigurationSection("slots")==null) return out;
        for (String k : dataGlobal.getConfigurationSection("slots").getKeys(false)) {
            String nm = dataGlobal.getString("slots."+k+".name");
            out.add("#"+k+" - " + nm);
        }
        return out;
    }

    public void openGui(Player p, boolean includeGlobal) {
        openGui(p); // simple owned-only for now
    }

    public void applyTitle(Player p, String titleColored) {
        if (lp.isAvailable()) lp.setSuffix(p, titleColored);
        p.sendMessage("§a접미사 적용: " + titleColored);
    }

    public void saveAll() { savePlayers(); saveGlobal(); }

    private void savePlayers() {
        try { dataPlayers.save(filePlayers); }
        catch (IOException e) { plugin.getLogger().warning("players.yml 저장 실패: " + e.getMessage()); }
    }
    private void saveGlobal() {
        try { dataGlobal.save(fileGlobal); }
        catch (IOException e) { plugin.getLogger().warning("titles.yml 저장 실패: " + e.getMessage()); }
    }
}
