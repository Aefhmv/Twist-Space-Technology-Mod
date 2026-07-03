package com.Nxer.TwistSpaceTechnology.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumicenergistics.common.storage.AEEssentiaStack;

public class ItemEssentiaHelper {

    /**
     * Convert a CrystalEssence ItemStack to its corresponding native AEEssentiaStack.
     * Returns null if the stack is not a CrystalEssence or has no aspects.
     * 1 CrystalEssence = 1 essentia unit (AEEssentiaStackType.getAmountPerUnit() == 1).
     */
    @Nullable
    public static AEEssentiaStack getAeEssentiaStack(@Nullable ItemStack stack) {
        Aspect asp = readAspectFromCrystal(stack);
        if (asp == null) return null;
        long amount = stack.stackSize;
        if (amount <= 0) return null;
        return new AEEssentiaStack(asp, amount);
    }

    /**
     * Convert an IAEItemStack (CrystalEssence) to its corresponding native AEEssentiaStack.
     */
    @Nullable
    public static AEEssentiaStack getAeEssentiaStack(@Nullable IAEItemStack stack) {
        if (stack == null) return null;
        return getAeEssentiaStack(stack.getItemStack());
    }

    /**
     * Convert a native AEEssentiaStack back to a CrystalEssence IAEItemStack.
     * Returns null if the essentia stack is null or has no aspect.
     */
    @Nullable
    public static IAEItemStack newAeItemStack(@Nullable AEEssentiaStack essentia) {
        if (essentia == null || essentia.getAspect() == null) return null;

        ItemStack crystal = createCrystal(essentia.getAspect());

        long stackSize = essentia.getStackSize();
        if (stackSize <= 0) stackSize = 1;
        if (stackSize > Integer.MAX_VALUE) stackSize = Integer.MAX_VALUE;

        IAEItemStack aeStack = AEItemStack.create(crystal);
        aeStack.setStackSize(stackSize);
        return aeStack;
    }

    /**
     * Read the primary Aspect from a CrystalEssence or custom tagged ItemStack.
     */
    @Nullable
    public static Aspect readAspectFromCrystal(@Nullable ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) return null;

        // TC CrystalEssence - aspects stored in root NBT
        if (stack.getItem() instanceof thaumcraft.common.items.ItemCrystalEssence) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                AspectList aspects = new AspectList();
                aspects.readFromNBT(tag);
                if (aspects.size() > 0) {
                    return aspects.getAspects()[0];
                }
            }
        }

        // Generic tagged item
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("aspect")) {
                Aspect aspect = Aspect.getAspect(tag.getString("aspect"));
                if (aspect != null) return aspect;
            }
            if (tag.hasKey("Aspects")) {
                AspectList list = new AspectList();
                list.readFromNBT(tag.getCompoundTag("Aspects"));
                if (list.size() > 0) return list.getAspects()[0];
            }
        }

        return null;
    }

    /**
     * Create a single CrystalEssence ItemStack carrying the given Aspect.
     */
    public static ItemStack createCrystal(Aspect asp) {
        ItemStack stack = new ItemStack(thaumcraft.common.config.ConfigItems.itemCrystalEssence, 1);
        AspectList aspects = new AspectList().add(asp, 1);
        NBTTagCompound tag = new NBTTagCompound();
        aspects.writeToNBT(tag);
        stack.setTagCompound(tag);
        return stack;
    }

    public static ItemStack createCrystal(Aspect asp, int count) {
        ItemStack stack = createCrystal(asp);
        stack.stackSize = count;
        return stack;
    }

    /**
     * Based on two input Aspects, return the compound Aspect synthesised from them (if any).
     */
    @Nullable
    public static Aspect findCombinedAspect(Aspect aspectA, Aspect aspectB) {
        for (Aspect candidate : Aspect.aspects.values()) {
            Aspect[] comps = candidate.getComponents();
            if (comps != null && comps.length == 2) {
                if ((comps[0] == aspectA && comps[1] == aspectB) || (comps[0] == aspectB && comps[1] == aspectA)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static final Map<Aspect, Map<Aspect, Aspect>> COMBO_CACHE = new HashMap<>();

    @Nullable
    public static Aspect findCombinedAspectCached(Aspect a, Aspect b) {
        Map<Aspect, Aspect> inner = COMBO_CACHE.get(a);
        if (inner != null && inner.containsKey(b)) {
            return inner.get(b);
        }
        inner = COMBO_CACHE.get(b);
        if (inner != null && inner.containsKey(a)) {
            return inner.get(a);
        }
        Aspect result = findCombinedAspect(a, b);
        if (result != null) {
            COMBO_CACHE.computeIfAbsent(a, k -> new HashMap<>())
                .put(b, result);
            COMBO_CACHE.computeIfAbsent(b, k -> new HashMap<>())
                .put(a, result);
        }
        return result;
    }

}
