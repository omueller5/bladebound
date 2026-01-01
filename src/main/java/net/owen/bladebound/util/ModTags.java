package net.owen.bladebound.util;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class ModTags {
    private ModTags() {}

    public static final TagKey<Item> STAVES = TagKey.of(
            RegistryKeys.ITEM,
            Identifier.of("bladebound", "staves")
    );
}
