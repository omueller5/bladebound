package net.owen.bladebound.client.spell;

public final class ClientSpellState {
    private ClientSpellState() {}

    // default spell id
    private static String selectedSpellId = "fireball";

    public static String getSelectedSpellId() {
        return selectedSpellId;
    }

    public static void setSelectedSpellId(String id) {
        selectedSpellId = (id == null || id.isBlank()) ? "fireball" : id;
    }
}
