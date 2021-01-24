package pro.filemanager.core.tools

import pro.filemanager.core.base.BaseItem

object SearchTool {
    fun <T : BaseItem> search(query: String, dataSet: MutableList<T>) : MutableList<T> =
            mutableListOf<T>().apply {
                dataSet.forEach {
                    if(it.displayName.startsWith(query, true)) {
                        add(it)
                    }
                }

                dataSet.forEach {
                    if(it.displayName.contains(query, true)  && !contains(it)) {
                        add(it)
                    }
                }
            }
}