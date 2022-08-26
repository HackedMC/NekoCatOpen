package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue

class HypixelLowHop : SpeedMode("HypixelLowHop") {

    private val timerValue = BoolValue("Timer Boost", true)
    private val timerSpeedValue = FloatValue("Timer", 1f, 0.1f, 2.0f)
    private var runspeed = 0f

    override fun onPreMotion() {
        if (timerValue.get()) {
            mc.timer.timerSpeed = (timerSpeedValue.get() as Number).toFloat()
        }
        if (MovementUtils.isMoving() && mc.thePlayer.onGround) {
            runspeed = 1.2f
            mc.thePlayer.motionY = 0.31999998688697817
        }
        MovementUtils.strafe((MovementUtils.getBaseMoveSpeed() * 0.90151 * runspeed.toDouble()).toFloat())
        if (runspeed.toDouble() > 1.0f) {
            runspeed -= 0.05f
        }
    }
}