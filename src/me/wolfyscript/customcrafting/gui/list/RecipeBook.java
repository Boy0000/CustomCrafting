package me.wolfyscript.customcrafting.gui.list;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.PlayerCache;
import me.wolfyscript.customcrafting.data.cache.KnowledgeBook;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.Setting;
import me.wolfyscript.customcrafting.recipes.furnace.CustomFurnaceRecipe;
import me.wolfyscript.customcrafting.recipes.workbench.CraftingRecipe;
import me.wolfyscript.customcrafting.recipes.CustomRecipe;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.inventory.GuiAction;
import me.wolfyscript.utilities.api.inventory.GuiClick;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RecipeBook extends ExtendedGuiWindow {

    public RecipeBook(InventoryAPI inventoryAPI) {
        super("recipe_book", inventoryAPI, 45);
    }

    @Override
    public void onInit() {
        createItem("next_page", WolfyUtilities.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MTg1YjFkNTE5YWRlNTg1ZjE4NGMzNGYzZjNlMjBiYjY0MWRlYjg3OWU4MTM3OGU0ZWFmMjA5Mjg3In19fQ=="));
        createItem("previous_page", WolfyUtilities.getCustomHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWQ3M2NmNjZkMzFiODNjZDhiODY0NGMxNTk1OGMxYjczYzhkOTczMjNiODAxMTcwYzFkODg2NGJiNmE4NDZkIn19fQ=="));

        createItem("craft_recipe", Material.CRAFTING_TABLE);
        createItem("furnace_recipe", Material.FURNACE);
    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            Player player = event.getPlayer();
            PlayerCache cache = CustomCrafting.getPlayerCache(player);
            KnowledgeBook knowledgeBook = cache.getKnowledgeBook();
            for(int i = 36; i < 45; i++){
                event.setItem(i,"none", "glass_white");
            }
            if(!knowledgeBook.getSetting().equals(Setting.MAIN_MENU)){
                for(int i = 1; i < 9; i++){
                    event.setItem(i,"none", "glass_white");
                }
                if(knowledgeBook.getRecipeID().isEmpty()){
                    event.setItem(38, "previous_page");
                    event.setItem(42, "next_page");
                    List<CustomRecipe> recipes = new ArrayList<>();
                    switch (knowledgeBook.getSetting()){
                        case CRAFT_RECIPE:
                            for(CraftingRecipe recipe : CustomCrafting.getRecipeHandler().getCraftingRecipes()){
                                if(!recipe.needsPermission() || (player.hasPermission("customcrafting.craft.*") || player.hasPermission("customcrafting.craft." + recipe.getId()) || player.hasPermission("customcrafting.craft." + recipe.getId().split(":")[0]))){
                                    recipes.add(recipe);
                                }
                            }
                            break;
                        case FURNACE_RECIPE:
                            recipes.addAll(CustomCrafting.getRecipeHandler().getFurnaceRecipes());
                    }
                    if((45 * knowledgeBook.getPage()) >= recipes.size()){
                        knowledgeBook.setPage(0);
                    }
                    int item = 0;
                    for (int i = 36 * knowledgeBook.getPage(); item < 36 && i < recipes.size(); i++) {
                        Recipe recipe = recipes.get(i);
                        if (recipe instanceof Keyed) {
                            ItemStack itemStack = recipe.getResult();
                            if (itemStack.getType().equals(Material.AIR)) {
                                itemStack = new ItemStack(Material.STONE);
                                itemStack.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.setDisplayName("§r§7" + ((Keyed) recipe).getKey().toString());
                                itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                itemStack.setItemMeta(itemMeta);
                            }
                            ItemMeta itemMeta = itemStack.getItemMeta();
                            List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
                            lore.add(ChatColor.translateAlternateColorCodes('&', api.getLanguageAPI().getActiveLanguage().replaceKeys("$items.recipe_book.lores.click$"))+WolfyUtilities.hideString(";;"+((Keyed) recipe).getKey().toString()));
                            itemMeta.setLore(lore);
                            itemStack.setItemMeta(itemMeta);
                            event.setItem(9 + item, itemStack);
                        }
                        item++;
                    }
                }else{
                    switch (knowledgeBook.getSetting()){
                        case CRAFT_RECIPE:
                            if (!knowledgeBook.getIngredients().isEmpty()) {
                                int slot;
                                for (int i = 0; i < 9; i++) {
                                    slot = 10 + i + (i / 3) * 6;
                                    event.setItem(slot, knowledgeBook.getIngredients().isEmpty() ? new ItemStack(Material.AIR) : knowledgeBook.getIngredient(i) != null ? knowledgeBook.getIngredient(i) : new ItemStack(Material.AIR));
                                }
                                event.setItem(24, knowledgeBook.getResult());
                            }
                            break;
                        case FURNACE_RECIPE:
                            if(knowledgeBook.getSource() != null){
                                event.setItem(20, knowledgeBook.getSource());
                                event.setItem(24, knowledgeBook.getResult());
                            }
                    }
                }
            }else{
                //MAINMENU
                event.setItem(0, "none", "glass_gray");
                event.setItem(11, "craft_recipe");
                event.setItem(13, "furnace_recipe");
            }
        }
    }

    @Override
    public boolean onClick(GuiClick guiClick) {
        Player player = guiClick.getPlayer();
        PlayerCache cache = CustomCrafting.getPlayerCache(player);
        KnowledgeBook book = cache.getKnowledgeBook();
        ItemStack itemStack = guiClick.getCurrentItem();
        if(itemStack != null && itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore()){
            List<String> lore = itemStack.getItemMeta().getLore();
            String line = lore.get(lore.size()-1);
            if(line.startsWith(WolfyUtilities.translateColorCodes(api.getLanguageAPI().getActiveLanguage().replaceKeys("$items.recipe_book.lores.click$")))){
                String code = WolfyUtilities.unhideString(line);
                String id = code.split(";;")[1];
                if(code.contains(";;") && id.contains(":")){
                    book.setRecipeID(id);
                    if(!book.getRecipeID().isEmpty()){
                        CustomRecipe recipe = CustomCrafting.getRecipeHandler().getRecipe(book.getRecipeID());
                        if(recipe != null){
                            switch (book.getSetting()){
                                case CRAFT_RECIPE:
                                    book.setIngredients(((CraftingRecipe) recipe).getIngredients());
                                    book.setResult(recipe.getCustomResult());
                                    break;
                                case FURNACE_RECIPE:
                                    book.setSource(((CustomFurnaceRecipe) recipe).getSource());
                                    book.setResult(recipe.getCustomResult());
                                    break;
                            }
                        }
                    }

                }
            }
        }
        update(guiClick.getGuiHandler());
        return true;

    }

    @Override
    public boolean onAction(GuiAction guiAction) {
        PlayerCache cache = CustomCrafting.getPlayerCache(guiAction.getPlayer());
        KnowledgeBook book = cache.getKnowledgeBook();
        switch (guiAction.getAction()){
            case "back":
                if(book.getRecipeID().isEmpty()){
                    book.setSetting(Setting.MAIN_MENU);
                }else{
                    book.setRecipeID("");
                }
                break;
            case "craft_recipe":
                book.setSetting(Setting.CRAFT_RECIPE);
                break;
            case "furnace_recipe":
                book.setSetting(Setting.FURNACE_RECIPE);
                break;

        }
        return true;
    }

}
