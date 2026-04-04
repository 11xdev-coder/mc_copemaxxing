package com.pesok999.copemaxxing.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FreeLookEventHandler {
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FreeLookEventHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (event.phase == TickEvent.Phase.START) {
            boolean isKeyHeld = KeyBindings.FREE_LOOK.isDown();

            if (isKeyHeld && !FreeLookState.isActive) {
                FreeLookState.isActive = true;
                FreeLookState.cameraYaw = mc.player.getYRot();
                FreeLookState.cameraPitch = mc.player.getXRot();
                FreeLookState.savedPlayerYaw = mc.player.getYRot();
                FreeLookState.savedPlayerPitch = mc.player.getXRot();
                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (!isKeyHeld && FreeLookState.isActive) {
                FreeLookState.isActive = false;
                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }

            if (FreeLookState.isActive) {
                // Restore real player rotation so WASD movement goes the right direction.
                // We do this at START before movement/AI code runs.
                mc.player.setYRot(FreeLookState.savedPlayerYaw);
                mc.player.yRotO = FreeLookState.savedPlayerYaw;
                mc.player.setXRot(FreeLookState.savedPlayerPitch);
                mc.player.xRotO = FreeLookState.savedPlayerPitch;
            }
        }

        if (event.phase == TickEvent.Phase.END && FreeLookState.isActive) {
            // Renders happen AFTER tick END. Camera.setup() uses yRot to decide
            // which angle to orbit the camera from. Set it to cameraYaw here
            // so the camera sits at the right position in the upcoming render.
            // We'll restore savedPlayerYaw at the next tick START.
            mc.player.setYRot(FreeLookState.cameraYaw);
            mc.player.yRotO = FreeLookState.cameraYaw;
            mc.player.setXRot(FreeLookState.cameraPitch);
            mc.player.xRotO = FreeLookState.cameraPitch;

            // Lock head/body after AI has finished updating them this tick.
            mc.player.setYHeadRot(FreeLookState.savedPlayerYaw);
            mc.player.setYBodyRot(FreeLookState.savedPlayerYaw);
        }
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!FreeLookState.isActive) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // At this point in the render:
        //   tick END set yRot = cameraYaw (or last frame's ComputeCameraAngles did)
        //   turnPlayer() ran and added the mouse delta on top
        //   Camera.setup() already ran using the new yRot for camera position
        //
        // So player.getYRot() == old_cameraYaw + mouseDelta == new cameraYaw.
        // Just read it directly.
        FreeLookState.cameraYaw = mc.player.getYRot();
        FreeLookState.cameraPitch = Mth.clamp(mc.player.getXRot(), -90f, 90f);

        event.setYaw(FreeLookState.cameraYaw);
        event.setPitch(FreeLookState.cameraPitch);

        // Set yRot = cameraYaw as the baseline for next frame's turnPlayer().
        // If another render happens before the next tick (high FPS), turnPlayer()
        // will add the new delta on top of cameraYaw, and this stays correct.
        mc.player.setYRot(FreeLookState.cameraYaw);
        mc.player.yRotO = FreeLookState.cameraYaw;
        mc.player.setXRot(FreeLookState.cameraPitch);
        mc.player.xRotO = FreeLookState.cameraPitch;

        // Lock head/body visuals.
        mc.player.setYHeadRot(FreeLookState.savedPlayerYaw);
        mc.player.setYBodyRot(FreeLookState.savedPlayerYaw);
    }
}