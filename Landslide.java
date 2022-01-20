package me.justinjaques.landslide;



import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;
import java.util.List;

public final class Landslide extends EarthAbility implements AddonAbility, Listener {

    private enum States {
        SOURCE_SELECTED, TRAVELLING
    }

    private static final String AUTHOR = ChatColor.GREEN + "Viridescent_";
    private static final String VERSION = ChatColor.GREEN + "1.0.0";
    private static final String NAME = "Landslide";
    private static long COOLDOWN;
    private static long RANGE;
    private static long SOURCE_RANGE;
    private static double SPEED;
    static String path = "ExtraAbilities.Viridescent_.Earth.Landslide.";
    private static double DAMAGE;

    private Location locationMain;
    private Location locationRight;
    private Location locationLeft;
    private Listener listener;
    private Permission perm;
    private Vector direction;
    private double knockBackX;
    private double knockBackY;
    private Vector Knockback;

    private double distanceTravelled;
    private Block mainSourceBlock;
    private Block sourceBlockLeft;
    private Block sourceBlockRight;
    private States state;

    private void setFields() {
        SPEED = ConfigManager.defaultConfig.get().getDouble(path+"SPEED");
        SOURCE_RANGE = ConfigManager.defaultConfig.get().getLong(path+"SOURCE_RANGE");
        COOLDOWN = ConfigManager.defaultConfig.get().getLong(path+"COOLDOWN");
        RANGE = ConfigManager.defaultConfig.get().getLong(path+"RANGE");
        DAMAGE = ConfigManager.defaultConfig.get().getDouble(path+"DAMAGE");
        knockBackX = ConfigManager.defaultConfig.get().getDouble(path+"knockBackX");
        knockBackY = ConfigManager.defaultConfig.get().getDouble(path+"knockBackY");
    }


    public Landslide(Player player) {
        super(player);
        setFields();

        Block block = getEarthSourceBlock(player, "PK::Viridescent::Landslide", SOURCE_RANGE);
        if(block == null) {
            return;
        }
        mainSourceBlock = block;
        direction = player.getLocation().getDirection().setY(0);
        mainSourceBlock.getLocation().setDirection(direction);
        locationMain = mainSourceBlock.getLocation().add(0.5, 0.5, 0.5).setDirection(player.getLocation().getDirection());
        sourceBlockLeft =  GeneralMethods.getLeftSide(locationMain.setDirection(player.getLocation().getDirection()), 1).getBlock();
        sourceBlockRight = GeneralMethods.getRightSide(locationMain.setDirection(player.getLocation().getDirection()), 1).getBlock();
        locationRight = sourceBlockRight.getLocation().add(.5, .5, .5);
        locationLeft = sourceBlockLeft.getLocation().add(.5, .5, .5);
        state = States.SOURCE_SELECTED;

        if(!bPlayer.isOnCooldown(this)) {
            start();

        }

    }

    public void removeWithCooldown() {
        remove();
        bPlayer.addCooldown(this);
    }

    private void affectTargets() {
        List<Entity> targets = GeneralMethods.getEntitiesAroundPoint(locationMain, 3);
            for(Entity target : targets) {
                if(target.getUniqueId() == player.getUniqueId()) {
                    continue;
                }
                if(target instanceof LivingEntity) {
                    DamageHandler.damageEntity(target, DAMAGE, this);
                    Knockback = new Vector(knockBackX, knockBackY, 0);
                    target.setVelocity(Knockback);
                }
            }

        }

    private boolean climb() {
        Block above = locationMain.getBlock().getRelative(BlockFace.UP);
        if (!isTransparent(above)) {
            locationMain.add(0, 1, 0);
            locationLeft.add(0, 1, 0);
            locationRight.add(0, 1, 0);
            above = locationMain.getBlock().getRelative(BlockFace.UP);
            return isEarthbendable(locationMain.getBlock()) && isTransparent(above);
        } else if (isTransparent(locationMain.getBlock()) ) {
            locationMain.add(0, -1, 0);
            locationLeft.add(0, -1, 0);
            locationRight.add(0, -1, 0);
            return isEarthbendable(locationMain.getBlock());
        }
        return true;
    }


