package com.chatspliter.mixin;

import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private ChatHud chatHud;

    @Shadow
    private int ticks;

    /**
     * Render our custom filtered chat HUDs after the main game HUD.
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        try {
            ChatHudManager.getInstance().render(context, ticks);
        } catch (Exception e) {
            // Silently ignore rendering errors
        }
    }

    /**
     * Periodically refresh filtered HUDs to remove expired messages.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void beforeRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ticks % 20 == 0) {
            try {
                ChatHudManager.getInstance().refreshAll();
            } catch (Exception e) {
                // Silently ignore
            }
        }
    }
}
