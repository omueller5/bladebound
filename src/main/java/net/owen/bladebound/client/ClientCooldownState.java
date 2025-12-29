package net.owen.bladebound.client;

public final class ClientCooldownState {

    private ClientCooldownState() {}

    // Client player "age" tick when cooldown ends
    private static int cooldownEndAge = 0;

    public static void startCooldown(int clientPlayerAge, int cooldownTicks) {
        cooldownEndAge = clientPlayerAge + Math.max(0, cooldownTicks);
    }

    public static int getTicksLeft(int clientPlayerAge) {
        return Math.max(0, cooldownEndAge - clientPlayerAge);
    }

    public static boolean isCoolingDown(int clientPlayerAge) {
        return getTicksLeft(clientPlayerAge) > 0;
    }
}
