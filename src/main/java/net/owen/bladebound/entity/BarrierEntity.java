package net.owen.bladebound.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class BarrierEntity extends Entity {

    private UUID ownerUuid;          // server-side only
    private int lifeTicks = 0;

    // visual/physical size of the barrier plane in world units
    private static final double HALF_W = 0.85;
    private static final double HALF_H = 1.10;
    private static final double HALF_D = 0.20; // tiny thickness so AABB isn't flat

    public BarrierEntity(EntityType<? extends BarrierEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);

        // Prevent frustum culling (very common reason "it doesn't render")
        this.ignoreCameraFrustum = true;
    }

    /**
     * IMPORTANT:
     * Make the barrier entity untargetable so your own raycast-based spells
     * (Zoltraak/Frost) do NOT hit it and stop early.
     */
    @Override
    public boolean canHit() {
        return false;
    }

    /**
     * If this method exists in your mappings, it prevents projectiles/rays from treating this
     * as a hittable entity. If it shows red, delete this override and keep canHit().
     */
    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void refreshLife() {
        this.lifeTicks = 6; // refreshed each tick while channeling / active
    }

    // 1.21+ signature
    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
        // no tracked data needed
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getWorld().isClient) {
            lifeTicks--;
            if (lifeTicks <= 0) {
                this.discard();
                return;
            }

            if (ownerUuid == null) {
                this.discard();
                return;
            }

            var owner = this.getWorld().getPlayerByUuid(ownerUuid);
            if (owner == null) {
                this.discard();
                return;
            }

            Vec3d look = owner.getRotationVec(1.0f).normalize();
            Vec3d eye = owner.getEyePos();

            // Barrier 1.10 blocks in front of eyes
            Vec3d center = eye.add(look.multiply(1.10));
            this.setPos(center.x, center.y, center.z);

            // Keep facing same direction as owner
            this.setYaw(owner.getYaw(1.0f));
            this.setPitch(owner.getPitch(1.0f));

            // IMPORTANT: give the entity a non-zero bounding box so it can be rendered/culled properly
            this.setBoundingBox(new Box(
                    center.x - HALF_W, center.y - HALF_H, center.z - HALF_D,
                    center.x + HALF_W, center.y + HALF_H, center.z + HALF_D
            ));
        } else {
            // Client-side particles (light + throttled)
            if (this.age % 12 == 0) {
                for (int i = 0; i < 2; i++) {
                    double ox = (this.getWorld().random.nextDouble() * 2.0 - 1.0) * 0.60;
                    double oy = (this.getWorld().random.nextDouble() * 2.0 - 1.0) * 0.85;
                    double oz = (this.getWorld().random.nextDouble() * 2.0 - 1.0) * 0.02;

                    this.getWorld().addParticle(
                            ParticleTypes.ENCHANT,
                            this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                            0.0, 0.01, 0.0
                    );
                }
            }
        }
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if (nbt.containsUuid("Owner")) {
            ownerUuid = nbt.getUuid("Owner");
        }
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid);
        }
    }
}
