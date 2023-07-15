package me.numilani.terracodex

import com.bergerkiller.bukkit.common.cloud.CloudSimpleHandler
import com.bergerkiller.bukkit.common.config.FileConfiguration
import me.numilani.terracodex.data.IDataSourceConnector
import me.numilani.terracodex.data.MariadbDataSourceConnector
import me.numilani.terracodex.data.SqliteDataSourceConnector
import org.bukkit.plugin.java.JavaPlugin
import java.sql.DriverManager

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
                if (!dataSource.databaseIsInitialized()) dataSource.initDatabase()
            }
            "mariadb" -> {
                var dbPath = cfg.get("jdbcConnString", "NOURL")
                if (dbPath == "NOURL") throw Exception("invalid db connection string in config.yml")

                var dbUser = cfg.get("dbUsername", "NOUSER")
                var dbPwd = cfg.get("dbPassword", "NOPASS")
                if (dbUser == "NOUSER" || dbPwd == "NOPASS"){
                    throw Exception("Invalid database credentials in config.yml")
                }

                dataSource = MariadbDataSourceConnector(this, dbPath, dbUser, dbPwd)
                if (!dataSource.databaseIsInitialized()) dataSource.initDatabase()
            }
            else -> throw Exception("Invalid dataSourceType in config.yml")
        }

        // Parse and register all commands
        cmdHandler.enable(this)
        cmdHandler.parser.parse(CodexCommands(this))
    }

    private fun doPluginInit() {
        var cfgFile = FileConfiguration(this, "config.yml")
        cfgFile.addHeader("Sets the source of data. Options: sqlite")
        cfgFile.set("dataSourceType", "sqlite")

        cfgFile.addHeader("If using anything other than sqlite, enter your connection string and credentials here.")
        cfgFile.set("jdbcConnString", "")
        cfgFile.set("dbUsername", "")
        cfgFile.set("dbPassword", "")

        cfgFile.saveSync()
    }

    override fun onDisable() {
        dataSource.ensureConnClosed()
    }
}
