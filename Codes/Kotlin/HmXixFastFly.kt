package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import kotlin.math.cos
import kotlin.math.sin

class HmXixFastFly : FlyMode("HmXixFast") {
    private var speedValue = FloatValue("${valuePrefix}Speed", 2f, 1f, 20f)
    private var vSpeedValue = FloatValue("${valuePrefix}Vertical", 2f, 1f, 20f)
    private var motionTicks = IntegerValue("${valuePrefix}motionTicks", 1, 1, 1000)
    private var startY: Double = 0.0
    private var jumpTicks = 0

    @EventTarget
    override fun onEnable() {
        jumpTicks = motionTicks.get()
        startY = mc.thePlayer.posY
    }

    @EventTarget
    override fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.isSneaking) {
            mc.thePlayer.motionY = (0-vSpeedValue.get()).toDouble()
        } else if (mc.gameSettings.keyBindJump.isKeyDown) {
            mc.thePlayer.motionY = vSpeedValue.get().toDouble()
        } else {
            mc.thePlayer.motionY = -0.01
        }
        if (jumpTicks > 0) {
            mc.thePlayer.motionY = 0.2
            jumpTicks--
        }
    }

    @EventTarget
    override fun onMove(event: MoveEvent) {
        val speed = speedValue.get()
        var forward = mc.thePlayer.movementInput.moveForward.toDouble()
        var strafe = mc.thePlayer.movementInput.moveStrafe.toDouble()
        var yaw = mc.thePlayer.rotationYaw
        if ((forward == 0.0) && strafe == 0.0) {
            event.setX(0.0)
            event.setZ(0.0)
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (if (forward > 0.0) -45 else 45).toFloat()
                } else if (strafe < 0.0) {
                    yaw += (if (forward > 0.0) 45 else -45).toFloat()
                }
                strafe = 0.0
                if (forward > 0.0) {
                    forward = 1.0
                } else if (forward < 0.0) {
                    forward = -1.0
                }
            }
            event.setX(
                forward * speed * cos(Math.toRadians((yaw + 90.0f).toDouble()))
                        + strafe * speed * sin(Math.toRadians((yaw + 90.0f).toDouble()))
            )
            event.setZ(
                (forward * speed * sin(Math.toRadians((yaw + 90.0f).toDouble()))
                        - strafe * speed * cos(Math.toRadians((yaw + 90.0f).toDouble())))
            )
        }
    }
}