package net.owen.bladebound.config;

public class BlackHoleConfig {

    // Master toggle
    public boolean enabled = true;

    // Duration in ticks (20 ticks = 1 second). 30s = 600 ticks.
    public int durationTicks = 20 * 30;

    // Mana
    public int manaCost = 350;

    // Radii (3-zone)
    public float coreRadius = 6.0f;
    public float midRadius  = 10.0f;
    public float pullRadius = 14.0f;

    // Pull + spiral feel
    public double pullStrength = 0.12;
    public double swirlStrength = 0.18;
    public boolean swirlClockwise = true;

    // Damage
    public float coreDps = 80.0f;
    public float midDps  = 10.0f;

    // Event Horizon
    public boolean eventHorizonEnabled = true;
    public int eventHorizonDurationTicks = 25;
    public int slownessAmplifier = 3; // Slowness IV
    public int weaknessAmplifier = 1; // Weakness II

    // Singularity collapse (end burst)
    public boolean collapseEnabled = true;
    public float collapseDamage = 10.0f;  // 5 hearts = 10.0
    public float collapseRadius = 6.0f;   // usually match coreRadius
    public boolean collapseHitCaster = false;

    // Terrain ripping
    public boolean ripBlocks = true;
    public int blocksPerRip = 5;
    public int ripIntervalTicks = 2;
    public double fallingBlockKick = 0.45;

    // Performance budgets
    public int maxTargetsPerTick = 90;
    public int maxFallingBlocksNearby = 140;
    public boolean deleteDebrisInCore = true;
    public boolean deleteItemsInCore = true;
}
