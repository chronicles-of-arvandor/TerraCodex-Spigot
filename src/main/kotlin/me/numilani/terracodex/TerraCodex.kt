package me.numilani.terracodex

import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TerraCodex() : JavaPlugin() {
    var CmdHandler : CloudSimpleHandler = CloudSimpleHandler()

    override fun onEnable() {

        val dbfile = "${dataFolder}${File.separatorChar}sample.db"
        // TODO: once SqliteDataSourceConnector is written, intialize here

        // Parse and register all commands
        CmdHandler.enable(this)
    }


    override fun onDisable() {
        // Plugin shutdown logic
    }
}