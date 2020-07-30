package me.wolfyscript.customcrafting.gui.main_gui;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.PlayerStatistics;
import me.wolfyscript.customcrafting.data.TestCache;
import me.wolfyscript.customcrafting.gui.ExtendedGuiWindow;
import me.wolfyscript.customcrafting.gui.Setting;
import me.wolfyscript.customcrafting.gui.main_gui.buttons.RecipeTypeButton;
import me.wolfyscript.utilities.api.inventory.GuiUpdateEvent;
import me.wolfyscript.utilities.api.inventory.InventoryAPI;
import me.wolfyscript.utilities.api.inventory.button.ButtonState;
import me.wolfyscript.utilities.api.inventory.button.buttons.ActionButton;
import me.wolfyscript.utilities.api.utils.inventory.PlayerHeadUtils;
import me.wolfyscript.utilities.api.utils.inventory.item_builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemFlag;

public class MainMenu extends ExtendedGuiWindow {

    public MainMenu(InventoryAPI inventoryAPI, CustomCrafting customCrafting) {
        super("main_menu", inventoryAPI, 45, customCrafting);
    }

    @Override
    public void onInit() {
        registerButton(new RecipeTypeButton("workbench", Setting.WORKBENCH, Material.CRAFTING_TABLE));
        registerButton(new RecipeTypeButton("furnace", Setting.FURNACE, Material.FURNACE));
        registerButton(new RecipeTypeButton("anvil", Setting.ANVIL, Material.ANVIL));
        registerButton(new RecipeTypeButton("blast_furnace", Setting.BLAST_FURNACE, Material.BLAST_FURNACE));
        registerButton(new RecipeTypeButton("smoker", Setting.SMOKER, Material.SMOKER));
        registerButton(new RecipeTypeButton("campfire", Setting.CAMPFIRE, Material.CAMPFIRE));
        registerButton(new RecipeTypeButton("stonecutter", Setting.STONECUTTER, Material.STONECUTTER));
        registerButton(new RecipeTypeButton("grindstone", Setting.GRINDSTONE, Material.GRINDSTONE));
        registerButton(new RecipeTypeButton("brewing", Setting.BREWING_STAND, Material.BREWING_STAND));
        registerButton(new RecipeTypeButton("elite_workbench", Setting.ELITE_WORKBENCH, new ItemBuilder(Material.CRAFTING_TABLE).addItemFlags(ItemFlag.HIDE_ENCHANTS).addUnsafeEnchantment(Enchantment.DURABILITY, 0).create()));
        registerButton(new RecipeTypeButton("cauldron", Setting.CAULDRON, Material.CAULDRON));
        registerButton(new ActionButton("item_editor", new ButtonState("item_editor", Material.CHEST, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            TestCache cache = (TestCache) guiHandler.getCustomCache();
            cache.setSetting(Setting.ITEMS);
            cache.getItems().setRecipeItem(false);
            cache.getItems().setSaved(false);
            cache.getItems().setNamespacedKey(null);
            guiHandler.changeToInv("item_editor");
            return true;
        })));
        registerButton(new ActionButton("recipe_list", new ButtonState("recipe_list", Material.WRITTEN_BOOK, (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.changeToInv("recipe_list");
            ((TestCache) guiHandler.getCustomCache()).setSetting(Setting.RECIPE_LIST);
            return true;
        })));
        registerButton(new ActionButton("settings", new ButtonState("settings", PlayerHeadUtils.getViaURL("b3f293ebd0911bb8133e75802890997e82854915df5d88f115de1deba628164"), (guiHandler, player, inventory, i, inventoryClickEvent) -> {
            guiHandler.changeToInv("settings");
            return true;
        })));
    }

    @EventHandler
    public void onUpdate(GuiUpdateEvent event) {
        if (event.verify(this)) {
            event.setButton(0, "settings");
            event.setButton(8, "none", "gui_help");

            event.setButton(4, "none", "patreon");
            event.setButton(39, "none", "instagram");
            event.setButton(40, "none", "youtube");
            event.setButton(41, "none", "discord");

            event.setButton(10, "workbench");
            event.setButton(12, "furnace");
            event.setButton(14, "anvil");
            event.setButton(16, "cauldron");
            event.setButton(20, "blast_furnace");
            event.setButton(22, "smoker");
            event.setButton(24, "campfire");
            event.setButton(28, "stonecutter");
            event.setButton(30, "grindstone");
            event.setButton(32, "brewing");
            event.setButton(34, "elite_workbench");
            event.setButton(36, "item_editor");
            event.setButton(44, "recipe_list");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onUpdateGuis(GuiUpdateEvent event) {
        if (event.getWolfyUtilities().equals(CustomCrafting.getApi()) && event.getGuiHandler().getCurrentInv() != null && event.getGuiHandler().getCurrentInv().equals(event.getGuiWindow())) {
            PlayerStatistics playerStatistics = CustomCrafting.getPlayerStatistics(event.getPlayer());
            if (!event.getGuiWindow().getNamespace().startsWith("crafting_grid")) {
                if (event.getGuiHandler().getCurrentInv().getSize() > 9) {
                    for (int i = 0; i < 9; i++) {
                        event.setButton(i, "none", playerStatistics.getDarkMode() ? "glass_gray" : "glass_white");
                    }
                    for (int i = 9; i < event.getGuiHandler().getCurrentInv().getSize() - 9; i++) {
                        event.setButton(i, "none", playerStatistics.getDarkMode() ? "glass_black" : "glass_gray");
                    }
                    for (int i = event.getGuiHandler().getCurrentInv().getSize() - 9; i < event.getGuiHandler().getCurrentInv().getSize(); i++) {
                        event.setButton(i, "none", playerStatistics.getDarkMode() ? "glass_gray" : "glass_white");
                    }
                    event.setButton(8, "none", "gui_help");
                } else {
                    for (int i = 0; i < 9; i++) {
                        event.setButton(i, "none", playerStatistics.getDarkMode() ? "glass_black" : "glass_gray");
                    }
                }
            }
        }
    }
}
