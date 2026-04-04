package com.pesok999.copemaxxing.client;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// handles free look toggle and camera movement
public class FreeLookEventHandler {
    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FreeLookEventHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // run at tick start to avoid running twice
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        boolean isKeyHeld = KeyBindings.FREE_LOOK.isDown();

        if (isKeyHeld && !FreeLookState.isActive) {
            // activate free look

            FreeLookState.isActive = true;
            FreeLookState.yaw = mc.player.getYRot();
            FreeLookState.pitch = mc.player.getXRot();

            // enable third person
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        else if (!isKeyHeld && FreeLookState.isActive) {
            FreeLookState.isActive = false;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        // fires every rendered frame right before camera
        // override camera angles to our own angles instead
        if (FreeLookState.isActive) {
            event.setYaw(FreeLookState.yaw);
            event.setPitch(FreeLookState.pitch);
        }
    }
}
