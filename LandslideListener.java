package me.justinjaques.landslide;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class LandslideListener implements Listener {
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
        if(bPlayer.canBend(CoreAbility.getAbility(Landslide.class))) {
            new Landslide(player);
        }
    }

    @EventHandler
    public void check(EntityChangeBlockEvent event) {
        if (event.getEntity().hasMetadata("Landslide")) {
            //event.setCancelled(true);
        }

    }
}
