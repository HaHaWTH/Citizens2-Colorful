package net.citizensnpcs.trait;

import java.util.function.Function;
import java.util.function.Supplier;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.BoundingBox;
import net.citizensnpcs.api.util.EntityDim;
import net.citizensnpcs.util.NMS;
import org.bukkit.Location;

@TraitName(value = "boundingbox")
public class BoundingBoxTrait extends Trait implements Supplier<BoundingBox> {
    private EntityDim base;
    private Function<EntityDim, BoundingBox> function;
    @Persist
    private float height = -1;
    @Persist
    private float scale = -1;
    @Persist
    private float width = -1;

    public BoundingBoxTrait() {
        super("boundingbox");
    }

    @Override
    public BoundingBox get() {
        Location location = this.npc.getEntity().getLocation();
        if (this.function != null) {
            BoundingBox bb = this.function.apply(this.getAdjustedDimensions());
            NMS.setDimensions(this.npc.getEntity(), bb.toDimensions());
            return bb.add(location);
        }
        EntityDim dim = this.getAdjustedDimensions();
        NMS.setDimensions(this.npc.getEntity(), dim);
        return new BoundingBox(location.getX() - (double) (dim.width / 2.0f), location.getY(), location.getZ() - (double) (dim.width / 2.0f), location.getX() + (double) (dim.width / 2.0f), location.getY() + (double) dim.height, location.getZ() + (double) (dim.width / 2.0f));
    }

    public EntityDim getAdjustedDimensions() {
        EntityDim desired = this.base;
        if (this.scale != -1.0f) {
            desired = desired.mul(this.scale);
        }
        return new EntityDim(this.width == -1.0f ? desired.width : this.width, this.height == -1.0f ? desired.height : this.height);
    }

    @Override
    public void onDespawn() {
        this.npc.data().remove(NPC.Metadata.BOUNDING_BOX_FUNCTION);
    }

    @Override
    public void onRemove() {
        this.onDespawn();
    }

    @Override
    public void onSpawn() {
        this.base = EntityDim.from(this.npc.getEntity());
        this.npc.data().set(NPC.Metadata.BOUNDING_BOX_FUNCTION, this);
    }

    public void setBoundingBoxFunction(Function<EntityDim, BoundingBox> func) {
        this.function = func;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}
