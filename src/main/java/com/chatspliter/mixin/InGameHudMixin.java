package com.chatspliter.mixin;

import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private ChatComponent chat;

    @Shadow
    private int tickCount;

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(GuiGraphics context, DeltaTracker partialTick, CallbackInfo ci) {
        try {
            ChatHudManager.getInstance().render(context, tickCount);
        } catch (Exception e) {
            // Silently ignore rendering errors
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(GuiGraphics context, DeltaTracker partialTick, CallbackInfo ci) {
        if (tickCount % 20 == 0) {
            try {
                ChatHudManager.getInstance().refreshAll();
            } catch (Exception e) {
                // Silently ignore
            }
        }
    }
}
