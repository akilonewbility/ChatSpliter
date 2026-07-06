package com.chatspliter.mixin;

import com.chatspliter.ChatSpliterMod;
import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        MinecraftClient self = (MinecraftClient) (Object) this;
        try {
            ChatSpliterMod.onClientTick(self);
        } catch (Exception ignored) {}

        // Process HUD drag
        try {
            if (ChatHudManager.getInstance().isAnyDragging()) {
                double mx = self.mouse.getX() / self.getWindow().getScaleFactor();
                double my = self.mouse.getY() / self.getWindow().getScaleFactor();
                ChatHudManager.getInstance().onMouseDrag(mx, my);
            }
        } catch (Exception ignored) {}
    }
}
