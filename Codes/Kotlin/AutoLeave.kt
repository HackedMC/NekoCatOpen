package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import java.util.*

@ModuleInfo(name = "AutoLeave", spacedName = "Auto Leave", description = "Automatically makes you leave the server whenever your health is low.", category = ModuleCategory.COMBAT)
class AutoLeave : Module() {
    private val healthValue = FloatValue("Health", 8f, 0f, 20f)
    private val modeValue = ListValue("Mode", arrayOf("Quit", "HytPit", "InvalidPacket", "SelfHurt", "IllegalChat", "Custom"), "HytPit")
    private val keepArmorValue = BoolValue("Keep-Armor", true)
    private val declarationValue = BoolValue("Declaration", true)
    private val customTextValue = TextValue("Custom-Text", "/hub", {modeValue.get().equals("Custom")})
    private val autoDisableValue = BoolValue("Auto-Disable", false)
    private val debugValue = BoolValue("Debug", true)

    @EventTarget
    fun move(item: Int, isArmorSlot: Boolean) {
        if (item != -1) {
            val openInventory = mc.currentScreen !is GuiInventory
            if (openInventory) mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
            mc.playerController.windowClick(
                mc.thePlayer.inventoryContainer.windowId,
                if (isArmorSlot) item else if (item < 9) item + 36 else item,
                0,
                1,
                mc.thePlayer
            )
            if (openInventory) mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.health <= healthValue.get() && !mc.thePlayer.capabilities.isCreativeMode && !mc.isIntegratedServerRunning) {
            if (keepArmorValue.get()) {
                for (i in 0..3) {
                    val armorSlot = 3 - i
                    move(8 - armorSlot, true)
                }
            }

            if (declarationValue.get()) mc.thePlayer.sendChatMessage("[${LiquidBounce.CLIENT_NAME}] Good Bye~~")

            when (modeValue.get().toLowerCase()) {
                "quit" -> mc.theWorld.sendQuittingDisconnectingPacket()
                "hytpit" -> mc.thePlayer.sendChatMessage("/hub")
                "invalidpacket" -> mc.netHandler.addToSendQueue(C04PacketPlayerPosition(Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, !mc.thePlayer.onGround))
                "selfhurt" -> mc.netHandler.addToSendQueue(C02PacketUseEntity(mc.thePlayer, C02PacketUseEntity.Action.ATTACK))
                "illegalchat" -> mc.thePlayer.sendChatMessage(Random().nextInt().toString() + "§§§" + Random().nextInt())
                "custom" -> mc.thePlayer.sendChatMessage(customTextValue.get())
            }

            if (debugValue.get()) LiquidBounce.hud.addNotification(Notification("Trigger Auto Leave!", Notification.Type.WARNING, 3000))

            if (autoDisableValue.get()) state = false
        }
    }
}