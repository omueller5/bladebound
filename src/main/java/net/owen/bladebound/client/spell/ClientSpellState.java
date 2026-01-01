package net.owen.bladebound.client.spell;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientSpellState {
    private ClientSpellState() {}

    // default selected spell (stable id)
    private static Identifier selectedSpellId = Identifier.of("bladebound", "firebolt");

    // learned spell ids (client-side cache)
    private static final Set<Identifier> learned = new HashSet<>();

    public static Identifier getSelectedSpellId() {
        return selectedSpellId;
    }

    public static void setSelectedSpellId(Identifier id) {
        selectedSpellId = (id == null) ? Identifier.of("bladebound", "firebolt") : id;
    }

    public static boolean hasLearned(Identifier id) {
        return id != null && learned.contains(id);
    }

    public static Set<Identifier> getLearnedView() {
        return Collections.unmodifiableSet(learned);
    }

    /** Called from SpellStateSyncPayload receiver. */
    public static void set(List<Identifier> learnedIds, Identifier selectedId) {
        learned.clear();
        if (learnedIds != null) {
            learned.addAll(learnedIds);
        }

        // If selected isn't learned, fall back to firebolt (or first learned if you prefer)
        if (selectedId != null && learned.contains(selectedId)) {
            selectedSpellId = selectedId;
        } else {
            Identifier fallback = Identifier.of("bladebound", "firebolt");
            selectedSpellId = learned.contains(fallback) ? fallback : (learned.isEmpty() ? fallback : learned.iterator().next());
        }
    }
}
