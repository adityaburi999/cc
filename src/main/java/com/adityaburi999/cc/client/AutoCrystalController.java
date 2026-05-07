package com.adityaburi999.cc.client;

import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoCrystalController {
    private boolean active;
    private int pendingStartRights;
    private boolean nextLeft;
    private long nextActionAtNanos;

    public void tick(MinecraftClient client, AutoCrystalConfig config) {
        if (!shouldRun(client, config)) {
            reset();
            return;
        }

        if (!active) {
            active = true;
            pendingStartRights = 2;
            nextLeft = true;
            nextActionAtNanos = System.nanoTime();
        }

        long now = System.nanoTime();
        if (now < nextActionAtNanos) {
            return;
        }

        boolean misclick = ThreadLocalRandom.current().nextDouble() < config.misclickChance;
        if (pendingStartRights > 0) {
            if (!misclick) {
                performRightClick(client);
            }
            pendingStartRights--;
            if (pendingStartRights == 0) {
                nextLeft = true;
            }
        } else {
            if (!misclick) {
                if (nextLeft) {
                    performLeftClick(client);
                } else {
                    performRightClick(client);
                }
            }
            nextLeft = !nextLeft;
        }

        scheduleNext(config);
    }

    private void scheduleNext(AutoCrystalConfig config) {
        double minCps = config.minCps();
        double maxCps = Math.max(minCps, config.maxCps);
        double cps = minCps + (ThreadLocalRandom.current().nextDouble() * (maxCps - minCps));
        long delayNanos = (long) (1_000_000_000L / cps);
        nextActionAtNanos = System.nanoTime() + delayNanos;
    }

    private boolean shouldRun(MinecraftClient client, AutoCrystalConfig config) {
        if (!config.enabled || client == null || client.player == null || client.world == null) {
            return false;
        }
        if (client.currentScreen != null) {
            return false;
        }
        if (!client.options.useKey.isPressed()) {
            return false;
        }
        if (!client.player.getMainHandStack().isOf(Items.END_CRYSTAL)) {
            return false;
        }
        HitResult target = client.crosshairTarget;
        if (!(target instanceof BlockHitResult blockHit)) {
            return false;
        }
        Block block = client.world.getBlockState(blockHit.getBlockPos()).getBlock();
        return block == Blocks.OBSIDIAN || block == Blocks.BEDROCK;
    }

    private void performRightClick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (player == null || interactionManager == null) {
            return;
        }
        HitResult target = client.crosshairTarget;
        if (target instanceof BlockHitResult blockHit) {
            interactionManager.interactBlock(player, Hand.MAIN_HAND, blockHit);
            player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void performLeftClick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (player == null || interactionManager == null) {
            return;
        }
        HitResult target = client.crosshairTarget;
        if (target instanceof EntityHitResult entityHit) {
            interactionManager.attackEntity(player, entityHit.getEntity());
            player.swingHand(Hand.MAIN_HAND);
            return;
        }
        if (target instanceof BlockHitResult blockHit) {
            interactionManager.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
            player.swingHand(Hand.MAIN_HAND);
        }
    }

    private void reset() {
        active = false;
        pendingStartRights = 0;
        nextLeft = true;
        nextActionAtNanos = 0L;
    }
}
