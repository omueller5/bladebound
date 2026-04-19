package net.owen.bladebound.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class BarrierEntity extends Entity {

    private UUID ownerUuid;
    private int lifeTicks = 0;

    private static final double HALF_W = 0.85;
    private static final double HALF_H = 1.10;
    private static final double HALF_D = 0.20;

    public BarrierEntity(EntityType<? extends BarrierEntity> type, World world) {
        super(type, world);
        this.noClip = true;
        this.setNoGravity(true);
    }

    @Override
    public boolean canHit() {
        return false;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void refreshLife() {
        this.lifeTicks = 6;
    }

    @Override
    protected void initDataTracker(net.minecraft.entity.data.DataTracker.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.getEntityWorld().isClient()) {
            lifeTicks--;
            if (lifeTicks <= 0) {
                this.discard();
                return;
            }

            if (ownerUuid == null) {
                this.discard();
                return;
            }

            var owner = this.getEntityWorld().getPlayerByUuid(ownerUuid);
            if (owner == null) {
                this.discard();
                return;
            }

            Vec3d look = owner.getRotationVec(1.0f).normalize();
            Vec3d eye = owner.getEyePos();

            Vec3d center = eye.add(look.multiply(1.10));
            this.setPos(center.x, center.y, center.z);

            this.setYaw(owner.getYaw(1.0f));
            this.setPitch(owner.getPitch(1.0f));

            this.setBoundingBox(new Box(
                    center.x - HALF_W, center.y - HALF_H, center.z - HALF_D,
                    center.x + HALF_W, center.y + HALF_H, center.z + HALF_D
            ));
        } else {
            if (this.age % 12 == 0) {
                for (int i = 0; i < 2; i++) {
                    double ox = (this.getEntityWorld().random.nextDouble() * 2.0 - 1.0) * 0.60;
                    double oy = (this.getEntityWorld().random.nextDouble() * 2.0 - 1.0) * 0.85;
                    double oz = (this.getEntityWorld().random.nextDouble() * 2.0 - 1.0) * 0.02;

                    this.getEntityWorld().addParticleClient(
                            ParticleTypes.ENCHANT,
                            this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                            0.0, 0.01, 0.0
                    );
                }
            }
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.ownerUuid = view.read("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putNullable("Owner", Uuids.INT_STREAM_CODEC, this.ownerUuid);
    }
}