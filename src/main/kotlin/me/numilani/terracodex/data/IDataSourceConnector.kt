package me.numilani.terracodex.data

/*
For sake of getting an MVP out as quickly as possible,
I'm using Sqlite for storing data. Eventually, though,
the plugin should establish a connection to either
an external database or, more likely, some API endpoint
for a TerraCodex server, since the web interface will
likely act as the primary server in the future.
*/

interface IDataSourceConnector {

    fun establishConnection()

    /**
     * Creates a new codex. Returns the ID of the new codex.
     */
    fun createCodex(name: String): String
    fun getCodexByName(name: String): Codex?
    fun getCodexById(id: String): Codex?

    /**
     * Updates a codex by comparing and merging changes. Returns true if update succeeds.
     */
    fun updateCodex(change: Codex) : Boolean
    // NOTE: The lack of a "delete codex" is intentional here.
    // Deleting a codex should not be done in-game, only by an admin.

    /**
     * Creates a new category. Returns the ID of the new category.
     */
    fun createCategory(name: String, codexId: String): String
    fun getCategories(codexId: String): List<Category>
    fun getCategoryByName(name: String): Category?
    fun getCategoryById(id: String): Category?

    /**
     * Updates a category by comparing and merging. Returns true if update succeeds.
     */
    fun updateCategory(change: Category) : Boolean

    /**
     * Creates a new page. Returns the ID of the new page.
     */
    fun createPage(name: String, categoryId: String) : String

    fun getAllCategoryPages(categoryId: String): List<Page>
    fun getAllCodexPages(codexId: String): List<Page>

    /**
     * Find pages by search criterion. All fields are optional as they can be mixed and matched.
     * If no parameters are filled in, this function should return an empty list,
     * otherwise it should return a list of codex entries.
     *
     * @param tags A list of tags to match.
     * @param categories A list of categories to match.
     * @param fieldValue A list of field/value pairs to match
     * @param textSearch A string to search for in the contents of the codex entry.
     */
    fun findPages(tags: List<String>, categories: List<String>, fieldValue: List<Pair<String, String>>, textSearch: String): List<Page>

    fun getPageByName(name: String): Page?
    fun getPageById(id: String): Page?

    fun revealPage(pageId: String, playerId: String)
}