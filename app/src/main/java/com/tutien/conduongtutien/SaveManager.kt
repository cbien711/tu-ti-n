package com.tutien.conduongtutien

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SaveManager {
    private const val PREF_NAME = "ConDuongTuTienSave"
    private const val KEY_SAVE_DATA = "save_data"

    fun saveGame(context: Context, player: Player) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("level", player.level)
            put("realmIndex", player.realmIndex)
            put("layer", player.layer)
            put("subLayer", player.subLayer)
            put("tuvi", player.tuvi)
            put("maxTuvi", player.maxTuvi)
            put("gold", player.gold)
            put("rebirthPills", player.rebirthPills)
            put("rebirthPoints", player.rebirthPoints)
            put("rebirthCount", player.rebirthCount)
            
            put("hp", player.hp)
            put("maxHp", player.maxHp)
            put("mana", player.mana)
            put("maxMana", player.maxMana)
            put("atk", player.atk)
            put("matk", player.matk)
            put("def", player.def)
            put("critDmg", player.critDmg)
            put("critRate", player.critRate)
            put("regen", player.regen)
            put("dodge", player.dodge)
            put("trueDmg", player.trueDmg)
            put("vamp", player.vamp)
            put("effectRes", player.effectRes)
            put("atkSpeed", player.atkSpeed)

            put("upAtkLvl", player.upAtkLvl)
            put("upMatkLvl", player.upMatkLvl)
            put("upDefLvl", player.upDefLvl)
            put("upHpLvl", player.upHpLvl)
            put("upManaLvl", player.upManaLvl)
            put("upCritLvl", player.upCritLvl)
            put("upCritDmgLvl", player.upCritDmgLvl)
            put("upRegenLvl", player.upRegenLvl)

            val invArray = JSONArray()
            for (eq in player.inventory) {
                invArray.put(eqToJson(eq))
            }
            put("inventory", invArray)

            val eqObj = JSONObject()
            for ((slot, eq) in player.equippedItems) {
                if (eq != null) {
                    eqObj.put(slot.toString(), eqToJson(eq))
                }
            }
            put("equipped", eqObj)
        }
        prefs.edit().putString(KEY_SAVE_DATA, json.toString()).apply()
    }

    fun loadGame(context: Context): Player {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val dataStr = prefs.getString(KEY_SAVE_DATA, null) ?: return Player()
        return try {
            val json = JSONObject(dataStr)
            val player = Player().apply {
                level = json.optInt("level", 1)
                realmIndex = json.optInt("realmIndex", 0)
                layer = json.optInt("layer", 0)
                subLayer = json.optInt("subLayer", 0)
                tuvi = json.optLong("tuvi", 0)
                maxTuvi = json.optLong("maxTuvi", 100)
                gold = json.optLong("gold", 1000)
                rebirthPills = json.optInt("rebirthPills", 0)
                rebirthPoints = json.optInt("rebirthPoints", 0)
                rebirthCount = json.optInt("rebirthCount", 0)

                hp = json.optDouble("hp", 100.0)
                maxHp = json.optDouble("maxHp", 100.0)
                mana = json.optDouble("mana", 50.0)
                maxMana = json.optDouble("maxMana", 50.0)
                atk = json.optDouble("atk", 15.0)
                matk = json.optDouble("matk", 10.0)
                def = json.optDouble("def", 5.0)
                critDmg = json.optDouble("critDmg", 1.5)
                critRate = json.optDouble("critRate", 0.05)
                regen = json.optDouble("regen", 2.0)
                dodge = json.optDouble("dodge", 0.02)
                trueDmg = json.optDouble("trueDmg", 0.0)
                vamp = json.optDouble("vamp", 0.0)
                effectRes = json.optDouble("effectRes", 0.05)
                atkSpeed = json.optDouble("atkSpeed", 1.0)

                upAtkLvl = json.optInt("upAtkLvl", 0)
                upMatkLvl = json.optInt("upMatkLvl", 0)
                upDefLvl = json.optInt("upDefLvl", 0)
                upHpLvl = json.optInt("upHpLvl", 0)
                upManaLvl = json.optInt("upManaLvl", 0)
                upCritLvl = json.optInt("upCritLvl", 0)
                upCritDmgLvl = json.optInt("upCritDmgLvl", 0)
                upRegenLvl = json.optInt("upRegenLvl", 0)
            }

            val invArray = json.optJSONArray("inventory")
            if (invArray != null) {
                for (i in 0 until invArray.length()) {
                    player.inventory.add(jsonToEq(invArray.getJSONObject(i)))
                }
            }

            val eqObj = json.optJSONObject("equipped")
            if (eqObj != null) {
                val keys = eqObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    player.equippedItems[key.toInt()] = jsonToEq(eqObj.getJSONObject(key))
                }
            }
            player
        } catch (e: Exception) {
            Player()
        }
    }

    private fun eqToJson(eq: Equipment): JSONObject {
        return JSONObject().apply {
            put("id", eq.id)
            put("name", eq.name)
            put("rarity", eq.rarity)
            put("slot", eq.slot)
            put("levelReq", eq.levelReq)
            put("sellPrice", eq.sellPrice)
            val statsObj = JSONObject()
            for ((k, v) in eq.statBonuses) { statsObj.put(k, v) }
            put("stats", statsObj)
        }
    }

    private fun jsonToEq(json: JSONObject): Equipment {
        val statsMap = mutableMapOf<String, Double>()
        val statsObj = json.optJSONObject("stats")
        if (statsObj != null) {
            val keys = statsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                statsMap[k] = statsObj.getDouble(k)
            }
        }
        return Equipment(
            id = json.optString("id", "eq_1"),
            name = json.optString("name", "Phàm Thiết Kiếm"),
            rarity = json.optInt("rarity", 0),
            slot = json.optInt("slot", 0),
            levelReq = json.optInt("levelReq", 1),
            statBonuses = statsMap,
            sellPrice = json.optLong("sellPrice", 100)
        )
    }
}
