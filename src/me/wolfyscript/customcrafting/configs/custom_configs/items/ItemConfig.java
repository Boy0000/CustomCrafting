package me.wolfyscript.customcrafting.configs.custom_configs.items;

import me.wolfyscript.customcrafting.configs.custom_configs.CustomConfig;
import me.wolfyscript.customcrafting.items.CustomItem;
import me.wolfyscript.utilities.api.config.ConfigAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ItemConfig extends CustomConfig {

    public ItemConfig(ConfigAPI configAPI, String defaultpath, String defaultName, String folder, String name, boolean override) {
        super(configAPI, defaultpath, defaultName, folder, "items", name, override);
    }

    public ItemConfig(ConfigAPI configAPI, String defaultName, String folder, String name, boolean override) {
        super(configAPI, defaultName, folder, "items", name, override);
    }

    public ItemConfig(ConfigAPI configAPI, String defaultName, String folder, String name) {
        this(configAPI, defaultName, folder, name, false);
    }

    public ItemConfig(ConfigAPI configAPI, String folder, String name) {
        this(configAPI, "item", folder, name);
    }

    public ItemStack getCustomItem(){
        return getItem("item");
    }

    public void setCustomItem(CustomItem itemStack){
        saveItem("item", itemStack);
        setBurnTime(itemStack.getBurnTime());
        if(itemStack.getAllowedBlocks().isEmpty()){
            setAllowedBlocks(new ArrayList<>(Collections.singleton(Material.FURNACE)));
        }else{
            setAllowedBlocks(itemStack.getAllowedBlocks());
        }
    }

    public void setItem(ItemStack itemStack){
        saveItem("item", itemStack);
    }

    public void setAllowedBlocks(ArrayList<Material> furnaces){
        List<String> mats = new ArrayList<>();
        furnaces.forEach(material -> mats.add(material.name().toLowerCase(Locale.ROOT)));
        set("fuel.allowed_blocks", mats);
    }

    public ArrayList<Material> getAllowedBlocks(){
        ArrayList<Material> furnaces = new ArrayList<>();
        if(getStringList("fuel.allowed_blocks") != null){
            getStringList("fuel.allowed_blocks").forEach(s -> {
                Material material = Material.matchMaterial(s);
                if(material != null){
                    furnaces.add(material);
                }
            });
        }
        return furnaces;
    }

    public void setBurnTime(int burntime){
        set("fuel.burntime", burntime);
    }

    public int getBurnTime(){
        return getInt("fuel.burntime");
    }
}
