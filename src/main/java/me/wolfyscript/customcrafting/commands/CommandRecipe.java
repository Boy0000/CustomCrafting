package me.wolfyscript.customcrafting.commands;

import me.wolfyscript.customcrafting.CustomCrafting;
import me.wolfyscript.customcrafting.data.TestCache;
import me.wolfyscript.customcrafting.gui.Setting;
import me.wolfyscript.customcrafting.recipes.types.ICustomRecipe;
import me.wolfyscript.customcrafting.utils.ChatUtils;
import me.wolfyscript.utilities.api.WolfyUtilities;
import me.wolfyscript.utilities.api.custom_items.CustomItems;
import me.wolfyscript.utilities.api.utils.chat.ClickData;
import me.wolfyscript.utilities.api.utils.chat.ClickEvent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRecipe implements CommandExecutor, TabCompleter {

    private final List<String> COMMANDS = Arrays.asList("toggle", "edit", "delete", "save");
    private final List<String> RECIPES = Arrays.asList("workbench", "furnace", "anvil", "blast_furnace", "smoker", "campfire", "stonecutter");
    private final CustomCrafting customCrafting;

    public CommandRecipe(CustomCrafting customCrafting) {
        this.customCrafting = customCrafting;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        WolfyUtilities api = CustomCrafting.getApi();
        if (args.length > 0) {
            String label = args[0];
            if (label.equalsIgnoreCase("toggle") && ChatUtils.checkPerm(sender, "customcrafting.cmd.recipes.toggle")) {
                if (args.length > 1) {
                    String id = args[1];
                    if (!id.isEmpty() && id.contains(":")) {
                        if (customCrafting.getRecipeHandler().getDisabledRecipes().contains(id)) {
                            sender.sendMessage("Enabled recipe " + id);
                            customCrafting.getRecipeHandler().getDisabledRecipes().remove(id);
                        } else {
                            sender.sendMessage("Disabled recipe " + id);
                            customCrafting.getRecipeHandler().getDisabledRecipes().add(id);
                            Bukkit.getOnlinePlayers().forEach(player -> player.undiscoverRecipe(new NamespacedKey(id.split(":")[0], id.split(":")[1])));
                        }
                    }
                }
            } else if ((label.equalsIgnoreCase("edit") && ChatUtils.checkPerm(sender, "customcrafting.cmd.recipes.edit")) || (label.equalsIgnoreCase("delete") && ChatUtils.checkPerm(sender, "customcrafting.cmd.recipes.delete"))) {
                if (args.length > 2) {
                    Player player = (Player) sender;
                    ICustomRecipe customRecipe = customCrafting.getRecipeHandler().getRecipe(new me.wolfyscript.utilities.api.utils.NamespacedKey(args[2].split(":")[0], args[2].split(":")[1]));
                    if (customRecipe != null) {
                        if (label.equalsIgnoreCase("edit")) {
                            Setting setting = Setting.WORKBENCH;
                            switch (args[1]) {
                                case "furnace":
                                    setting = Setting.FURNACE;
                                    break;
                                case "anvil":
                                case "blast_furnace":
                                case "smoker":
                                case "campfire":
                                case "stonecutter":
                                    setting = Setting.valueOf(args[1].toUpperCase(Locale.ROOT));
                                    break;
                            }
                            ((TestCache) api.getInventoryAPI().getGuiHandler(player).getCustomCache()).setSetting(setting);
                            if (customCrafting.getRecipeHandler().loadRecipeIntoCache(customRecipe, api.getInventoryAPI().getGuiHandler(player))) {
                                Bukkit.getScheduler().runTaskLater(customCrafting, () -> api.getInventoryAPI().openGui(player, "none", "recipe_creator"), 1);
                            }
                        } else if (label.equalsIgnoreCase("delete")) {
                            api.sendPlayerMessage(player, "$msg.gui.recipe_editor.delete.confirm$", new String[]{"%RECIPE%", customRecipe.getNamespacedKey().toString()});
                            StringBuilder command = new StringBuilder("/recipes");
                            for (int i = 0; i < args.length - 1; i++) {
                                command.append(" ").append(args[i]);
                            }
                            command.append(" ");
                            api.sendActionMessage(player, new ClickData("$msg.gui.recipe_editor.delete.confirmed$", (wolfyUtilities, player1) -> Bukkit.getScheduler().runTask(customCrafting, () -> {
                                customCrafting.getRecipeHandler().unregisterRecipe(customRecipe);
                                //TODO
                                /*
                                if (customRecipe.getConfig().getConfigFile().delete()) {
                                    api.sendPlayerMessage(player1, "§aRecipe deleted!");
                                } else {
                                    api.sendPlayerMessage(player1, "§cCould not delete recipe!");
                                }

                                 */
                            })), new ClickData("$msg.gui.recipe_editor.delete.declined$", (wolfyUtilities, player1) -> api.sendPlayerMessage(player1, "§cCancelled"), new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.SUGGEST_COMMAND, command.toString())));
                        }
                    } else {
                        api.sendPlayerMessage((Player) sender, "$msg.gui.recipe_editor.not_existing$", new String[]{"%RECIPE%", args[0] + ":" + args[1]});
                    }
                }
            } else if(label.equalsIgnoreCase("save") && ChatUtils.checkPerm(sender, "customcrafting.cmd.recipes.save")){
                CustomItems.getCustomItems().forEach((namespacedKey, customItem) -> {
                    api.sendConsoleMessage("Saving item: "+namespacedKey.toString());
                    customCrafting.saveItem(namespacedKey, customItem);
                });
                customCrafting.getRecipeHandler().getRecipes().values().forEach(recipe -> {
                    api.sendConsoleMessage("Saving recipe: "+recipe.getNamespacedKey().toString());
                    recipe.save();
                });
                sender.sendMessage("§eAll recipes are resaved! See the console log for errors.");
                sender.sendMessage("§cNotice that some recipes must be recreated due incompatibility! These are: ");
                sender.sendMessage("§c- recipes that caused errors when saving (their config is corrupted from now on)");
                sender.sendMessage("§c- recipes that don't work when the server is restarted");
                sender.sendMessage("§eYou can also report the errors for further information!");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        List<String> results = new ArrayList<>();
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("toggle")) {
                List<String> recipes = customCrafting.getRecipeHandler().getVanillaRecipes().stream().filter(recipe -> recipe instanceof Keyed).map(recipe -> ((Keyed) recipe).getKey().toString()).collect(Collectors.toList());
                recipes.addAll(customCrafting.getRecipeHandler().getAdvancedCraftingRecipes().stream().map(recipe -> recipe.getNamespacedKey().toString()).collect(Collectors.toSet()));
                copyPartialMatches(args[args.length - 1], recipes, results);
            } else if (args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("delete")) {
                if (args.length == 2) {
                    StringUtil.copyPartialMatches(args[1], RECIPES, results);
                } else if (args.length == 3) {
                    List<String> recipes = customCrafting.getRecipeHandler().getRecipes(args[1]).stream().map(recipe -> recipe.getNamespacedKey().toString()).collect(Collectors.toList());
                    StringUtil.copyPartialMatches(args[2], recipes, results);
                }
            }
        } else {
            if (sender instanceof Player) {
                StringUtil.copyPartialMatches(args[0], COMMANDS, results);
            } else {
                StringUtil.copyPartialMatches(args[0], Arrays.asList("toggle", "delete"), results);
            }
        }
        Collections.sort(results);
        return results;
    }

    @Nonnull
    public static <T extends Collection<? super String>> T copyPartialMatches(@Nonnull String token, @Nonnull Iterable<String> originals, @Nonnull T collection) throws UnsupportedOperationException, IllegalArgumentException {
        Validate.notNull(token, "Search token cannot be null");
        Validate.notNull(collection, "Collection cannot be null");
        Validate.notNull(originals, "Originals cannot be null");
        Iterator var4 = originals.iterator();
        while (var4.hasNext()) {
            String string = (String) var4.next();
            if (containsIgnoreCase(string, token)) {
                collection.add(string);
            }
        }
        return collection;
    }

    public static boolean containsIgnoreCase(String string, String other) {
        return string.toLowerCase(Locale.ROOT).contains(other.toLowerCase(Locale.ROOT));
    }
}
