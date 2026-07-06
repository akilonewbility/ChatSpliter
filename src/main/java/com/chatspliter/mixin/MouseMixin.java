package com.chatspliter.mixin;

import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Handle mouse clicks and scroll on filtered HUDs.
 * Only active in-game (no screen) or when chat is open.
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {

    @Unique
    private double sx() {
        var c = MinecraftClient.getInstance();
        return ((Mouse) (Object) this).getX() / c.getWindow().getScaleFactor();
    }

    @Unique
    private double sy() {
        var c = MinecraftClient.getInstance();
        return ((Mouse) (Object) this).getY() / c.getWindow().getScaleFactor();
    }

    @Unique
    private boolean shouldHandle() {
        var s = MinecraftClient.getInstance().currentScreen;
        return s == null || s instanceof ChatScreen;
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"), cancellable = true)
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (!shouldHandle()) return;

        if (action == GLFW.GLFW_PRESS && button == 0) {
            if (ChatHudManager.getInstance().onMouseClick(sx(), sy(), button)) {
                ci.cancel();
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            ChatHudManager.getInstance().onMouseRelease();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horiz, double vert, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ChatScreen) {
            ChatHudManager.getInstance().onMouseScroll(sx(), sy(), vert);
        }
    }
}
