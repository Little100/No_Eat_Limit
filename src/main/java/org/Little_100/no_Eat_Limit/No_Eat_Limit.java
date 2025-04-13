package org.Little_100.no_Eat_Limit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class No_Eat_Limit extends JavaPlugin implements Listener {

    // 玩家冷却时间映射表
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    // 冷却时间设置为1秒 (1000毫秒)
    private final long COOLDOWN_TIME = 1000;
    // 饥饿值恢复延迟(ticks), 4 ticks = 0.2秒
    private final long HUNGER_RESTORE_DELAY = 4L;

    @Override
    public void onEnable() {
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("没有限制的吃食物插件已经启动 现在玩家可以在任何情况下和吃金苹果一样的吃食物了");
    }

    @Override
    public void onDisable() {
        getLogger().info("没有限制的吃食物插件已经关闭");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 检查是否右键点击
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // 检查物品是否为空
        if (item == null) {
            return;
        }

        // 检查冷却时间
        if (isOnCooldown(player)) {
            return;
        }

        Material itemType = item.getType();
        
        // 检查是否为可食用的普通食物（不是金苹果等特殊食物）
        if (isRegularFood(itemType) && player.getFoodLevel() == 20) {
            // 设置饥饿值为19，让玩家可以吃食物
            player.setFoodLevel(19);
            
            // 延迟恢复饥饿值为20
            getServer().getScheduler().runTaskLater(this, () -> {
                if (player.isOnline()) {
                    player.setFoodLevel(20);
                }
            }, HUNGER_RESTORE_DELAY);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 检查是否为普通食物
        if (isRegularFood(item.getType())) {
            // 设置1秒冷却
            setCooldown(player);
        }
    }

    // 检查是否为普通食物
    private boolean isRegularFood(Material material) {
        return material.isEdible() && 
               material != Material.GOLDEN_APPLE && 
               material != Material.ENCHANTED_GOLDEN_APPLE;
    }

    // 检查玩家是否在冷却中
    private boolean isOnCooldown(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (!cooldowns.containsKey(playerUUID)) {
            return false;
        }

        long lastInteractTime = cooldowns.get(playerUUID);
        long currentTime = System.currentTimeMillis();

        // 如果冷却时间已过
        if (currentTime - lastInteractTime >= COOLDOWN_TIME) {
            cooldowns.remove(playerUUID);
            return false;
        }

        return true;
    }

    // 设置玩家冷却时间
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}
