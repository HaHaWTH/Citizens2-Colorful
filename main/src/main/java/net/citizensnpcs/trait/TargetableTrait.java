package net.citizensnpcs.trait;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.entity.Monster;

@TraitName("targetable")
public class TargetableTrait extends Trait {
    @Persist
    private Boolean targetable;
    private Set<UUID> targeters;

    public TargetableTrait() {
        super("targetable");
    }

    // Only for internal use
    public void addTargeter(UUID uuid) {
        if (targeters == null) {
            targeters = new HashSet<>();
        }
        targeters.add(uuid);
    }

    public void clearTargeters() {
        if (targeters == null)
            return;

        if (SUPPORTS_GET_ENTITY) {
            for (UUID entityUUID : targeters) {
                final Entity entity = Bukkit.getEntity(entityUUID);
                if (entity instanceof Monster) {
                    if (entity.isValid()) {
                        ((Monster) entity).setTarget(null);
                    }
                }
            }
        }
        targeters = null;
    }

    public boolean isTargetable() {
        return targetable == null ? !npc.isProtected() : targetable;
    }

    @Override
    public void onDespawn() {
        clearTargeters();
    }

    // Only for internal use
    public void removeTargeter(UUID uuid) {
        if (targeters != null) {
            targeters.remove(uuid);
        }
    }

    public void setTargetable(boolean targetable) {
        if (Boolean.valueOf(targetable).equals(this.targetable))
            return;
        this.targetable = targetable;
        if (!targetable) {
            clearTargeters();
        }
    }

    private static boolean SUPPORTS_GET_ENTITY = true;
    static {
        try {
            Bukkit.class.getMethod("getEntity", UUID.class);
        } catch (NoSuchMethodException e) {
            SUPPORTS_GET_ENTITY = false;
        }
    }
}
