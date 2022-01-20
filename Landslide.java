package me.justinjaques.landslide;



import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;
import java.util.Random;
public final class Landslide extends EarthAbility implements AddonAbility, Listener {

    private enum States {
        SOURCE_SELECTED, TRAVELLING
    }

    private static final String AUTHOR = ChatColor.GREEN + "Viridescent_";
    private static final String VERSION = ChatColor.GREEN + "1.0.0";
    private static final String NAME = "Landslide";
    private static long COOLDOWN;
    private static long RANGE;
    Random rand = new Random();
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
    private double distanceTravelled;
    private Block mainSourceBlock;
    private Block sourceBlockLeft;
    private Block sourceBlockRight;
    private States state;
    private Location mainSourceLocation;

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
            System.out.println("Block null");
            return;
        }
        distanceTravelled = 0;
        mainSourceBlock = block;
        sourceBlockLeft =  GeneralMethods.getLeftSide(mainSourceBlock.getLocation(), 1).getBlock();
        sourceBlockRight = GeneralMethods.getRightSide(mainSourceBlock.getLocation(), 1).getBlock();
        locationMain = mainSourceBlock.getLocation().add(0.5, 0.5, 0.5).setDirection(player.getLocation().getDirection());
        locationRight = sourceBlockRight.getLocation().add(.5, .5, .5);
        locationLeft = sourceBlockLeft.getLocation().add(.5, .5, .5);
        System.out.println(locationMain);
        System.out.println(locationRight);
        System.out.println(locationLeft);
        direction = player.getLocation().getDirection().setY(0);
        state = States.SOURCE_SELECTED;

        start();

    }

    public void onClick() {
        if(state == States.SOURCE_SELECTED) {
            direction = player.getLocation().getDirection().setY(0);
            locationMain.add(direction);
            locationLeft.add(direction);
            locationRight.add(direction);
            state = States.TRAVELLING;

        }

    }

    public void removeWithCooldown() {
        remove();
        bPlayer.addCooldown(this);
    }


    public void Line(Location loc1, Location loc2, Location loc3, Vector direction) {
        playEarthbendingSound(locationMain);
        this.direction = direction;
        //double yRandom = rand.nextInt(2);
        FallingBlock b1 = GeneralMethods.spawnFallingBlock(loc1, loc1.getBlock().getType(),loc1.getBlock().getType().createBlockData());
        FallingBlock b2 = GeneralMethods.spawnFallingBlock(loc2, loc2.getBlock().getType(),loc2.getBlock().getType().createBlockData());
        FallingBlock b3 =   GeneralMethods.spawnFallingBlock(loc3, loc3.getBlock().getType(),loc3.getBlock().getType().createBlockData());
        loc1.add(direction);
        loc2.add(direction);
        loc3.add(direction);
        b1.setMetadata("Landslide", new FixedMetadataValue(ProjectKorra.plugin, 1));
        b2.setMetadata("Landslide", new FixedMetadataValue(ProjectKorra.plugin, 1));
        b3.setMetadata("Landslide", new FixedMetadataValue(ProjectKorra.plugin, 1));
        for (int i = 0; i < SPEED; i++) {
            b1.setVelocity(new Vector(0,0.35, 0));
            b2.setVelocity(new Vector(0,0.35, 0));
            b3.setVelocity(new Vector(0,0.35, 0));
        }
        removeWithCooldown();
    }



    private void progressSourceSelected() {
        if(mainSourceBlock.getLocation().distanceSquared(player.getLocation()) > SOURCE_RANGE * SOURCE_RANGE || !isEarthbendable(player, mainSourceBlock)) {
            remove();
        }



    }

    private void progressTravelling() {
        distanceTravelled += SPEED;
        Line(locationMain, locationLeft, locationRight, direction);

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
        ConfigManager.defaultConfig.get().addDefault(path+"RANGE", 25);
        ConfigManager.defaultConfig.get().addDefault(path+"SOURCE_RANGE", 4);
        ConfigManager.defaultConfig.get().addDefault(path+"SPEED", 40);
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
