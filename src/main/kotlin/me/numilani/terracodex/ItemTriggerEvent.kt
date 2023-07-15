package me.numilani.terracodex

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import java.security.SecureRandom

class ItemTriggerEvent(var plugin: TerraCodex) : Listener {

    @EventHandler
    fun onLoredItemPickup(event: EntityPickupItemEvent){
        if (event.entity is Player && event.item.hasMetadata("tcpickuploreid")){
            plugin.dataSource.revealPage(event.item.getMetadata("tcpickuploreid").first().asString(), event.entity.uniqueId.toString())
        }
    }
}