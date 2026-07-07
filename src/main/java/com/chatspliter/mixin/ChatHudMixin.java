package com.chatspliter.mixin;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Component message, CallbackInfo ci) {
        ChatSpliterConfig config = ChatSpliterConfig.getInstance();
        if (!config.enabled) return;

        try {
            int currentTick = (int) (System.currentTimeMillis() / 50);
            ChatHudManager.getInstance().onChatMessage(message, currentTick);

            if (config.hideMatchedFromMain && config.findMatchingGroup(message.getString()) != null) {
                ci.cancel();
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }
}
