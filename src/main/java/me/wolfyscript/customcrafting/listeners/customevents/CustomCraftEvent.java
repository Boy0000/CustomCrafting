package me.wolfyscript.customcrafting.listeners.customevents;

import me.wolfyscript.utilities.api.custom_items.CustomItem;
import me.wolfyscript.customcrafting.recipes.types.CraftingRecipe;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;

import java.util.List;

public class CustomCraftEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private List<CustomItem> result;
    private Inventory inventory;
    private boolean cancelled;
    private CraftingRecipe craftingRecipe;

    public CustomCraftEvent(CraftingRecipe craftingRecipe, Inventory inventory) {
        this.craftingRecipe = craftingRecipe;
        this.inventory = inventory;
        this.result = craftingRecipe.getCustomResults();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public List<CustomItem> getResult() {
        return result;
    }

    public void setResult(List<CustomItem> result) {
        this.result = result;
    }

    public CraftingRecipe getRecipe() {
        return craftingRecipe;
    }

    @Override
    public String getEventName() {
        return super.getEventName();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
