# ================================
# Clear learned spells (SAFE TAGS)
# ================================
tag @s remove bb_learn_bladebound__firebolt
tag @s remove bb_learn_bladebound__frost_ray
tag @s remove bb_learn_bladebound__heal
tag @s remove bb_learn_bladebound__stone_dart
tag @s remove bb_learn_bladebound__lightning_strike
tag @s remove bb_learn_bladebound__mana_barrier
tag @s remove bb_learn_bladebound__zoltraak
tag @s remove bb_learn_bladebound__perfect_heal
tag @s remove bb_learn_bladebound__world_rewrite

# ================================
# Clear selected spell
# ================================
tag @s remove bb_sel_bladebound__firebolt
tag @s remove bb_sel_bladebound__frost_ray
tag @s remove bb_sel_bladebound__heal
tag @s remove bb_sel_bladebound__stone_dart
tag @s remove bb_sel_bladebound__lightning_strike
tag @s remove bb_sel_bladebound__mana_barrier
tag @s remove bb_sel_bladebound__zoltraak
tag @s remove bb_sel_bladebound__perfect_heal
tag @s remove bb_sel_bladebound__world_rewrite

tellraw @s {"text":"Spell data reset.","color":"gray"}
