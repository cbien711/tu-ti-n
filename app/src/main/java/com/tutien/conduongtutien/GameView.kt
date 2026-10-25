package com.tutien.conduongtutien

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View

class GameView(context: Context, val player: Player) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentMonster: Monster = GameEngine.generateMonster(player)
    private var combatTimer = 0L
    private var currentTab = 0 
    private val logMessages = mutableListOf<String>("Chào mừng đạo hữu đến với Con Đường Tu Tiên!")

    init {
        GameEngine.recalculatePlayerStats(player)
    }

    fun update() {
        if (currentTab == 0 && player.autoBattle) {
            combatTimer += 30
            if (combatTimer >= 1000) {
                combatTimer = 0
                executeCombatStep()
            }
        }
        if (System.currentTimeMillis() % 2000L < 50L) {
            if (player.hp < player.maxHp) player.hp = (player.hp + player.regen).coerceAtMost(player.maxHp)
        }
        invalidate()
    }

    private fun executeCombatStep() {
        val isCrit = kotlin.math.Random.nextDouble() < player.critRate
        var dmg = player.atk * if (isCrit) player.critDmg else 1.0
        dmg = (dmg - currentMonster.def * 0.3).coerceAtLeast(1.0) + player.trueDmg
        
        currentMonster.hp -= dmg
        if (player.vamp > 0) {
            player.hp = (player.hp + dmg * player.vamp).coerceAtMost(player.maxHp)
        }

        if (currentMonster.hp <= 0) {
            player.tuvi += currentMonster.tuviReward
            player.gold += currentMonster.goldReward
            addLog("Đánh bại ${currentMonster.name}, nhận ${currentMonster.tuviReward} tu vi, ${currentMonster.goldReward} vàng!")

            if (kotlin.math.Random.nextDouble() < currentMonster.pillDropRate) {
                player.rebirthPills++
                addLog("🌟 Nhận được 1 Đan Dược Chuyển Sinh! (${player.rebirthPills}/50)")
            }

            if (kotlin.math.Random.nextDouble() < currentMonster.equipmentDropRate) {
                val eq = GameEngine.generateEquipment(player)
                player.inventory.add(eq)
                addLog("⚔️ Nhận được trang bị: ${eq.name} (${GameEngine.rarities[eq.rarity]})")
            }

            checkLevelUp()
            currentMonster = GameEngine.generateMonster(player)
        } else {
            if (kotlin.math.Random.nextDouble() > player.dodge) {
                val mDmg = (currentMonster.atk - player.def * 0.4).coerceAtLeast(1.0)
                player.hp -= mDmg
                if (player.hp <= 0) {
                    player.hp = 1.0
                    addLog("⚠️ Đạo hữu đã kiệt sức trước ${currentMonster.name}, tạm lui dưỡng thương!")
                }
            } else {
                addLog("💨 Đạo hữu né tránh thành công đòn tấn công!")
            }
        }
    }

    private fun checkLevelUp() {
        if (player.tuvi >= player.maxTuvi) {
            player.tuvi -= player.maxTuvi
            player.level++
            player.maxTuvi = (player.maxTuvi * 1.3).toLong()
            addLog("🎉 Chúc mừng đạo hữu đã tăng lên cấp ${player.level}!")
            
            player.subLayer++
            if (player.subLayer > 5) {
                player.subLayer = 0
                player.layer++
                if (player.layer > 8) {
                    player.layer = 0
                    player.realmIndex = (player.realmIndex + 1).coerceAtMost(GameEngine.realms.size - 1)
                    addLog("🔥 Đại đột phá! Đạo hữu bước vào cảnh giới: ${GameEngine.realms[player.realmIndex]}")
                }
            }
            GameEngine.recalculatePlayerStats(player)
        }
    }

    private fun addLog(msg: String) {
        if (logMessages.size > 15) logMessages.removeAt(0)
        logMessages.add(msg)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        paint.color = 0xFF0B0F19.toInt()
        canvas.drawRect(0f, 0f, w, h, paint)

        // Header
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRect(0f, 0f, w, 140f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 36f
        canvas.drawText("【 CON ĐƯỜNG TU TIÊN 】", 30f, 50f, paint)

        paint.textSize = 28f
        paint.color = 0xFF38BDF8.toInt()
        val realmStr = "${GameEngine.realms[player.realmIndex]} - Tầng ${player.layer + 1} (Tiểu cảnh ${player.subLayer + 1}/6)"
        canvas.drawText("Cảnh giới: $realmStr", 30f, 95f, paint)

        paint.color = 0xFF4ADE80.toInt()
        canvas.drawText("Vàng: ${player.gold} | Đan: ${player.rebirthPills}/50", 30f, 132f, paint)

        // HP & Tu vi bars
        paint.color = 0xFF334155.toInt()
        canvas.drawRect(30f, 155f, w - 30f, 185f, paint)
        paint.color = 0xFFEF4444.toInt()
        canvas.drawRect(30f, 155f, 30f + (w - 60f) * (player.hp / player.maxHp).coerceIn(0.0, 1.0).toFloat(), 185f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 22f
        canvas.drawText("HP: ${player.hp.toInt()}/${player.maxHp.toInt()}", 40f, 177f, paint)

        paint.color = 0xFF334155.toInt()
        canvas.drawRect(30f, 195f, w - 30f, 225f, paint)
        paint.color = 0xFF8B5CF6.toInt()
        canvas.drawRect(30f, 195f, 30f + (w - 60f) * (player.tuvi.toDouble() / player.maxTuvi.toDouble()).coerceIn(0.0, 1.0).toFloat(), 225f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("Tu Vi: ${player.tuvi}/${player.maxTuvi}", 40f, 217f, paint)

        when (currentTab) {
            0 -> drawCombatScreen(canvas, w, h)
            1 -> drawCharacterScreen(canvas, w, h)
            2 -> drawEquipmentScreen(canvas, w, h)
            3 -> drawInventoryScreen(canvas, w, h)
            4 -> drawUpgradeScreen(canvas, w, h)
            5 -> drawRebirthScreen(canvas, w, h)
        }

        drawBottomNav(canvas, w, h)
    }

    private fun drawCombatScreen(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(RectF(30f, 250f, w - 30f, 480f), 20f, 20f, paint)
        paint.color = if (currentMonster.isBoss) 0xFFEF4444.toInt() else 0xFFF87171.toInt()
        paint.textSize = 34f
        canvas.drawText(currentMonster.name, 60f, 300f, paint)
        paint.textSize = 24f
        paint.color = 0xFF94A3B8.toInt()
        canvas.drawText("Cấp độ: ${currentMonster.level} | Sao: ${"★".repeat(currentMonster.stars)}", 60f, 340f, paint)

        paint.color = 0xFF334155.toInt()
        canvas.drawRect(60f, 370f, w - 60f, 405f, paint)
        paint.color = 0xFFDC2626.toInt()
        canvas.drawRect(60f, 370f, 60f + (w - 120f) * (currentMonster.hp / currentMonster.maxHp).coerceIn(0.0, 1.0).toFloat(), 405f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 22f
        canvas.drawText("HP Quái: ${currentMonster.hp.toInt()}/${currentMonster.maxHp.toInt()}", 70f, 396f, paint)

        paint.color = 0xFF0F172A.toInt()
        canvas.drawRoundRect(RectF(30f, 500f, w - 30f, h - 150f), 20f, 20f, paint)
        paint.color = 0xFF38BDF8.toInt()
        paint.textSize = 22f
        var startY = 540f
        for (log in logMessages.takeLast(7)) {
            canvas.drawText(log, 50f, startY, paint)
            startY += 35f
        }
    }

    private fun drawCharacterScreen(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(RectF(30f, 250f, w - 30f, h - 150f), 20f, 20f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 30f
        canvas.drawText("📊 THÔNG TIN NHÂN VẬT", 60f, 300f, paint)

        paint.textSize = 24f
        paint.color = 0xFFCBD5E1.toInt()
        val stats = listOf(
            "Cấp độ: ${player.level} | Chuyển sinh: ${player.rebirthCount}",
            "Tấn công vật lý: ${player.atk.toInt()}",
            "Tấn công phép: ${player.matk.toInt()}",
            "Phòng thủ (DEF): ${player.def.toInt()}",
            "Tỷ lệ chí mạng: ${(player.critRate * 100).toInt()}%",
            "Sát thương chí mạng: ${(player.critDmg * 100).toInt()}%",
            "Né tránh: ${(player.dodge * 100).toInt()}%",
            "Hút máu: ${(player.vamp * 100).toInt()}%",
            "Hồi HP mỗi giây: ${player.regen.toInt()}"
        )
        var y = 350f
        for (st in stats) { canvas.drawText(st, 60f, y, paint); y += 40f }
    }

    private fun drawEquipmentScreen(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(RectF(30f, 250f, w - 30f, h - 150f), 20f, 20f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 30f
        canvas.drawText("🛡️ TRANG BỊ ĐANG MẶC (8 Ô)", 60f, 300f, paint)

        val slots = listOf("Vũ Khí", "Mũ", "Áo Giáp", "Găng Tay", "Giày", "Nhẫn", "Dây Chuyền", "Pháp Bảo")
        var y = 360f
        for (i in slots.indices) {
            val eq = player.equippedItems[i]
            paint.color = if (eq != null) GameEngine.rarityColors[eq.rarity] else 0xFF64748B.toInt()
            canvas.drawText("${slots[i]}: ${eq?.name ?: "Trống"}", 60f, y, paint)
            y += 45f
        }
    }

    private fun drawInventoryScreen(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(RectF(30f, 250f, w - 30f, h - 150f), 20f, 20f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 30f
        canvas.drawText("🎒 TÚI ĐỒ (${player.inventory.size} vật phẩm)", 60f, 300f, paint)

        paint.textSize = 22f
        var y = 350f
        for ((idx, eq) in player.inventory.withIndex().take(8)) {
            paint.color = GameEngine.rarityColors[eq.rarity]
            canvas.drawText("${idx + 1}. ${eq.name} (Bán: ${eq.sellPrice}v)", 60f, y, paint)
            y += 45f
        }
        if (player.inventory.isEmpty()) {
            paint.color = 0xFF94A3B8.toInt()
            canvas.drawText("Túi đồ trống, hãy đánh quái để nhận trang bị!", 60f, 370f, paint)
        }
    }

    private fun drawUpgradeScreen(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(RectF(30f, 250f, w - 30f, h - 150f), 20f, 20f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 30f
        canvas.drawText("💰 NÂNG CẤP CHỈ SỐ BẰNG VÀNG", 60f, 300f, paint)

        val upgrades = listOf(
            "1. Tấn Công (Cấp ${player.upAtkLvl}) - Giá: ${100L * (player.upAtkLvl + 1)}v",
            "2. Phòng Thủ (Cấp ${player.upDefLvl}) - Giá: ${100L * (player.upDefLvl + 1)}v",
            "3. HP Tối Đa (Cấp ${player.upHpLvl}) - Giá: ${150L * (player.upHpLvl + 1)}v"
        )
        paint.textSize = 22f
        paint.color = 0xFF4ADE80.toInt()
        var y = 370f
        for (up in upgrades) { canvas.drawText(up, 60f, y, paint); y += 50f }
        paint.color = 0xFFFCD34D.toInt()
        canvas.drawText("💡 Chạm vào đây để nâng cấp Tấn công!", 60f, y + 20f, paint)
    }

    private fun drawRebirthScreen(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(RectF(30f, 250f, w - 30f, h - 150f), 20f, 20f, paint)
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 30f
        canvas.drawText("⚡ CHUYỂN SINH TU TIÊN", 60f, 300f, paint)

        paint.textSize = 24f
        paint.color = 0xFFCBD5E1.toInt()
        canvas.drawText("Yêu cầu: 50 Đan Dược Chuyển Sinh", 60f, 360f, paint)
        canvas.drawText("Hiện có: ${player.rebirthPills}/50 viên", 60f, 410f, paint)

        paint.color = if (player.rebirthPills >= 50) 0xFF4ADE80.toInt() else 0xFFEF4444.toInt()
        canvas.drawText(if (player.rebirthPills >= 50) "🔥 Đủ điều kiện Chuyển Sinh!" else "⏳ Chưa đủ đan dược", 60f, 470f, paint)
    }

    private fun drawBottomNav(canvas: Canvas, w: Float, h: Float) {
        paint.color = 0xFF0F172A.toInt()
        canvas.drawRect(0f, h - 120f, w, h, paint)

        val tabTitles = listOf("Chiến Đấu", "Nhân Vật", "Trang Bị", "Túi Đồ", "Nâng Cấp", "Chuyển Sinh")
        val cellW = w / tabTitles.size
        for (i in tabTitles.indices) {
            paint.color = if (currentTab == i) 0xFF38BDF8.toInt() else 0xFF94A3B8.toInt()
            paint.textSize = 20f
            canvas.drawText(tabTitles[i], i * cellW + 15f, h - 60f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val w = width.toFloat()
            val h = height.toFloat()
            if (event.y > h - 120f) {
                val tabTitles = listOf("Chiến Đấu", "Nhân Vật", "Trang Bị", "Túi Đồ", "Nâng Cấp", "Chuyển Sinh")
                val cellW = w / tabTitles.size
                val clickedTab = (event.x / cellW).toInt()
                if (clickedTab in tabTitles.indices) currentTab = clickedTab
            } else if (currentTab == 4 && event.y in 350f..450f) {
                val cost = 100L * (player.upAtkLvl + 1)
                if (player.gold >= cost) {
                    player.gold -= cost
                    player.upAtkLvl++
                    GameEngine.recalculatePlayerStats(player)
                    addLog("📈 Nâng cấp Tấn Công thành công lên cấp ${player.upAtkLvl}!")
                } else {
                    addLog("❌ Không đủ vàng để nâng cấp!")
                }
            } else if (currentTab == 5 && event.y in 440f..500f) {
                if (player.rebirthPills >= 50) {
                    player.rebirthPills -= 50
                    player.rebirthCount++
                    player.level = 1
                    player.realmIndex = 0
                    player.layer = 0
                    player.subLayer = 0
                    player.tuvi = 0
                    player.maxTuvi = 100
                    player.gold = 500
                    GameEngine.recalculatePlayerStats(player)
                    addLog("🌟 CHUYỂN SINH THÀNH CÔNG!")
                } else {
                    addLog("❌ Cần đủ 50 Đan Dược Chuyển Sinh!")
                }
            }
            invalidate()
            return true
        }
        return false
    }
}
