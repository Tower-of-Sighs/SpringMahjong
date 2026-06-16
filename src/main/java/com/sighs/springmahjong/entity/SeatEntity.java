package com.sighs.springmahjong.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Invisible mount entity for sitting on chairs.
 * Player right-clicks a chair → mounts SeatEntity → sits down.
 * Player presses Shift → dismounts → stands up.
 */
public class SeatEntity extends Entity {

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos pos) {
        super(ModEntities.SEAT.get(), level);
        setPos(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5);
        this.noPhysics = true;
        setInvulnerable(true);
    }

    public static void createAndMount(Level level, BlockPos pos, Player player) {
        SeatEntity seat = new SeatEntity(level, pos);
        level.addFreshEntity(seat);
        player.startRiding(seat);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isVehicle()) {
            discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // Invulnerable seat entity
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }
}
