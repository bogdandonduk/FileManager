package pro.filemanager.core.tools.info

import androidx.fragment.app.FragmentManager
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseItem

object InfoTool {

    @Volatile var lastItems = mutableListOf<BaseItem>()
    @Volatile var lastAlbums = mutableListOf<BaseFolderItem>()

    @Volatile var showingDialogInProgress = false

    fun showInfoItemBottomModalSheetFragment(fm: FragmentManager, items: MutableList<BaseItem>) {
        if(!showingDialogInProgress) {
            showingDialogInProgress = true

            lastItems.clear()
            lastItems.addAll(items)

            InfoItemBottomModalSheetFragment().show(fm, null)
        }
    }

    fun showInfoAlbumBottomModalSheetFragment(fm: FragmentManager, folderItems: MutableList<BaseFolderItem>) {
        if(!showingDialogInProgress) {
            showingDialogInProgress = true

            lastAlbums.clear()
            lastAlbums.addAll(folderItems)

            InfoAlbumBottomModalSheetFragment().show(fm, null)
        }
    }
}