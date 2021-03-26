package me.cg360.games.tabletop.game.jenga;

import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import me.cg360.games.tabletop.game.jenga.entity.EntityJengaBlock;
import net.cg360.nsapi.commons.Check;

import java.util.Optional;
import java.util.UUID;

public class JengaLayer {

    public static final String NBT_COMPOUND_TOWER = "tower_data";

    public static final String NBT_TOWER_UUID = "tower_uuid";
    public static final String NBT_LAYERS_BELOW_COUNT = "layers_below";
    public static final String NBT_POS_IN_LAYER = "layer_pos";
    public static final String NBT_LAYER_ALTERNATE_AXIS = "layer_alternate_axis";


    protected JengaLayer layerBelow;
    protected UUID towerUUID;
    protected int layersBelowCount;

    protected Location layerOrigin;

    protected float scale;
    protected boolean isAxisAlternate; // Otherwise facing Z

    protected EntityJengaBlock left; // Lowest on the layer's axis
    protected EntityJengaBlock center;
    protected EntityJengaBlock right; // Highest on the layer's axis.


    /** @param belowInStack the layer in the stack that this stack will be placed on top of. */
    public JengaLayer(JengaLayer belowInStack) {
        this(belowInStack.getLayerOrigin().getLocation(), belowInStack.getScale(), !belowInStack.isAxisAlternate()); // Retain scale, flip alternate axis value.
        this.layerBelow = belowInStack;
        this.towerUUID = belowInStack.getTowerUUID(); // Copy UUID, it's now part of the tower :D
        this.layersBelowCount = belowInStack.getLayersBelowCount() + 1;
        this.layerOrigin = layerOrigin.add(0, scale, 0); //Update location, stack y up by 1.
    }

    /**
     * Creates an empty JengaBlock layer. This is the root of a tower.
     * @param layerOrigin the central position of the layer.
     * @param scale the scale of the blocks (and positions) of the layer)
     * @param isAxisAlternate True if the blocks are placed up the X axis. False if they're placed up the Z axis.
     */
    public JengaLayer(Location layerOrigin, float scale, boolean isAxisAlternate) {
        Check.nullParam(layerOrigin, "layerOrigin");
        Check.nullParam(layerOrigin.getLevel(), "layerOrigin.level");

        this.layerBelow = null;
        this.towerUUID = UUID.randomUUID();
        this.layersBelowCount = 0;

        this.layerOrigin = layerOrigin;

        this.scale = scale;
        this.isAxisAlternate = isAxisAlternate;

        this.left = null;
        this.center = null;
        this.right = null;
    }



    /** Spawns the Jenga block of the lower position of the layer's set axis */
    public boolean spawnLeft() {

        if((left == null) || left.isClosed()) {
            Location loc;

            if (isAxisAlternate) {
                loc = new Location(
                        layerOrigin.getX() - scale,
                        layerOrigin.getY(),
                        layerOrigin.getZ(),
                        layerOrigin.getLevel()
                );

            } else {
                loc = new Location(
                        layerOrigin.getX(),
                        layerOrigin.getY(),
                        layerOrigin.getZ() - scale,
                        layerOrigin.getLevel()
                );
            }

            this.left = spawnBlock(loc, 0);
            return true;
        }
        return false;
    }

    /** Spawns the Jenga block in the center of the layer. */
    public boolean spawnCenter() {

        if((center == null) || center.isClosed()) {
            // Spawns in the center anyway, no need for an if statement.
            Location loc = new Location(layerOrigin.getX(), layerOrigin.getY(), layerOrigin.getZ(), layerOrigin.getLevel());

            this.center = spawnBlock(loc, 1);
            return true;
        }
        return false;
    }

    /** Spawns the Jenga block of the higher position of the layer's set axis */
    public boolean spawnRight() {

        if((right == null) || right.isClosed()) {
            Location loc;

            if (isAxisAlternate) {
                loc = new Location(
                        layerOrigin.getX() + scale,
                        layerOrigin.getY(),
                        layerOrigin.getZ(),
                        layerOrigin.getLevel()
                );

            } else {
                loc = new Location(
                        layerOrigin.getX(),
                        layerOrigin.getY(),
                        layerOrigin.getZ() + scale,
                        layerOrigin.getLevel()
                );
            }

            this.right = spawnBlock(loc, 2);
            return true;
        }
        return false;
    }

    /** Spawns all the blocks of the layer if not already spawned. */
    public void fillLayer() {
        spawnLeft();
        spawnCenter();
        spawnRight();
    }

    /**
     * Spawns an entity for a Jenga Block.
     * @param position the position in the world of the EntityJengaBlock
     * @param positionWithinLayer 0 = left; 1 = center; 2 = right;
     * @return the id of the spawned block.
     */
    protected EntityJengaBlock spawnBlock(Location position, int positionWithinLayer) {

        CompoundTag nbt = new CompoundTag()
                .putList(new ListTag<>("Pos")
                        .add(new DoubleTag("", position.getX()))
                        .add(new DoubleTag("", position.getY()))
                        .add(new DoubleTag("", position.getZ())))
                .putList(new ListTag<DoubleTag>("Motion")
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0))
                        .add(new DoubleTag("", 0)))
                .putList(new ListTag<FloatTag>("Rotation")
                        .add(new FloatTag("", isAxisAlternate ? 90f : 0f))
                        .add(new FloatTag("", 0f)))
                .putBoolean("npc", true)
                .putFloat("scale", scale)
                .putCompound(NBT_COMPOUND_TOWER, new CompoundTag()
                        .putString(NBT_TOWER_UUID, towerUUID.toString())
                        .putInt(NBT_POS_IN_LAYER, positionWithinLayer)
                        .putInt(NBT_LAYERS_BELOW_COUNT, layersBelowCount)
                        .putBoolean(NBT_LAYER_ALTERNATE_AXIS, isAxisAlternate)
                );

        nbt.putBoolean("ishuman", true);

        FullChunk chunk = position.getLevel().getChunk((int) Math.floor(position.getX() / 16), (int) Math.floor(position.getZ() / 16), true);
        EntityJengaBlock jengaHuman = new EntityJengaBlock(chunk, nbt);

        jengaHuman.setPositionAndRotation(position, isAxisAlternate ? 90f : 0f, 0);
        jengaHuman.setImmobile(true);
        jengaHuman.setNameTagAlwaysVisible(false);
        jengaHuman.setNameTagVisible(false);
        jengaHuman.setScale(scale);

        jengaHuman.spawnToAll();

        return jengaHuman;
    }

    /** @return the layer below this layer in the Jenga stack. Empty means it is the bottom of the tower.*/
    public Optional<JengaLayer> getLayerBelow() { return Optional.ofNullable(layerBelow); }
    /** @return the unique id of the tower this layer is part of. */
    public UUID getTowerUUID() { return towerUUID; }
    /** @return the amount of layers below this layer. */
    public int getLayersBelowCount() { return layersBelowCount; }

    public Location getLayerOrigin() { return layerOrigin; }
    public float getScale() { return scale; }
    public boolean isAxisAlternate() { return isAxisAlternate; }

    public EntityJengaBlock getLeft() { return left; }
    public EntityJengaBlock getCenter() { return center; }
    public EntityJengaBlock getRight() { return right; }

}