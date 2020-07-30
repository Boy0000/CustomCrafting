package me.wolfyscript.customcrafting.listeners;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.recipes.types.ICustomRecipe;
import me.wolfyscript.customcrafting.recipes.types.anvil.AnvilData;
import me.wolfyscript.customcrafting.recipes.types.anvil.CustomAnvilRecipe;
import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.utilities.api.utils.inventory.item_builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.*;

public class AnvilListener implements Listener {

    private static final HashMap<UUID, AnvilData> preCraftedRecipes = new HashMap<>();

    private final CustomCrafting customCrafting;

    public AnvilListener(CustomCrafting customCrafting) {
        this.customCrafting = customCrafting;
    }

    @EventHandler
    public void onCheck(PrepareAnvilEvent event) {
        Player player = (Player) event.getView().getPlayer();
        AnvilInventory inventory = event.getInventory();
        List<CustomAnvilRecipe> recipes = customCrafting.getRecipeHandler().getAvailableAnvilRecipes(player);
        recipes.sort(Comparator.comparing(ICustomRecipe::getPriority));
        preCraftedRecipes.remove(player.getUniqueId());
        for (CustomAnvilRecipe recipe : recipes) {
            CustomItem finalInputLeft = null;
            CustomItem finalInputRight = null;

            if (recipe.hasInputLeft()) {
                if (inventory.getItem(0) != null) {
                    for (CustomItem customItem : recipe.getInputLeft()) {
                        if (customItem.isSimilar(inventory.getItem(0), recipe.isExactMeta())) {
                            finalInputLeft = customItem.clone();
                            break;
                        }
                    }
                    if (finalInputLeft == null) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            if (recipe.hasInputRight()) {
                if (inventory.getItem(1) != null) {
                    for (CustomItem customItem : recipe.getInputRight()) {
                        if (customItem.isSimilar(inventory.getItem(1), recipe.isExactMeta())) {
                            finalInputRight = customItem.clone();
                            break;
                        }
                    }
                    if (finalInputRight == null) {
                        continue;
                    }
                } else {
                    continue;
                }
            }
            ItemStack inputLeft = inventory.getItem(0);
            ItemBuilder result;

            //RECIPE RESULTS!
            if (recipe.getMode().equals(CustomAnvilRecipe.Mode.RESULT)) {
                result = new ItemBuilder(recipe.getCustomResult().create());
            } else {
                result = new ItemBuilder(event.getResult());
                if (result != null && result.create().hasItemMeta()) {
                    if (recipe.isBlockEnchant()) {
                        if (result.create().hasItemMeta() && result.getItemMeta().hasEnchants()) {
                            for (Enchantment enchantment : result.create().getEnchantments().keySet()) {
                                result.create().removeEnchantment(enchantment);
                            }
                            for(Map.Entry<Enchantment, Integer> entry : inputLeft.getEnchantments().entrySet()){
                                result.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                    if (recipe.isBlockRename()) {
                        ItemMeta itemMeta = result.getItemMeta();
                        if (inputLeft.hasItemMeta() && inputLeft.getItemMeta().hasDisplayName()) {
                            itemMeta.setDisplayName(inputLeft.getItemMeta().getDisplayName());
                        } else {
                            itemMeta.setDisplayName(null);
                        }
                        result.setItemMeta(itemMeta);
                    }
                    if (recipe.isBlockRepair()) {
                        ItemMeta itemMeta = result.getItemMeta();
                        if (itemMeta instanceof Damageable) {
                            if (inputLeft.hasItemMeta() && inputLeft.getItemMeta() instanceof Damageable) {
                                ((Damageable) itemMeta).setDamage(((Damageable) inputLeft.getItemMeta()).getDamage());
                            }
                            result.setItemMeta(itemMeta);
                        }
                    }
                }
                if (result == null || result.create().getType().equals(Material.AIR)) {
                    result = new ItemBuilder(inputLeft.clone());
                }
                if (recipe.getMode().equals(CustomAnvilRecipe.Mode.DURABILITY)) {
                    if (result.hasCustomDurability()) {
                        int damage = result.getCustomDamage() - recipe.getDurability();
                        if (damage < 0) {
                            damage = 0;
                        }
                        result.setCustomDamage(damage);
                    } else if (result.getItemMeta() instanceof Damageable) {
                        ItemMeta itemMeta = result.getItemMeta();
                        ((Damageable) itemMeta).setDamage(((Damageable) itemMeta).getDamage() - recipe.getDurability());
                        result.setItemMeta(itemMeta);
                    }
                }
            }
            int repairCost = recipe.getRepairCost();

            ItemMeta inputMeta = inputLeft.getItemMeta();
            if (inputMeta instanceof Repairable) {
                int itemRepairCost = ((Repairable) inputMeta).getRepairCost();
                if (recipe.getRepairCostMode().equals(CustomAnvilRecipe.RepairCostMode.ADD)) {
                    repairCost = repairCost + itemRepairCost;
                } else if (recipe.getRepairCostMode().equals(CustomAnvilRecipe.RepairCostMode.MULTIPLY)) {
                    repairCost = recipe.getRepairCost() * (itemRepairCost > 0 ? itemRepairCost : 1);
                }
            }
            if (recipe.isApplyRepairCost()) {
                ItemMeta itemMeta = result.getItemMeta();
                if (itemMeta instanceof Repairable) {
                    ((Repairable) itemMeta).setRepairCost(repairCost);
                    result.setItemMeta(itemMeta);
                }
            }

            //Save current active recipe to consume correct item inputs!
            AnvilData anvilData = new AnvilData(recipe, finalInputLeft, finalInputRight);
            preCraftedRecipes.put(player.getUniqueId(), anvilData);

            /*
                 Set the values and result 1 tick after they are replaced by NMS.
                 So the player will get the correct Item and the correct values are displayed!
            */
            final int finalRepairCost = repairCost;
            ItemStack finalResult = result.create();

            inventory.setRepairCost(finalRepairCost);
            event.setResult(finalResult);
            Bukkit.getScheduler().runTask(customCrafting, () -> {
                inventory.setRepairCost(finalRepairCost);
                event.setResult(finalResult);
                player.updateInventory();
            });
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof AnvilInventory) {
            AnvilInventory inventory = (AnvilInventory) event.getClickedInventory();
            Player player = (Player) event.getWhoClicked();
            //TODO: Input consume method
            if (event.getSlot() == 2) {
                if (preCraftedRecipes.get(player.getUniqueId()) != null) {
                    //Custom Recipe
                    AnvilData grindstoneData = preCraftedRecipes.get(player.getUniqueId());
                    CustomItem inputLeft = grindstoneData.getInputLeft();
                    CustomItem inputRight = grindstoneData.getInputRight();

                    final ItemStack itemLeft = inventory.getItem(0) == null ? null : inventory.getItem(0).clone();
                    final ItemStack itemRight = inventory.getItem(1) == null ? null : inventory.getItem(1).clone();

                    Bukkit.getScheduler().runTaskLater(customCrafting, () -> {
                        if (inputLeft != null) {
                            inputLeft.consumeItem(itemLeft, 1, inventory);
                            inventory.setItem(0, itemLeft);
                        }
                        if (inputRight != null) {
                            inputRight.consumeItem(itemRight, 1, inventory);
                            inventory.setItem(1, itemRight);
                        }
                        preCraftedRecipes.remove(player.getUniqueId());
                    }, 1);
                    return;
                } else {
                    //Vanilla Recipe
                }
            }
        }
    }
}
