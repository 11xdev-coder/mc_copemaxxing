package com.pesok999.copemaxxing.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mc_copemaxxing", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LowFireAndShield {
    private static final ResourceLocation FIRE_OVERLAY = new ResourceLocation("minecraft", "fire");
    // push down by this % of screen height
    private static final float FIRE_Y = 0.8F;
    private static final float SHIELD_Y_OFFSET = -0.5f;

    @SubscribeEvent
    public static void onPreFireOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!event.getOverlay().id().equals(FIRE_OVERLAY)) return;

        event.setCanceled(true); // stop vanilla from drawing it

        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        TextureAtlasSprite sprite = mc.getBlockRenderer()
                .getBlockModelShaper()
                .getParticleIcon(Blocks.FIRE.defaultBlockState());

        GuiGraphics gui = event.getGuiGraphics();
        int yOffset = (int)(screenHeight * FIRE_Y);

        // Vanilla draws fire in two layers slightly offset, we replicate that
        gui.blit(sprite.atlasLocation(),
                0, yOffset, 0,
                sprite.getU0(), sprite.getV0(),
                screenWidth / 2, screenHeight,
                (int) (sprite.getU1() - sprite.getU0()), (int) (sprite.getV1() - sprite.getV0())
        );
        gui.blit(sprite.atlasLocation(),
                screenWidth / 2, yOffset, 0,
                sprite.getU0(), sprite.getV0(),
                screenWidth / 2, screenHeight,
                (int) (sprite.getU1() - sprite.getU0()), (int) (sprite.getV1() - sprite.getV0())
        );
    }

    @SubscribeEvent
    public static void onPostFireOverlay(RenderGuiOverlayEvent.Post event) {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        var heldItem = (event.getHand() == InteractionHand.MAIN_HAND) ? player.getMainHandItem() : player.getOffhandItem();
        if (!(heldItem.getItem() instanceof ShieldItem)) return;

        boolean isBlocking = player.isUsingItem() && player.getUsedItemHand() == event.getHand();
        float offset = isBlocking ? SHIELD_Y_OFFSET : SHIELD_Y_OFFSET * 0.5f;
        event.getPoseStack().translate(0, offset, 0);
    }
}
