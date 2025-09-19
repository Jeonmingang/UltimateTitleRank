package com.minkang.ultimate.titles;
import org.bukkit.Material; import java.util.*;
public class ColorChatService { private final Main plugin; private final java.util.Map<String,ColorDef> colors=new java.util.HashMap<>();
public ColorChatService(Main plugin){ this.plugin=plugin; load(); }
public void load(){ colors.clear(); for(Object o:plugin.getConfig().getMapList("colorchat.colors")){ @SuppressWarnings("unchecked") java.util.Map<String,Object> m=(java.util.Map<String,Object>)o; String id=String.valueOf(m.get("id")); String name=String.valueOf(m.get("name")); String code=String.valueOf(m.get("code")); String mat=String.valueOf(m.get("material")); Material material=Material.matchMaterial(mat); if(material==null) material=Material.PAPER; colors.put(id.toLowerCase(), new ColorDef(id,name,code,material)); } }
public java.util.Collection<ColorDef> all(){ return colors.values(); }
public ColorDef get(String id){ return id==null?null:colors.get(id.toLowerCase()); }
public void grant(java.util.UUID uuid,String colorId){ PlayerData d=plugin.storage().get(uuid); d.getColorsOwned().add(colorId.toLowerCase()); plugin.storage().save(uuid,d); }
public void revoke(java.util.UUID uuid,String colorId){ PlayerData d=plugin.storage().get(uuid); d.getColorsOwned().remove(colorId.toLowerCase()); if(colorId.equalsIgnoreCase(d.getActiveColorId())) d.setActiveColorId(null); plugin.storage().save(uuid,d); }
public void setActive(java.util.UUID uuid,String colorId){ PlayerData d=plugin.storage().get(uuid); d.setActiveColorId(colorId==null?null:colorId.toLowerCase()); plugin.storage().save(uuid,d); }
}