package com.pesok999.copemaxxing.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class FreeCamEventHandler {
    // blocks/tick
    private static final float SPEED = 0.5f;

    private FreeCamEntity freeCam = null;

    private float savedPlayerYaw = 0f;
    private float savedPlayerPitch = 0f;
    private float frozenPlayerYaw = 0f;
    private float frozenPlayerPitch = 0f;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new FreeCamEventHandler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        // consumeClick() makes it a toggle
        if (KeyBindings.FREE_CAM.consumeClick()) {
            if (freeCam == null) {
                activate(mc);
            }
            else {
                deactivate(mc);
            }
        }

        if (freeCam != null) {
            freeCam.xo = freeCam.getX();
            freeCam.yo = freeCam.getY();
            freeCam.zo = freeCam.getZ();
            freeCam.yRotO = freeCam.getYRot();
            freeCam.xRotO = freeCam.getXRot();

            moveFreeCam(mc);

            mc.player.setDeltaMovement(0, 0, 0);
            mc.player.setYRot(savedPlayerYaw);     // 0
            mc.player.yRotO = savedPlayerYaw;      // 0
            mc.player.setXRot(savedPlayerPitch);   // 0
            mc.player.xRotO = savedPlayerPitch;    // 0
            mc.player.setYHeadRot(frozenPlayerYaw);
            mc.player.setYBodyRot(frozenPlayerYaw);
        }
    }

    public void activate(Minecraft mc) {
        if (mc.player == null) return;

        freeCam = new FreeCamEntity(mc.level);

        // start at player eye level
        double startX = mc.player.getX();
        double eyeY = mc.player.getY();
        double startZ = mc.player.getZ();
        freeCam.setPos(startX, eyeY, startZ);

        freeCam.xo = startX;
        freeCam.yo = eyeY;
        freeCam.zo = startZ;

        float startYaw = mc.player.getYRot();
        float startPitch = mc.player.getXRot();

        freeCam.setYRot(startYaw);
        freeCam.yRotO = startYaw;
        freeCam.setXRot(startPitch);
        freeCam.xRotO = startPitch;

        frozenPlayerYaw = startYaw;
        frozenPlayerPitch = startPitch;
        savedPlayerYaw = 0f;
        savedPlayerPitch = 0f;

        // set custom camera entity to send all mouse calls to it
        mc.setCameraEntity(freeCam);

        // freeze player in place
        mc.player.noPhysics = true;

        // turn off free look
        FreeLookState.isActive = false;
    }

    public void deactivate(Minecraft mc) {
        if (mc.player == null) return;

        mc.setCameraEntity(mc.player);
        mc.player.noPhysics = false;
        freeCam = null;
    }

    public void moveFreeCam(Minecraft mc) {
        long window = mc.getWindow().getWindow();

        boolean wDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_W);
        boolean aDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_A);
        boolean sDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_S);
        boolean dDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_D);
        boolean spaceDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_SPACE);
        boolean shiftDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT);

        float fwd = 0f;
        float strafe = 0f;
        float vert = 0f;

        if (wDown) fwd += SPEED;
        if (aDown) strafe -= SPEED;
        if (sDown) fwd -= SPEED;
        if (dDown) strafe += SPEED;
        if (spaceDown) vert += SPEED;
        if (shiftDown) vert -= SPEED;

        // no vector to build
        if (fwd == 0 && strafe == 0 && vert == 0) return;

        // build movement vector looking in camera direction
        float yawRad = (float) Math.toRadians(freeCam.getYRot());
        float pitchRad = (float) Math.toRadians(freeCam.getXRot());

        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);

        float fx = -sinYaw * cosPitch;
        float fy = -sinPitch;
        float fz = cosYaw * cosPitch;

        float rx = -cosYaw;
        float rz = -sinYaw;

        double newX = freeCam.getX() + fx * fwd + rx * strafe;
        double newY = freeCam.getY() + fy * fwd + vert;
        double newZ = freeCam.getZ() + fz * fwd + rz * strafe;

        freeCam.setPos(newX, newY, newZ);

        freeCam.xo = newX;
        freeCam.yo = newY;
        freeCam.zo = newZ;
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (freeCam == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // delta is just player.xRot directly since we reset to 0 each frame
        float deltaYaw   = mc.player.getYRot();
        float deltaPitch = mc.player.getXRot();

        float newYaw   = freeCam.getYRot() + deltaYaw;
        float newPitch = Mth.clamp(freeCam.getXRot() + deltaPitch, -90f, 90f);

        freeCam.setYRot(newYaw);
        freeCam.yRotO = newYaw;
        freeCam.setXRot(newPitch);
        freeCam.xRotO = newPitch;

        // Reset player to 0 for delta calc next frame
        mc.player.setYRot(savedPlayerYaw);
        mc.player.yRotO = savedPlayerYaw;
        mc.player.setXRot(savedPlayerPitch);
        mc.player.xRotO = savedPlayerPitch;

        // Freeze the player BODY at the original rotation using frozen values
        mc.player.setYHeadRot(frozenPlayerYaw);
        mc.player.setYBodyRot(frozenPlayerYaw);

        event.setYaw(newYaw);
        event.setPitch(newPitch);
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) {
        // Cancel hand rendering entirely while free cam is active
        if (freeCam != null) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (freeCam == null) return;

        // Cancel just the overlays you want hidden
        var overlay = event.getOverlay();
        if (overlay == VanillaGuiOverlay.EXPERIENCE_BAR.type()
                || overlay == VanillaGuiOverlay.JUMP_BAR.type()
                || overlay == VanillaGuiOverlay.HOTBAR.type()) {
            event.setCanceled(true);
        }
    }
}
