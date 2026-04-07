package com.pesok999.copemaxxing.client;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

public class FreeCamEntity extends Entity {
    public FreeCamEntity(Level p_19871_) {
        // create a harmless dummy
        super(EntityType.ITEM, p_19871_);
        noPhysics = true;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {}

    @Override
    protected float getEyeHeight(Pose p_19976_, EntityDimensions p_19977_) {
        // eye height = 0 so setPos map directly to camera position
        return 0f;
    }
}