    public void Line(Location loc1, Location loc2, Location loc3, Vector direction) {
        playEarthbendingSound(locationMain);
        TempBlock airBlock = new TempBlock(locationMain.getBlock(), Material.AIR, locationMain.getBlock().getBlockData());
        TempBlock airBlock2 = new TempBlock(locationRight.getBlock(), Material.AIR, locationRight.getBlock().getBlockData());
        TempBlock airBlock3 = new TempBlock(locationLeft.getBlock(), Material.AIR, locationLeft.getBlock().getBlockData());

        FallingBlock b1 = GeneralMethods.spawnFallingBlock(loc1.clone(), loc1.getBlock().getType(),loc1.getBlock().getType().createBlockData());
        FallingBlock b2 = GeneralMethods.spawnFallingBlock(loc2.clone(), loc2.getBlock().getType(),loc2.getBlock().getType().createBlockData());
        FallingBlock b3 = GeneralMethods.spawnFallingBlock(loc3.clone(), loc3.getBlock().getType(),loc3.getBlock().getType().createBlockData());
        b1.setDropItem(false);
        b2.setDropItem(false);
        b3.setDropItem(false);
        loc1.add(direction);
        loc2.add(direction);
        loc3.add(direction);
        b1.setMetadata("PK::Viridescent::Landslide", new FixedMetadataValue(ProjectKorra.plugin, airBlock));
        b2.setMetadata("PK::Viridescent::Landslide", new FixedMetadataValue(ProjectKorra.plugin, airBlock2));
        b3.setMetadata("PK::Viridescent::Landslide", new FixedMetadataValue(ProjectKorra.plugin, airBlock3));

        for (int i = 0; i < SPEED; i++) {
            b1.setVelocity(new Vector(0,0.35, 0));
            b2.setVelocity(new Vector(0,0.35, 0));
            b3.setVelocity(new Vector(0,0.35, 0));
            affectTargets();
        }

    }


    private void progressSourceSelected() {
        if(mainSourceBlock.getLocation().distanceSquared(player.getLocation()) > SOURCE_RANGE * SOURCE_RANGE || !isEarthbendable(player, mainSourceBlock)) {
            remove();
        }

    }

    private void progressTravelling() {
        distanceTravelled += SPEED;
        Line(locationMain, locationLeft, locationRight, direction);
        climb();
        System.out.println(distanceTravelled);
        if(distanceTravelled >= RANGE)  {
            removeWithCooldown();
        }
    }

    @Override
    public void progress() {
        if(!bPlayer.canBend(this)) {
            removeWithCooldown();
        }
        switch(state) {
            case SOURCE_SELECTED:
                progressSourceSelected();

            case TRAVELLING:
                progressTravelling();
        }

    }

    @Override
    public boolean isSneakAbility() {
        return true;
    }

    @Override
    public boolean isHarmlessAbility() {
        return false;
    }

    @Override
    public long getCooldown() {
        return COOLDOWN;
    }

    @Override
    public String getInstructions() {
        return ChatColor.GREEN + "Simply press SNEAK on an Earthbendable around you to send a Landslide at your target, dealing considerable damage and knockback.";
    }

    @Override
    public String getDescription() {
        return ChatColor.GREEN + "Landslide is an Earthbending technique that allows it's user to manipulate the Earth under them and send masses of Rock and Dirt at them. This technique was often used by Chief of Metalbending, Toph Beifong.";
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void load() {
        listener = new LandslideListener();
        perm = new Permission("bending.ability.Landslide");
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);
        ConfigManager.defaultConfig.get().addDefault(path+"COOLDOWN", 6000);
        ConfigManager.defaultConfig.get().addDefault(path+"RANGE", 80);
        ConfigManager.defaultConfig.get().addDefault(path+"SOURCE_RANGE", 4);
        ConfigManager.defaultConfig.get().addDefault(path+"SPEED", 5);
        ConfigManager.defaultConfig.get().addDefault(path+"DAMAGE", 2);
        ConfigManager.defaultConfig.get().addDefault(path+"knockBackX", 1);
        ConfigManager.defaultConfig.get().addDefault(path+"knockBackY", 1);
        ConfigManager.defaultConfig.save();

    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
    }

    @Override
    public String getAuthor() {
        return AUTHOR;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}

