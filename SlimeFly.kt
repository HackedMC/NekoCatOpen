package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockSlime
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB

class SlimeFly : FlyMode("Slime") {
    private val hurtTimeValue = FloatValue("hurtTime", 2.5f, 1f, 5f)
    private var hurtTime: Float = 0f
    override fun onUpdate(event: UpdateEvent){
        if (BlockUtils.getBlock(mc.thePlayer.position.down()) is BlockSlime) {
            hurtTime = hurtTimeValue.get()
            if (hurtTime <= 0) {
                LiquidBounce.hud.addNotification(Notification(modeName, "´¥·¢Ê·À³Ä··½¿éFly", NotifyType.WARNING))
            }
        }
        if(hurtTime > 0f){
            hurtTime -= 0.05f
            if (hurtTime < 0f) hurtTime = 0f
        }
    }
    override fun onBlockBB(event: BlockBBEvent) {
        if (event.block is BlockAir && event.y <= mc.thePlayer.posY && hurtTime > 0f) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, mc.thePlayer.posY, event.z + 1.0)
        }
    }
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer && hurtTime > 0f) {
            packet.onGround = true
        }
    }
    override fun onDisable(){
        hurtTime = 0f
    }
    override fun onJump(event: JumpEvent) {
        if (hurtTime > 0f) event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        if (hurtTime > 0f) event.stepHeight = 0f
    }
}
