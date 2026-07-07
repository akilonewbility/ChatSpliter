package com.chatspliter.mixin;

import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseMixin {

    @Unique
    private double sx() {
        var c = Minecraft.getInstance();
        return ((MouseHandler) (Object) this).xpos() / (double) c.getWindow().getGuiScale();
    }

    @Unique
    private double sy() {
        var c = Minecraft.getInstance();
        return ((MouseHandler) (Object) this).ypos() / (double) c.getWindow().getGuiScale();
    }

    @Unique
    private boolean shouldHandle() {
        var s = Minecraft.getInstance().screen;
        return s == null || s instanceof ChatScreen;
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
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

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horiz, double vert, CallbackInfo ci) {
        var client = Minecraft.getInstance();
        if (client.screen instanceof ChatScreen) {
            ChatHudManager.getInstance().onMouseScroll(sx(), sy(), vert);
            ci.cancel();
        }
    }
}
