package net.citizensnpcs.nms.v1_12_R1.entity.nonliving;

import net.citizensnpcs.api.npc.MetadataStore;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_12_R1.entity.MobEntityController;
import net.citizensnpcs.nms.v1_12_R1.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_12_R1.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.EntityMinecartMobSpawner;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumPistonReaction;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.World;

public class MinecartSpawnerController extends MobEntityController {
    public MinecartSpawnerController() {
        super(EntityMinecartSpawnerNPC.class);
    }

    @Override
    public Minecart getBukkitEntity() {
        return (Minecart) super.getBukkitEntity();
    }

    public static class EntityMinecartSpawnerNPC extends EntityMinecartMobSpawner implements NPCHolder {
        private final CitizensNPC npc;

        public EntityMinecartSpawnerNPC(World world) {
            this(world, null);
        }

        public EntityMinecartSpawnerNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        public boolean a(EntityPlayer player) {
            return NMS.shouldBroadcastToPlayer(npc, () -> super.a(player));
        }

        @Override
        public void B_() {
            super.B_();
            if (npc != null) {
                npc.update();
                NMSImpl.minecartItemLogic(this);
            }
        }

        @Override
        public boolean bo() {
            return npc == null ? super.bo() : npc.isPushableByFluids();
        }

        @Override
        public void collide(net.minecraft.server.v1_12_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        public EnumPistonReaction getPushReaction() {
            return Util.callPistonPushEvent(npc) ? EnumPistonReaction.IGNORE : super.getPushReaction();
        }

        @Override
        public void die() {
            if (isAccessAllowed()) {
                super.die();
            }
        }

        @Override
        public boolean be() {
            boolean original = super.be();
            if (npc == null) {
                return original;
            } else {
                MetadataStore data = npc.data();
                return data == null ? original : data.get(NPC.Metadata.DEFAULT_PROTECTED, true) || original;
            }
        }
    }
}