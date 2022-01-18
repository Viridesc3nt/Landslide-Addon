package me.justinjaques.landslide;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class LandslideListener implements Listener {
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if(event.isSneaking()) {
            BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

            if(bPlayer.canBend(CoreAbility.getAbility(player, Landslide.class))) {
                new Landslide(player);
            }

        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if(event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        Landslide landslide = CoreAbility.getAbility(player, Landslide.class);

        if(landslide == null) {
            return;
        }
        landslide.onClick();
    }

}
