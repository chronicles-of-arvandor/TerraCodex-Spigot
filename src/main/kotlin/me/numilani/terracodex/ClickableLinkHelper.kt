package me.numilani.terracodex

class ClickableLinkHelper {

    fun createClickableCategoryJson(text: String, id: String): String {
        return "{\\\"text\\\":\\\"${text}\\\",\\\"bold\\\":true,\\\"color\\\":\\\"gold\\\",\\\"clickEvent\\\":{\\\"action\\\":\\\"run_command\\\",\\\"value\\\":\\\"/tc show-pages ${id}\\\"},\\\"hoverEvent\\\":{\\\"action\\\":\\\"show_text\\\",\\\"contents\\\":[\\\"View entries for ${text}\\\"]}}"
    }
}
