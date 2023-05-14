package me.numilani.terracodex

import cloud.commandframework.annotations.CommandMethod
import org.bukkit.command.CommandSender

class CodexCommands(var plugin: TerraCodex) {

    @CommandMethod("tc debug")
    fun DebugInfo(sender: CommandSender){
        if (!plugin.dataSource.conn.isValid(10)){
            sender.sendMessage("DB connection failed")
            return
        }
        else{
            sender.sendMessage("DB connection succeeded")
            var x = plugin.dataSource.getConfig("defaultCodex")
            sender.sendMessage("Current Codex: ${plugin.dataSource.getCodexById(x)?.id} (${plugin.dataSource.getCodexById(x)?.name})")
        }
    }


}