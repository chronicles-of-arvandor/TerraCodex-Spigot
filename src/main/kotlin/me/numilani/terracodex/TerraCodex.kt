package me.numilani.terracodex

import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class TerraCodex() : JavaPlugin() {
    private val cmdHandler : CloudSimpleHandler = CloudSimpleHandler()

    override fun onEnable() {

        val dbFile = File(dataFolder, "sample.db")
        // TODO: once SqliteDataSourceConnector is written, intialize here

        // Parse and register all commands
        cmdHandler.enable(this)
    }


    override fun onDisable() {
        // Plugin shutdown logic
    }
}