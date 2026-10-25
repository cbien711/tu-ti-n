package com.tutien.conduongtutien

import kotlin.random.Random

object GameEngine {
    val realms = listOf(
        "Luyện Khí", "Trúc Cơ", "Kim Đan", "Nguyên Anh", 
        "Hóa Thần", "Luyện Hư", "Hợp Thể", "Đại Thừa", 
        "Độ Kiếp", "Chân Tiên", "Tiên Vương", "Tiên Đế"
    )

    val rarities = listOf(
        "Phàm phẩm", "Tinh phẩm", "Huyền phẩm", "Địa phẩm", 
        "Thiên phẩm", "Linh phẩm", "Thánh phẩm", "Tiên phẩm", "Thần phẩm"
    )

    val rarityColors = listOf(
        0xFF9E9E9E.toInt(), 0xFF4CAF50.toInt(), 0xFF2196F3.toInt(),
        0xFF9C27B0.toInt(), 0xFFFF9800.toInt(), 0xFFE91E63.toInt(),
        0xFFFFEB3B.toInt(), 0xFF00BCD4.toInt(), 0xFFFF5722.toInt()
    )

    fun generateMonster(player: Player): Monster {
        val baseLevel = player.realmIndex * 54 + player.layer * 6 + player.subLayer + 1
        val isBoss = Random.nextDouble() < 0.15
        val namePrefix = if (isBoss) "Yêu Vương " else "Yêu Thú "
        val name = namePrefix + realms[player.realmIndex] + " Tầng ${player.layer + 1}"
        
        val mult = if (isBoss) 3.5 else 1.0
        val hp = (50.0 + baseLevel * 25.0) * mult
        val atk = (10.0 + baseLevel * 5.0) * mult
        val def = (3.0 + baseLevel * 2.0) * mult
        
        return Monster(
            name = name, level = baseLevel, stars = (1..5).random(),
            hp = hp, maxHp = hp, atk = atk, def = def, atkSpeed = 1.0,
            tuviReward = (20L * baseLevel * if(isBoss) 5 else 1),
            goldReward = (15L * baseLevel * if(isBoss) 4 else 1),
            pillDropRate = if (isBoss) 0.8 else 0.15,
            equipmentDropRate = if (isBoss) 0.6 else 0.2,
            isBoss = isBoss
        )
    }

    fun generateEquipment(player: Player): Equipment {
        val rarity = if (Random.nextDouble() < 0.05) 8 else Random.nextInt(0, 8)
        val slot = Random.nextInt(0, 8)
        val slotNames = listOf("Vũ Khí", "Mũ", "Áo Giáp", "Găng Tay", "Giày", "Nhẫn", "Dây Chuyền", "Pháp Bảo")
        val name = "${rarities[rarity]} ${slotNames[slot]}"
        
        val baseStat = (player.realmIndex + 1) * 10.0 * (rarity + 1)
        val stats = mutableMapOf<String, Double>()
        when (slot) {
            0 -> stats["atk"] = baseStat * 1.5
            1 -> stats["hp"] = baseStat * 20.0
            2 -> stats["def"] = baseStat * 1.2
            3 -> stats["critRate"] = 0.02 * (rarity + 1)
            4 -> stats["dodge"] = 0.01 * (rarity + 1)
            5 -> stats["critDmg"] = 0.1 * (rarity + 1)
            6 -> stats["mana"] = baseStat * 10.0
            7 -> stats["matk"] = baseStat * 1.4
        }
        
        return Equipment(
            id = "eq_${System.currentTimeMillis()}_${Random.nextInt(1000)}",
            name = name, rarity = rarity, slot = slot, levelReq = player.level,
            statBonuses = stats, sellPrice = (100L * (rarity + 1) * (player.realmIndex + 1))
        )
    }

    fun recalculatePlayerStats(player: Player) {
        val realmMultiplier = 1.0 + player.realmIndex * 0.8
        val baseAtk = 15.0 + player.level * 3.0 * realmMultiplier + player.upAtkLvl * 5.0
        val baseMatk = 10.0 + player.level * 2.0 * realmMultiplier + player.upMatkLvl * 4.0
        val baseDef = 5.0 + player.level * 1.5 * realmMultiplier + player.upDefLvl * 3.0
        val baseHp = 100.0 + player.level * 30.0 * realmMultiplier + player.upHpLvl * 50.0
        val baseMana = 50.0 + player.level * 15.0 * realmMultiplier + player.upManaLvl * 25.0

        var eqAtk = 0.0; var eqMatk = 0.0; var eqDef = 0.0; var eqHp = 0.0; var eqMana = 0.0
        var eqCritRate = 0.0; var eqCritDmg = 0.0; var eqDodge = 0.0; var eqTrueDmg = 0.0; var eqVamp = 0.0

        for ((_, eq) in player.equippedItems) {
            if (eq != null) {
                eqAtk += eq.statBonuses["atk"] ?: 0.0
                eqMatk += eq.statBonuses["matk"] ?: 0.0
                eqDef += eq.statBonuses["def"] ?: 0.0
                eqHp += eq.statBonuses["hp"] ?: 0.0
                eqMana += eq.statBonuses["mana"] ?: 0.0
                eqCritRate += eq.statBonuses["critRate"] ?: 0.0
                eqCritDmg += eq.statBonuses["critDmg"] ?: 0.0
                eqDodge += eq.statBonuses["dodge"] ?: 0.0
                eqTrueDmg += eq.statBonuses["trueDmg"] ?: 0.0
                eqVamp += eq.statBonuses["vamp"] ?: 0.0
            }
        }

        player.maxHp = baseHp + eqHp
        player.maxMana = baseMana + eqMana
        player.atk = baseAtk + eqAtk
        player.matk = baseMatk + eqMatk
        player.def = baseDef + eqDef
        player.critRate = (0.05 + player.upCritLvl * 0.01 + eqCritRate).coerceAtMost(0.95)
        player.critDmg = 1.5 + player.upCritDmgLvl * 0.05 + eqCritDmg
        player.dodge = (0.02 + eqDodge).coerceAtMost(0.60)
        player.trueDmg = eqTrueDmg
        player.vamp = eqVamp
        player.regen = 2.0 + player.upRegenLvl * 0.5

        if (player.hp > player.maxHp) player.hp = player.maxHp
        if (player.mana > player.maxMana) player.mana = player.maxMana
    }
}
