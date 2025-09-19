package com.minkang.ultimate.titles;
import org.bukkit.event.*; import org.bukkit.event.player.*;
public class ColorChatFormatterListener implements Listener { private final Main plugin; private final ColorChatService service;
public ColorChatFormatterListener(Main plugin, ColorChatService s){ this.plugin=plugin; this.service=s; }
@EventHandler public void onChat(AsyncPlayerChatEvent e){ PlayerData d=plugin.storage().get(e.getPlayer().getUniqueId()); String id=d.getActiveColorId(); if(id==null) return; ColorDef def=service.get(id); if(def==null) return; e.setMessage(Chat.color(def.code)+e.getMessage()+Chat.color("&r")); }
}