package com.pesok999.copemaxxing.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderPlayerEvent;
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
        if (mc.level == null || mc.player == null || mc.getCameraEntity() != mc.player) return;

        if (event.phase == TickEvent.Phase.START) {
            boolean isKeyHeld = KeyBindings.FREE_LOOK.isDown();

            if (isKeyHeld && !FreeLookState.isActive) {
                FreeLookState.isActive = true;

                // start free look from this rotation
                FreeLookState.cameraYaw = mc.player.getYRot();
                FreeLookState.cameraPitch = mc.player.getXRot();

                // save original values
                FreeLookState.savedPlayerYaw = mc.player.getYRot();
                FreeLookState.savedPlayerPitch = mc.player.getXRot();

                mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            } else if (!isKeyHeld && FreeLookState.isActive) {
                FreeLookState.isActive = false;

                // snap player rotation back to where their head was when free look started
                // first person camera follows yRot so this is all that's needed
                mc.player.setYRot(FreeLookState.savedPlayerYaw);
                mc.player.yRotO = FreeLookState.savedPlayerYaw;
                mc.player.setXRot(FreeLookState.savedPlayerPitch);
                mc.player.xRotO = FreeLookState.savedPlayerPitch;
                mc.player.setYHeadRot(FreeLookState.savedPlayerYaw);

                mc.options.setCameraType(CameraType.FIRST_PERSON);
            }

            if (FreeLookState.isActive) {
                // restore original player rotation
                // do this at START before movement/AI code runs.
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
        }
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!FreeLookState.isActive) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getCameraEntity() != mc.player) return;

        FreeLookState.cameraYaw = mc.player.getYRot();
        FreeLookState.cameraPitch = Mth.clamp(mc.player.getXRot(), -90f, 90f);

        event.setYaw(FreeLookState.cameraYaw);
        event.setPitch(FreeLookState.cameraPitch);

        mc.player.setYRot(FreeLookState.cameraYaw);
        mc.player.yRotO = FreeLookState.cameraYaw;
        mc.player.setXRot(FreeLookState.cameraPitch);
        mc.player.xRotO = FreeLookState.cameraPitch;

        mc.player.setYHeadRot(FreeLookState.savedPlayerYaw);
        // xRot stays as cameraPitch here — head pitch is handled in RenderPlayerEvent below
    }

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!FreeLookState.isActive) return;

        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() != mc.player || mc.getCameraEntity() != mc.player) return;

        // Swap xRot to savedPlayerPitch just for this render — head stays still
        event.getEntity().setXRot(FreeLookState.savedPlayerPitch);
        event.getEntity().xRotO = FreeLookState.savedPlayerPitch;
    }

    @SubscribeEvent
    public void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!FreeLookState.isActive) return;

        Minecraft mc = Minecraft.getInstance();
        if (event.getEntity() != mc.player|| mc.getCameraEntity() != mc.player) return;

        // Restore cameraPitch so the next frame's turnPlayer() adds delta correctly
        event.getEntity().setXRot(FreeLookState.cameraPitch);
        event.getEntity().xRotO = FreeLookState.cameraPitch;
    }
}