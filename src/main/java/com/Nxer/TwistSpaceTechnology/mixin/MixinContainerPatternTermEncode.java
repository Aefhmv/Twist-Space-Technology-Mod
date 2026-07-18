package com.Nxer.TwistSpaceTechnology.mixin;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.Nxer.TwistSpaceTechnology.util.ItemEssentiaHelper;

import appeng.container.implementations.ContainerPatternTerm;
import appeng.container.slot.SlotRestrictedInput;

/**
 * Mixin to intercept ContainerPatternTerm.encode() at RETURN.
 * encode() is void — after it returns, the pattern ItemStack is already
 * in patternSlotOUT. We modify its NBT in-place and re-put to trigger sync.
 */

// Previous failed attempts targeted decode/read paths (getPatternForItem,
// setTagCompound, writeToNBT), not the actual encoding method.
@Mixin(value = ContainerPatternTerm.class, remap = false)
public abstract class MixinContainerPatternTermEncode {

    @Shadow(remap = false)
    private SlotRestrictedInput patternSlotOUT;

    @Inject(method = "encode", at = @At("RETURN"))
    private void tst$convertOnEncode(CallbackInfo ci) {
        // FMLLog.info("[TST Mixin] encode() RETURN reached");
        ItemStack out = this.patternSlotOUT.getStack();
        /*
         * if (out == null) {
         * FMLLog.info("[TST Mixin] patternSlotOUT stack is null");
         * return;
         * }
         * if (!out.hasTagCompound()) {
         * FMLLog.info("[TST Mixin] patternSlotOUT stack has no NBT tag");
         * return;
         * }
         * FMLLog.info("[TST Mixin] pattern NBT: %s", out.getTagCompound().toString());
         */

        if (ItemEssentiaHelper.convertPatternNBT(out.getTagCompound())) {
            // FMLLog.info("[TST Mixin] Converted glass ampoules on encode() RETURN");
            this.patternSlotOUT.putStack(out); // re-put to force slot change sync
        } else {
            // FMLLog.info("[TST Mixin] convertPatternNBT returned false - no glass ampoule matched");
        }
    }
}
