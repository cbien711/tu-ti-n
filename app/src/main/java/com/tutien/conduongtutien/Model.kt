package com.tutien.conduongtutien

data class Player(
    var level: Int = 1,
    var realmIndex: Int = 0, // 0: Luyện Khí -> 11: Tiên Đế
    var layer: Int = 0,      // 0..8 (9 tầng)
    var subLayer: Int = 0,   // 0..5 (6 tiểu cảnh)
    var tuvi: Long = 0,
    var maxTuvi: Long = 100,
    var gold: Long = 1000,
    var rebirthPills: Int = 0,
    var rebirthPoints: Int = 0,
    var rebirthCount: Int = 0,
    
    // Combat stats
    var hp: Double = 100.0,
    var maxHp: Double = 100.0,
    var mana: Double = 50.0,
    var maxMana: Double = 50.0,
    var atk: Double = 15.0,
    var matk: Double = 10.0,
    var def: Double = 5.0,
    var critDmg: Double = 1.5,
    var critRate: Double = 0.05,
    var regen: Double = 2.0,
    var dodge: Double = 0.02,
    var trueDmg: Double = 0.0,
    var vamp: Double = 0.0,
    var effectRes: Double = 0.05,
    var atkSpeed: Double = 1.0,

    // Gold upgrade levels
    var upAtkLvl: Int = 0,
    var upMatkLvl: Int = 0,
    var upDefLvl: Int = 0,
    var upHpLvl: Int = 0,
    var upManaLvl: Int = 0,
    var upCritLvl: Int = 0,
    var upCritDmgLvl: Int = 0,
    var upRegenLvl: Int = 0,

    // Equipment 8 slots
    val equippedItems: MutableMap<Int, Equipment?> = mutableMapOf(),
    val inventory: MutableList<Equipment> = mutableListOf(),
    
    // Skill Tree
    val skillLevels: MutableMap<String, Int> = mutableMapOf(),
    
    var autoBattle: Boolean = true,
    var soundEnabled: Boolean = true
)

data class Equipment(
    val id: String,
    val name: String,
    val rarity: Int, // 0..8
    val slot: Int,   // 0..7
    val levelReq: Int,
    val statBonuses: Map<String, Double>,
    val sellPrice: Long
)

data class Monster(
    val name: String,
    val level: Int,
    val stars: Int,
    var hp: Double,
    val maxHp: Double,
    val atk: Double,
    val def: Double,
    val atkSpeed: Double,
    val tuviReward: Long,
    val goldReward: Long,
    val pillDropRate: Double,
    val equipmentDropRate: Double,
    val isBoss: Boolean = false
)
