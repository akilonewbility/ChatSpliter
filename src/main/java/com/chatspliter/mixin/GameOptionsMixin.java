package com.chatspliter.mixin;

import com.chatspliter.ChatSpliterMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Arrays;

@Mixin(Options.class)
public abstract class GameOptionsMixin {

    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        for (KeyMapping kb : keyMappings) {
            if (kb == ChatSpliterMod.OPEN_CONFIG_KEY) return;
        }
        KeyMapping[] arr = Arrays.copyOf(keyMappings, keyMappings.length + 1);
        arr[keyMappings.length] = ChatSpliterMod.OPEN_CONFIG_KEY;
        keyMappings = arr;
    }
}
