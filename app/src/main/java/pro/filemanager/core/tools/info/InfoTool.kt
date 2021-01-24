package pro.filemanager.core.tools.info

import androidx.fragment.app.FragmentManager
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseLibraryItem
import pro.filemanager.core.tools.info.folders.InfoFoldersFragment
import pro.filemanager.core.tools.info.library.InfoLibraryFragment

object InfoTool {

    @Volatile var lastLibraryItems = mutableListOf<BaseLibraryItem>()
    @Volatile var lastFolderItems = mutableListOf<BaseFolderItem>()

    @Volatile var sheetShown = false

    fun showInfoForLibraryItems(fm: FragmentManager, libraryItems: MutableList<BaseLibraryItem>) {
        if(!sheetShown) {
            sheetShown = true

            lastLibraryItems.clear()
            lastLibraryItems.addAll(libraryItems)

            InfoLibraryFragment().show(fm, null)
        }
    }

    fun showInfoForFolderItems(fm: FragmentManager, folderItems: MutableList<BaseFolderItem>) {
        if(!sheetShown) {
            sheetShown = true

            lastFolderItems.clear()
            lastFolderItems.addAll(folderItems)

            InfoFoldersFragment().show(fm, null)
        }
    }
}