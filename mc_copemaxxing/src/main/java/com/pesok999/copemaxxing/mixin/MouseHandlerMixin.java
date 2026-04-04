package com.pesok999.copemaxxing.mixin;

import com.pesok999.copemaxxing.client.FreeLookState;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// intercepts mouse input and redirects it to our own free look system
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @Redirect(
            method = "turnPlayer(D)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;turn(DD)V"
            )
    )
    private void redirectTurnToCamera(Entity entity, double yawDelta, double pitchDelta) {
        if (FreeLookState.isActive) {
            FreeLookState.yaw += (float) yawDelta;
            FreeLookState.pitch += (float) pitchDelta;

            // clamp so we cant turn the camera over
            FreeLookState.pitch = Mth.clamp(FreeLookState.pitch, -90f, 90f);
        }
        else {
            // else turn entity like default
            entity.turn(yawDelta, pitchDelta);
        }
    }
}
