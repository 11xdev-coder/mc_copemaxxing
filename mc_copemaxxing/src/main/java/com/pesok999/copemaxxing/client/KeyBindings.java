package com.pesok999.copemaxxing.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final KeyMapping FREE_LOOK = new KeyMapping(
            "key.mc_copemaxxing.free_look", // translation key
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT, // default key left alt
            "key.categories.copemaxxing"
    );

    public static final KeyMapping FREE_CAM = new KeyMapping(
            "key.mc_copemaxxing.free_cam",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, // V
            "key.categories.copemaxxing"
    );

    public static void register() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(KeyBindings::onRegisterKeyMappings);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(FREE_LOOK);
        event.register(FREE_CAM);
    }
}
