package pro.filemanager.core.tools.info

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.base.BaseFolderItem
import pro.filemanager.core.base.BaseBottomSheetDialogFragment
import pro.filemanager.databinding.LayoutInfoAlbumBottomModalSheetBinding

class InfoAlbumBottomModalSheetFragment : BaseBottomSheetDialogFragment() {

    lateinit var binding: LayoutInfoAlbumBottomModalSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutInfoAlbumBottomModalSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onCancel(dialog: DialogInterface) {
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        InfoTool.showingDialogInProgress = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.setOnShowListener {
            BottomSheetBehavior.from(dialog!!.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!).state = BottomSheetBehavior.STATE_EXPANDED
        }

        (view?.parent as View).run {
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(5, 0, 5, 0)
        }

        val albums = mutableListOf<BaseFolderItem>().apply {
            addAll(InfoTool.lastAlbums)
        }

        InfoTool.lastAlbums.clear()

        albums.let { albumItems ->
            if(albumItems.size == 1) {
                ApplicationLoader.ApplicationDefaultScope.launch {
                    var totalSize = 0L

                    albumItems[0].containedItems.forEach {
                        totalSize += it.size
                    }

                    withContext(Main) {
                        binding.layoutInfoAlbumBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, albumItems[0].totalSize)
                    }
                }

                binding.layoutInfoAlbumBottomModalSheetContentLayoutName.text = albumItems[0].displayName
                binding.layoutInfoAlbumBottomModalSheetContentLayoutPath.text = albumItems[0].data
                binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantity.text = albumItems[0].containedItems.size.toString()

            } else {
                ApplicationLoader.ApplicationDefaultScope.launch {
                    var totalQuantity = 0
                    var totalSize = 0L

                    albumItems.forEach { album ->
                        totalQuantity += album.containedItems.size
                        album.containedItems.forEach {
                            totalSize += it.size
                        }
                    }

                    withContext(Main) {
                        binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantity.text = String.format(frContext.resources.getString(R.string.info_total_quantity_content), totalQuantity)
                        binding.layoutInfoAlbumBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, totalSize)
                    }
                }

                binding.layoutInfoAlbumBottomModalSheetContentLayoutNameTitle.visibility = View.GONE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutName.visibility = View.GONE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutPathTitle.visibility = View.GONE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutPath.visibility = View.GONE

                binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantityTitle.visibility = View.VISIBLE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantity.visibility = View.VISIBLE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutSizeTitle.text = frContext.resources.getString(R.string.info_total_size)
            }
        }
    }

}