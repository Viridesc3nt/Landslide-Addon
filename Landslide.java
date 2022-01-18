package me.justinjaques.landslide;


import com.jedk1.jedcore.util.TempFallingBlock;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class Landslide extends EarthAbility implements AddonAbility {

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
    private Vector directionMain;
    private Vector directionRight;
    private Vector directionLeft;
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
    }


    public Landslide(Player player) {
        super(player);
        setFields();

        Block block = getEarthSourceBlock(player, "Landslide", SOURCE_RANGE);
        if(block == null) {
            return;
        }
        distanceTravelled = 0;
        mainSourceBlock = block;
        sourceBlockLeft =  GeneralMethods.getLeftSide(mainSourceBlock.getLocation(), 1).getBlock();
        sourceBlockRight = GeneralMethods.getRightSide(mainSourceBlock.getLocation(), 1).getBlock();
        locationMain = mainSourceBlock.getLocation().add(.5, .5, .5);
        locationRight = sourceBlockRight.getLocation().add(.5, .5, .5);
        locationLeft = sourceBlockLeft.getLocation().add(.5, .5, .5);

        state = States.SOURCE_SELECTED;


        start();

    }

    public void onClick() {
        if(state == States.SOURCE_SELECTED) {
            directionMain = GeneralMethods.getDirection(locationMain, GeneralMethods.getTargetedLocation(player, SOURCE_RANGE * SOURCE_RANGE)).normalize().multiply(SPEED);
            directionRight = GeneralMethods.getDirection(locationRight, GeneralMethods.getTargetedLocation(player, SOURCE_RANGE * SOURCE_RANGE)).normalize().multiply(SPEED);
            directionLeft = GeneralMethods.getDirection(locationLeft, GeneralMethods.getTargetedLocation(player, SOURCE_RANGE * SOURCE_RANGE)).normalize().multiply(SPEED);
            state = States.TRAVELLING;

        }

    }

    public void Line(Location location, Vector direction, long range) {
        playEarthbendingSound(location);
        location.add(direction);
        while(distanceTravelled > range) {
            new TempFallingBlock(location, location.getBlock().getType().createBlockData(), new Vector(0.0, 0.35, 0.0), this);
        }

    }

    public void removeWithCooldown() {
        remove();
        bPlayer.addCooldown(this);
        }

    private void progressSourceSelected() {
        System.out.println("Source selected");
        if(mainSourceBlock.getLocation().distanceSquared(player.getLocation()) > SOURCE_RANGE * SOURCE_RANGE || !isEarthbendable(player, mainSourceBlock)) {
            remove();
        }



    }

    private void progressTravelling() {
        locationMain.add(directionMain);
        locationLeft.add(directionLeft);
        locationRight.add(directionRight);

        distanceTravelled += SPEED;

        Line(locationMain, directionMain, RANGE);
        Line(locationRight, directionRight, RANGE);
        Line(locationLeft, directionLeft, RANGE);


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
    public String getName() {
        return NAME;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public void load() {
        perm = new Permission("bending.ability.Landslide");
        ProjectKorra.plugin.getServer().getPluginManager().addPermission(perm);
        listener = new LandslideListener();
        ConfigManager.defaultConfig.get().addDefault(path+"COOLDOWN", 6000);
        ConfigManager.defaultConfig.get().addDefault(path+"RANGE", 10);
        ConfigManager.defaultConfig.get().addDefault(path+"SOURCE_RANGE", 4);
        ConfigManager.defaultConfig.get().addDefault(path+"SPEED", 3);
        ConfigManager.defaultConfig.get().addDefault(path+"DAMAGE", 2);
        ConfigManager.defaultConfig.save();
        ProjectKorra.plugin.getServer().getPluginManager().registerEvents(listener, ProjectKorra.plugin);


    }

    @Override
    public void stop() {
        ProjectKorra.plugin.getServer().getPluginManager().removePermission(perm);
        HandlerList.unregisterAll(listener);

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
