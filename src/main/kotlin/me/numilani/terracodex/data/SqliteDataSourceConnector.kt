package me.numilani.terracodex.data

import me.numilani.terracodex.TerraCodex
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class SqliteDataSourceConnector(plugin: TerraCodex) : IDataSourceConnector {
    private val dbFilename = "sample.db"
    override val conn: Connection =
        DriverManager.getConnection("jdbc:sqlite:" + File(plugin.dataFolder, dbFilename).path)

    override fun ensureConnClosed() {
        if (!conn.isClosed) {
            conn.close()
        }
    }

    override fun initDatabase() {
        val statement = conn.createStatement()
        statement.execute("CREATE TABLE Codex (id TEXT PRIMARY KEY, name TEXT)")
        statement.execute("CREATE TABLE Category (id TEXT PRIMARY KEY, codexId TEXT, name TEXT)")
        statement.execute("CREATE TABLE Page (id TEXT PRIMARY KEY, categoryId TEXT, name TEXT, contents TEXT, tags TEXT, revealedTo TEXT, pageStatus TEXT)")
        statement.execute("CREATE TABLE Tag (id TEXT PRIMARY KEY, codexId TEXT, name TEXT)")
        statement.execute("CREATE TABLE InternalConfig (name TEXT PRIMARY KEY, value TEXT)")

        statement.execute("INSERT INTO InternalConfig (name, value) values ('defaultCodex', null)")

        val codexId = createCodex("default")
        updateConfig("defaultCodex", codexId)
    }

    override fun createCodex(name: String): String {
        val statement = conn.prepareStatement("INSERT INTO Codex (id, name) VALUES (?,?)")
        val uuid: String = UUID.randomUUID().toString()
        statement.setString(1, uuid)
        statement.setString(2, name)
        statement.execute()
        return uuid
    }

    override fun getCodexById(id: String): Codex? {
        var returnValue: Codex? = null

        val statement = conn.prepareStatement("SELECT id, name FROM Codex WHERE id = ?")
        statement.setString(1, id)
        val queryRes = statement.executeQuery()

        // there can only be one row as name is SQL UNIQUE, so only get the first row
        if (queryRes.next()) {
            returnValue = Codex(queryRes.getString(1), queryRes.getString(2))
        }
        return returnValue
    }

    override fun updateCodex(change: Codex): Boolean {
        var success = false

        val statement = conn.prepareStatement("UPDATE Codex SET name = ? WHERE id = ?")
        statement.setString(1, change.name)
        statement.setString(2, change.id)
        if (statement.executeUpdate() == 1) success = true
        return success
    }

    override fun createCategory(name: String, codexId: String): String {
        val statement = conn.prepareStatement("INSERT INTO Category (id, codexId, name) VALUES (?,?,?)")
        val uuid: String = UUID.randomUUID().toString()
        statement.setString(1, uuid)
        statement.setString(2, codexId)
        statement.setString(3, name)
        statement.execute()
        return uuid
    }

    override fun getCategories(codexId: String): List<Category> {
        val returnValue = mutableListOf<Category>()

        val statement = conn.prepareStatement("SELECT id, codexId, name FROM Category WHERE codexId = ?")
        statement.setString(1, codexId)
        val queryRes = statement.executeQuery()

        while (queryRes.next()) {
            returnValue += Category(queryRes.getString(1), queryRes.getString(2), queryRes.getString(3))
        }
        return returnValue
    }

    override fun getCategoryById(id: String): Category? {
        var returnValue: Category? = null

        val statement = conn.prepareStatement("SELECT id, codexId, name FROM Category WHERE id = ?")
        statement.setString(1, id)
        val queryRes = statement.executeQuery()

        // there should only be one, so just get the first row
        if (queryRes.next()) {
            returnValue = Category(queryRes.getString(1), queryRes.getString(2), queryRes.getString(3))
        }
        return returnValue
    }

    override fun getCategoryByName(name: String, codexId: String): Category? {
        var returnValue: Category? = null

        val statement = conn.prepareStatement("SELECT id, codexId, name FROM Category WHERE name = ? AND codexId = ?")
        statement.setString(1, name)
        statement.setString(2, codexId)
        val queryRes = statement.executeQuery()

        if (queryRes.next()) {
            returnValue = Category(queryRes.getString(1), queryRes.getString(2), queryRes.getString(3))
        }
        return returnValue
    }

    override fun updateCategory(change: Category): Boolean {
        var success = false

        val statement = conn.prepareStatement("UPDATE Category SET name = ?, codexId = ? WHERE id = ?")
        statement.setString(1, change.name)
        statement.setString(2, change.codexId)
        statement.setString(3, change.id)
        if (statement.executeUpdate() == 1) success = true
        return success
    }

    override fun deleteCategory(id: String): Boolean {
        var success = false

        val statement = conn.prepareStatement("DELETE FROM Category WHERE id = ?")
        statement.setString(1, id)
        if (statement.executeUpdate() == 1) success = true

        return success
    }

    override fun createPage(name: String, categoryId: String): String {
        val statement =
            conn.prepareStatement("INSERT INTO Page (id, categoryId, name, contents, tags, revealedTo, pageStatus) VALUES (?,?,?,'{}','{}','{}','NEW')")
        val uuid: String = UUID.randomUUID().toString()
        statement.setString(1, uuid)
        statement.setString(2, categoryId)
        statement.setString(3, name)
        statement.execute()
        return uuid
    }

    override fun updatePage(id: String, key: String, value: String): Boolean {
        var success = false

        var statementBody = "UPDATE Page SET "
        statementBody += when (key) {
            "name" -> "name = ?"
            "categoryId" -> "categoryId = ?"
            "tags" -> "tags = ?"
            "status" -> "status = ?"
            else -> "contents = json_set(contents, '$.$key', ?)"
        }
        statementBody += " WHERE id = ?"
        val statement = conn.prepareStatement(statementBody)
        statement.setString(1, value)
        statement.setString(2, id)

        if (statement.executeUpdate() == 1) success = true
        return success
    }

    override fun deletePage(id: String): Boolean {
        var success = false

        val statement = conn.prepareStatement("DELETE FROM Page WHERE id = ?")
        statement.setString(1, id)
        if (statement.executeUpdate() == 1) success = true

        return success
    }

    override fun getAllCategoryPages(categoryId: String): List<Page> {
        val returnValue = mutableListOf<Page>()

        val statement =
            conn.prepareStatement("SELECT id, categoryId, name, contents, tags, revealedTo, pageStatus FROM Page WHERE categoryId = ?")
        statement.setString(1, categoryId)
        val queryRes = statement.executeQuery()

        while (queryRes.next()) {
            returnValue += Page(
                queryRes.getString(1),
                queryRes.getString(2),
                queryRes.getString(3),
                queryRes.getString(4) ?: "{}",
                queryRes.getString(5) ?: "{}",
                queryRes.getString(6) ?: "{}",
                queryRes.getString(7) ?: "{}"
            )
        }
        return returnValue
    }

    // is this one right? This logic seems off - needs testing but its 2 AM
    override fun getAllCodexPages(codexId: String): List<Page> {
        val returnValue = mutableListOf<Page>()

        val statement =
            conn.prepareStatement("SELECT Page.id, categoryId, Page.name, contents, tags, revealedTo, pageStatus, Category.codexId FROM Page JOIN Category ON Category.id = Page.categoryId WHERE Category.codexId = ?")
        statement.setString(1, codexId)
        val queryRes = statement.executeQuery()

        while (queryRes.next()) {
            returnValue += Page(
                queryRes.getString(1),
                queryRes.getString(2),
                queryRes.getString(3),
                queryRes.getString(4) ?: "{}",
                queryRes.getString(5) ?: "{}",
                queryRes.getString(6) ?: "{}",
                queryRes.getString(7) ?: "{}"
            )
        }
        return returnValue
    }

    override fun findPages(
        tags: List<String>,
        categories: List<String>,
        fieldValue: List<Pair<String, String>>,
        textSearch: String
    ): List<Page> {
        TODO("Not yet implemented")
    }

    override fun getPageById(id: String): Page? {
        var returnValue: Page? = null

        val statement =
            conn.prepareStatement("SELECT id, categoryId, name, contents, tags, revealedTo, pageStatus FROM Page WHERE id = ?")
        statement.setString(1, id)
        val queryRes = statement.executeQuery()

        // there should only be one, so just get the first row
        if (queryRes.next()) {
            returnValue = Page(
                queryRes.getString(1),
                queryRes.getString(2),
                queryRes.getString(3),
                queryRes.getString(4) ?: "{}",
                queryRes.getString(5) ?: "{}",
                queryRes.getString(6) ?: "{}",
                queryRes.getString(7) ?: "{}"
            )
        }
        return returnValue
    }

    override fun getPageByName(name: String, codexId: String): Page? {
        var returnValue: Page? = null

        val statement =
            conn.prepareStatement("SELECT Page.id, categoryId, Page.name, contents, tags, revealedTo, pageStatus FROM Page JOIN Category ON Page.categoryId = Category.id WHERE Page.name = ? and Category.codexId = ?")
        statement.setString(1, name)
        statement.setString(2, codexId)
        val queryRes = statement.executeQuery()

        // there should only be one, so just get the first row
        if (queryRes.next()) {
            returnValue = Page(
                queryRes.getString(1),
                queryRes.getString(2),
                queryRes.getString(3),
                queryRes.getString(4) ?: "{}",
                queryRes.getString(5) ?: "{}",
                queryRes.getString(6) ?: "{}",
                queryRes.getString(7) ?: "{}"
            )
        }
        return returnValue
    }

    override fun revealPage(pageId: String, playerId: String) {
        TODO("Not yet implemented")
    }

    override fun getConfig(key: String): String {
        val statement = conn.prepareStatement("SELECT value FROM InternalConfig WHERE name = ?")
        statement.setString(1, key)
        val queryres = statement.executeQuery()

        while (queryres.next()) {
            return queryres.getString(1)
        }
        return ""
    }

    override fun updateConfig(key: String, value: String) {
        val statement = conn.prepareStatement("UPDATE InternalConfig SET value = ? WHERE name = ?")
        statement.setString(1, value)
        statement.setString(2, key)
        statement.execute()
    }
}
