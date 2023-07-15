package me.numilani.terracodex

import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandMethod
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

class CodexCommands(var plugin: TerraCodex) {
    val defaultCodexId = plugin.dataSource.getConfig("defaultCodex")
    var colorSysMsg = "${ChatColor.RESET}${ChatColor.BOLD}${ChatColor.GRAY}"
    var colorSubjectMsg = "${ChatColor.RESET}${ChatColor.ITALIC}${ChatColor.GOLD}"
    var colorErrMsg = "${ChatColor.RESET}${ChatColor.BOLD}${ChatColor.DARK_RED}"

    @CommandMethod("tc debug")
    fun debugInfo(sender: CommandSender) {
        if (!plugin.dataSource.conn.isValid(10)) {
            sender.sendMessage("DB connection failed")
            return
        } else {
            sender.sendMessage("DB connection succeeded")
            var x = plugin.dataSource.getConfig("defaultCodex")
            sender.sendMessage(
                "Current Codex: ${plugin.dataSource.getCodexById(x)?.id} (${
                    plugin.dataSource.getCodexById(
                        x
                    )?.name
                })"
            )
        }
    }

    @CommandMethod("tc category new <name>")
    fun createCategory(sender: CommandSender, @Argument("name") name: String) {
        if (plugin.dataSource.getCategoryByName(name, defaultCodexId) != null) {
            sender.sendMessage("A category with that name already exists!")
            return
        }
        try {
            var id = plugin.dataSource.createCategory(name, defaultCodexId)
            sender.sendMessage("${colorSysMsg}Category $colorSubjectMsg$name created!")
        } catch (e: Exception) {
            sender.sendMessage("${colorErrMsg}Failed to create category - does it already exist? (see server log)")
            plugin.logger.warning("Failed to create category: ${e.message} ${e.stackTraceToString()}")
        }
    }

    @CommandMethod("tc category list")
    fun listCategories(sender: CommandSender) {
        sender.sendMessage("${colorSysMsg}CATEGORIES:")
        for (category in plugin.dataSource.getCategories(defaultCodexId)) {
            sender.sendMessage("$colorSubjectMsg${category.name}")
        }
    }

    @CommandMethod("tc category rename <name> <newName>")
    fun renameCategory(sender: CommandSender, @Argument("name") name: String, @Argument("newName") newName: String) {
        var category = plugin.dataSource.getCategoryByName(name, defaultCodexId)
        if (category == null) {
            sender.sendMessage("${colorErrMsg}Cannot rename a category that doesn't exist. Check the category and try again.")
            return
        }
        var oldName = category.name
        category.name = newName
        try {
            var success = plugin.dataSource.updateCategory(category)
            if (success) sender.sendMessage("${colorSysMsg}Renamed $oldName to ${category.name}")
            else sender.sendMessage("${colorErrMsg}Couldn't find a category with that name!")
        } catch (e: Exception) {
            sender.sendMessage("${colorErrMsg}Failed to update category (see server log)")
            plugin.logger.warning("Failed to update category: ${e.message} ${e.stackTraceToString()}")
        }

    }

    @CommandMethod("tc category delete <name>")
    fun deleteCategory(sender: CommandSender, @Argument("name") name: String) {
        var category = plugin.dataSource.getCategoryByName(name, defaultCodexId)
        if (category == null) {
            sender.sendMessage("${colorErrMsg}Couldn't find a category by that name!")
            return
        }
        var categoryPages = plugin.dataSource.getAllCategoryPages(category.id)
        if (categoryPages.isNotEmpty()) {
            sender.sendMessage("${colorErrMsg}You cannot delete a category that contains pages!")
            return
        }
        try {
            plugin.dataSource.deleteCategory(category.id)
            sender.sendMessage("${colorSysMsg}Successfully deleted category $colorSubjectMsg$name!")
        } catch (e: Exception) {
            sender.sendMessage("${colorErrMsg}Failed to delete category (see server log)")
            plugin.logger.warning("Failed to delete category: ${e.message} ${e.stackTraceToString()}")
        }
    }

    @CommandMethod("tc category pages <name>")
    fun listPagesInCategory(sender: CommandSender, @Argument("name") name: String) {
        var category = plugin.dataSource.getCategoryByName(name, defaultCodexId)
        if (category == null) {
            sender.sendMessage("${colorErrMsg}Couldn't find a category by that name!")
            return
        }
        sender.sendMessage("${colorSysMsg}Pages in \"${plugin.dataSource.getCategoryById(category.id)?.name}\"")
        for (page in plugin.dataSource.getAllCategoryPages(category.id)) {
            sender.sendMessage("$colorSubjectMsg${page.name}")
        }
    }

    @CommandMethod("tc page new <name> <categoryName>")
    fun createPage(
        sender: CommandSender, @Argument("name") name: String, @Argument("categoryName") categoryName: String
    ) {
        var category = plugin.dataSource.getCategoryByName(categoryName, defaultCodexId)
        if (category == null) {
            sender.sendMessage("${colorErrMsg}Can't find a category by that name!")
            return
        }
        try {
            var pageId = plugin.dataSource.createPage(name, category.id)
            sender.sendMessage("${colorSysMsg}Page \"$name\" created!")
        } catch (e: Exception) {
            sender.sendMessage("${colorErrMsg}Couldn't create page - does it already exist? (see console log)")
            plugin.logger.warning("Couldn't create page: ${e.message} ${e.stackTraceToString()}")
        }
    }

    @CommandMethod("tc page view <name>")
    fun getPageByName(sender: CommandSender, @Argument("name") name: String) {
        val page = plugin.dataSource.getPageByName(name, defaultCodexId)
        if (page == null) {
            sender.sendMessage("${colorErrMsg}Cannot find a page by that name!")
            return
        }
        val category = plugin.dataSource.getCategoryById(page.categoryId)

        sender.sendMessage("${colorSysMsg}Contents of: $colorSubjectMsg${page.name}${colorSysMsg}")
        sender.sendMessage("${colorSysMsg}Category:${ChatColor.RESET} ${ChatColor.GRAY}${category?.name}")
        var jsonContents = Json.decodeFromString<Map<String, String>>(page.contents)
        for (key in jsonContents.keys) {
            if (jsonContents.getValue(key).length > 0) {
                sender.sendMessage("${colorSysMsg}$key: ${ChatColor.RESET}${ChatColor.GRAY}${jsonContents.getValue(key)}")
            }
        }
    }

    @CommandMethod("tc page update-field <name> <key> <value>")
    fun updatePageContents(
        sender: CommandSender,
        @Argument("name") name: String,
        @Argument("key") key: String,
        @Argument("value") value: String
    ) {
        var page = plugin.dataSource.getPageByName(name, defaultCodexId)
        if (page == null) {
            sender.sendMessage("${colorErrMsg}Cannot find a page by that name!")
            return
        }

        try {
            plugin.dataSource.updatePage(page.id, key, value)
            sender.sendMessage("${colorSysMsg}Page Updated!")
        } catch (e: Exception) {
            sender.sendMessage("${colorErrMsg}Couldn't update page (see server log)")
            plugin.logger.warning("Couldn't update page: ${e.message} ${e.stackTraceToString()}")
        }
    }

    @CommandMethod("tc page append-field <name> <key> <value>")
    fun appendPageContents(
        sender: CommandSender,
        @Argument("name") name: String,
        @Argument("key") key: String,
        @Argument("value") value: String
    ) {
        var page = plugin.dataSource.getPageByName(name, defaultCodexId)
        if (page == null) {
            sender.sendMessage("${colorErrMsg}Cannot find a page by that name!")
            return
        }
        var decodedContents = Json.decodeFromString<Map<String, String>>(page.contents).toMutableMap()
        if (!decodedContents.containsKey(key)) {
            sender.sendMessage("${colorErrMsg}Can't find a key \"${key}\" to append to!")
            return
        }
        val newValue = decodedContents[key].toString() + value

        try {
            plugin.dataSource.updatePage(page.id, key, newValue)
            sender.sendMessage("${colorSysMsg}Page Updated!")
        } catch (e: Exception) {
            sender.sendMessage("${colorErrMsg}Couldn't update page (see server log)")
            plugin.logger.warning("Couldn't update page: ${e.message} ${e.stackTraceToString()}")
        }
    }
}