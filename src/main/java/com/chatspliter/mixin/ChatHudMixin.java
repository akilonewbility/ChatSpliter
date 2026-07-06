package com.chatspliter.mixin;

import com.chatspliter.config.ChatSpliterConfig;
import com.chatspliter.hud.ChatHudManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin into ChatHud to intercept new chat messages.
 * - Routes messages to filtered HUDs
 * - Optionally hides matched messages from the main chat
 */
@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {
        ChatSpliterConfig config = ChatSpliterConfig.getInstance();
        if (!config.enabled) return;

        try {
            int currentTick = (int) (System.currentTimeMillis() / 50);
            ChatHudManager.getInstance().onChatMessage(message, currentTick);

            // Hide from main chat if the setting is enabled and message matches
            if (config.hideMatchedFromMain && config.findMatchingGroup(message.getString()) != null) {
                ci.cancel();
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }
}
