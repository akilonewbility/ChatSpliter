package com.chatspliter.mixin;

import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Minecraft self = (Minecraft) (Object) this;

        // Process HUD drag
        try {
            if (ChatHudManager.getInstance().isAnyDragging()) {
                double mx = self.mouseHandler.xpos() / (double) self.getWindow().getGuiScale();
                double my = self.mouseHandler.ypos() / (double) self.getWindow().getGuiScale();
                ChatHudManager.getInstance().onMouseDrag(mx, my);
            }
        } catch (Exception ignored) {}
    }
}
