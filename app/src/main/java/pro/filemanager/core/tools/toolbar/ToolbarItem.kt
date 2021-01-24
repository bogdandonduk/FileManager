package pro.filemanager.core.tools.toolbar

data class ToolbarItem(val title: String, val iconRes: Int, var action: () -> Unit)