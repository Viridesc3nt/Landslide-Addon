package me.justinjaques.landslide;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Objects;


public class LandslideListener implements Listener {
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

        if(CoreAbility.getAbility(Landslide.class).isStarted()) {
            event.setCancelled(true);
        }
        if(bPlayer.canBend(CoreAbility.getAbility(Landslide.class))) {
            new Landslide(player);
        }
    }

    @EventHandler
    public void check(EntityChangeBlockEvent event) {
        if (event.getEntity().hasMetadata("Landslide")) {
            ((TempBlock) Objects.requireNonNull(event.getEntity().getMetadata("PK::Viridescent::Landslide").get(0).value())).revertBlock();
            event.setCancelled(true);
        }

    }
}
