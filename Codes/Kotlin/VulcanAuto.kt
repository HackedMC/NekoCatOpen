package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.vulcan

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.entity.Entity
import net.minecraft.potion.Potion
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VulcanAuto : SpeedMode("VulcanAuto") {
    var Mode = "Fast"
    private var level = 1
    private var moveSpeed = 0.2873
    private var lastDist = 0.0
    private var timerDelay = 0
    private var Ticks = 0
    private var HurtTime = 0
    private var airMove = 0
    private val HurtTimeValue = IntegerValue("HurtTime", 2, 2, 10)
    private fun getEntity(interactEntity: Entity): Boolean {
        val aura = LiquidBounce.moduleManager[KillAura::class.java] as KillAura
        if(aura.state && aura.target != null){
            HurtTime = HurtTimeValue.get()
        }else if(Ticks >= 20 && HurtTime > 0){
            Ticks = 0
            HurtTime--
        }
        if(HurtTime <= 0){
            Ticks = 0
        }else{
            Ticks++
        }
        return (aura.state && aura.target != null) || HurtTime > 0

    }
    override fun onEnable() {
        if(Mode == "BHop"){
            mc.timer.timerSpeed = 1f
            level = if (mc.theWorld.getCollidingBoundingBoxes(
                    mc.thePlayer,
                    mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                ).size > 0 || mc.thePlayer.isCollidedVertically
            ) 1 else 4
        }
    }
    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        moveSpeed = baseMoveSpeed
        level = 0
        Ticks = 0
        HurtTime = 0
        Mode = "Fast"
    }

    override fun onMotion() {}

    override fun onUpdate() {
        if(!getEntity(mc.thePlayer)){
            if(Mode == "BHop" && mc.thePlayer.onGround){
                LiquidBounce.hud.addNotification(Notification("Switch Fast Mode.", Notification.Type.WARNING))
                Mode = "Fast"
            }
        }
        else{
            if(Mode == "Fast" && getEntity(mc.thePlayer) && mc.thePlayer.onGround){
                LiquidBounce.hud.addNotification(Notification("Switch BHop Mode.", Notification.Type.SUCCESS))
                Mode = "BHop"
            }
        }

        if (Mode == "Fast") {
            mc.timer.timerSpeed = 1.05f
            if (airMove >= 2){
                mc.thePlayer.jumpMovementFactor = 0.0230f
                if(airMove >= 13 && airMove % 8 == 0){
                    mc.thePlayer.motionY = -0.32 - 0.004 * Math.random()
                    mc.thePlayer.jumpMovementFactor = 0.0260f
                }
            }
            airMove++;
            if (abs(mc.thePlayer.movementInput.moveStrafe) < 0.1f) {
                mc.thePlayer.jumpMovementFactor = 0.0265f
            }else {
                mc.thePlayer.jumpMovementFactor = 0.0244f
            }
            if (!mc.thePlayer.onGround) {
                mc.gameSettings.keyBindJump.pressed = mc.gameSettings.keyBindJump.isKeyDown
            }
            if (MovementUtils.getSpeed() < 0.215f) {
                MovementUtils.strafe(0.215f)
            }
            if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.thePlayer.jump()
                MovementUtils.strafe()
                if(MovementUtils.getSpeed() < 0.5f) {
                    MovementUtils.strafe(0.4849f)
                }
            }else if (!MovementUtils.isMoving()) {
                mc.timer.timerSpeed = 1.00f
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }
        }
    }

    override fun onPreMotion() {
        if(Mode == "BHop"){
            val xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX
            val zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
            lastDist = sqrt(xDist * xDist + zDist * zDist)
        }
    }

    override fun onMove(event: MoveEvent) {
        if(Mode == "BHop"){
            ++timerDelay
            timerDelay %= 5

            if (timerDelay != 0) {
                mc.timer.timerSpeed = 1f
            } else {
                if (MovementUtils.isMoving()) mc.timer.timerSpeed = 32767f
                if (MovementUtils.isMoving()) {
                    mc.timer.timerSpeed = 1.3f
                    mc.thePlayer.motionX *= 1.0199999809265137
                    mc.thePlayer.motionZ *= 1.0199999809265137
                }
            }

            if (mc.thePlayer.onGround && MovementUtils.isMoving()) level = 2

            if (round(mc.thePlayer.posY - mc.thePlayer.posY.toInt().toDouble()) == round(0.138)) {
                val thePlayer = mc.thePlayer
                thePlayer.motionY -= 0.08
                event.y = event.y - 0.09316090325960147
                thePlayer.posY -= 0.09316090325960147
            }

            when {
                (level == 1 && (mc.thePlayer.moveForward != 0.0f || mc.thePlayer.moveStrafing != 0.0f)) -> {
                    level = 2
                    moveSpeed = 1.35 * baseMoveSpeed - 0.01
                }

                level == 2 -> {
                    level = 3
                    mc.thePlayer.motionY = 0.399399995803833
                    event.y = 0.399399995803833
                    moveSpeed *= 2.149
                }

                level == 3 -> {
                    level = 4
                    val difference = 0.66 * (lastDist - baseMoveSpeed)
                    moveSpeed = lastDist - difference
                }

                else -> {
                    if (mc.theWorld.getCollidingBoundingBoxes(
                            mc.thePlayer,
                            mc.thePlayer.entityBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                        ).size > 0 || mc.thePlayer.isCollidedVertically
                    ) level = 1
                    moveSpeed = lastDist - lastDist / 159.0
                }
            }

            moveSpeed = moveSpeed.coerceAtLeast(baseMoveSpeed)
            val movementInput = mc.thePlayer.movementInput
            var forward = movementInput.moveForward
            var strafe = movementInput.moveStrafe
            var yaw = mc.thePlayer.rotationYaw

            if (forward == 0.0f && strafe == 0.0f) {
                event.x = 0.0
                event.z = 0.0
            } else if (forward != 0.0f) {
                if (strafe >= 1.0f) {
                    yaw += (if (forward > 0.0f) -45 else 45).toFloat()
                    strafe = 0.0f
                } else if (strafe <= -1.0f) {
                    yaw += (if (forward > 0.0f) 45 else -45).toFloat()
                    strafe = 0.0f
                }
                if (forward > 0.0f) {
                    forward = 1.0f
                } else if (forward < 0.0f) {
                    forward = -1.0f
                }
            }

            val mx2 = cos(Math.toRadians((yaw + 90.0f).toDouble()))
            val mz2 = sin(Math.toRadians((yaw + 90.0f).toDouble()))
            event.x = forward.toDouble() * moveSpeed * mx2 + strafe.toDouble() * moveSpeed * mz2
            event.z = forward.toDouble() * moveSpeed * mz2 - strafe.toDouble() * moveSpeed * mx2
            mc.thePlayer.stepHeight = 0.6f
            if (forward == 0.0f && strafe == 0.0f) {
                event.x = 0.0
                event.z = 0.0
            }
        }
    }

    private val baseMoveSpeed: Double
        get() {
            var baseSpeed = 0.2873
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(
                Potion.moveSpeed
            ).amplifier + 1)
            return baseSpeed
        }

    private fun round(value: Double): Double {
        var bigDecimal = BigDecimal(value)
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP)
        return bigDecimal.toDouble()
    }
}
