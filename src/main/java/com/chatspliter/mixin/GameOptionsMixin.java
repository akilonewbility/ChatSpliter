package com.chatspliter.mixin;

import com.chatspliter.ChatSpliterMod;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Arrays;

/**
 * Ensures our keybinding is in allKeys so it appears in the Controls screen.
 * No @Final on the shadow so the array can be replaced.
 */
@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {

    @Shadow
    public KeyBinding[] allKeys;

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CallbackInfo ci) {
        for (KeyBinding kb : allKeys) {
            if (kb == ChatSpliterMod.OPEN_CONFIG_KEY) return;
        }
        KeyBinding[] arr = Arrays.copyOf(allKeys, allKeys.length + 1);
        arr[allKeys.length] = ChatSpliterMod.OPEN_CONFIG_KEY;
        allKeys = arr;
    }
}
