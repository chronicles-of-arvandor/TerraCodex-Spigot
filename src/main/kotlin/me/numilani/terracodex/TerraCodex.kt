package me.numilani.terracodex

import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler
import com.bergerkiller.bukkit.common.config.FileConfiguration
import me.numilani.terracodex.data.IDataSourceConnector
import me.numilani.terracodex.data.SqliteDataSourceConnector
import org.bukkit.plugin.java.JavaPlugin

class TerraCodex() : JavaPlugin() {
    private val cmdHandler: CloudSimpleHandler = CloudSimpleHandler()
    lateinit var cfg: FileConfiguration
    lateinit var dataSource: IDataSourceConnector

    override fun onEnable() {
        // First run setup
        var isFirstRun = false
        if (!FileConfiguration(this, "config.yml").exists()) {
            isFirstRun = true
            doPluginInit()
        }

        cfg = FileConfiguration(this, "config.yml")
        cfg.load()

        // setup data source
        when (cfg.get("dataSourceType", "NOTSPECIFIED")) {
            "sqlite" -> {
                dataSource = SqliteDataSourceConnector(this)
                if (isFirstRun) dataSource.initDatabase()
            }
            else -> throw Exception("Invalid dataSourceType in config.yml: " + cfg.get("dataSourceType", "NOTSPECIFIED"))
        }

        // Parse and register all commands
        cmdHandler.enable(this)
        cmdHandler.parser.parse(CodexCommands(this))
    }

    private fun doPluginInit() {
        var cfgFile = FileConfiguration(this, "config.yml")
        cfgFile.addHeader("Sets the source of data. Options: sqlite")
        cfgFile.set("dataSourceType", "sqlite")

        cfgFile.addHeader("If using anything other than sqlite, enter your connection string here.")
        cfgFile.set("jdbcConnString", "")

//        cfgFile.addHeader("When initializing the database, this codex name will be used.")
//        cfgFile.set("initCodexName", "CHANGEME")

        cfgFile.saveSync()
    }

    override fun onDisable() {
        dataSource.ensureConnClosed()
    }
}
