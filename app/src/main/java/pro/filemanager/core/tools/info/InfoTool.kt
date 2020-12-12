package pro.filemanager.core.tools.info

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import pro.filemanager.core.base.BaseAlbumItem
import pro.filemanager.core.base.BaseItem

object InfoTool {
    const val KEY_ARGUMENT_INFO_ITEMS = "infoItems"

    @Volatile var showingDialogInProgress = false

    fun showInfoItemBottomModalSheetFragment(fm: FragmentManager, items: MutableList<BaseItem>) {
        showingDialogInProgress = true

        val sheet = InfoItemBottomModalSheetFragment()
            sheet.arguments = bundleOf(KEY_ARGUMENT_INFO_ITEMS to items.toTypedArray())

        sheet.show(fm, null)
    }

    fun showInfoAlbumBottomModalSheetFragment(fm: FragmentManager, items: MutableList<BaseAlbumItem>) {
        showingDialogInProgress = true

        val sheet = InfoAlbumBottomModalSheetFragment()
            sheet.arguments = bundleOf(KEY_ARGUMENT_INFO_ITEMS to items.toTypedArray())

        sheet.show(fm, null)
    }
}