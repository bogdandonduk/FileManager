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
import pro.filemanager.R
import pro.filemanager.core.base.BaseAlbumItem
import pro.filemanager.core.base.BaseBottomSheetDialogFragment
import pro.filemanager.core.base.BaseItem
import pro.filemanager.databinding.LayoutInfoAlbumBottomModalSheetBinding
import java.util.*

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

        requireArguments().getParcelableArray(InfoTool.KEY_ARGUMENT_INFO_ITEMS)?.apply {
            if(this.size == 1) {
                binding.layoutInfoAlbumBottomModalSheetContentLayoutName.text = (this[0] as BaseAlbumItem).displayName
                binding.layoutInfoAlbumBottomModalSheetContentLayoutPath.text = (this[0] as BaseAlbumItem).data

                (this[0] as BaseAlbumItem).apply {
                    containedItems.forEach {
                        this.containedItems.forEach {
                            this.totalSize += it.size
                        }
                    }
                }
                binding.layoutInfoAlbumBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, (this[0] as BaseAlbumItem).totalSize)
            } else {
                binding.layoutInfoAlbumBottomModalSheetContentLayoutNameTitle.visibility = View.GONE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutName.visibility = View.GONE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutPathTitle.visibility = View.GONE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutPath.visibility = View.GONE

                binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantityTitle.visibility = View.VISIBLE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantity.text = String.format(frContext.resources.getString(R.string.info_total_quantity_content), this.size)
                binding.layoutInfoAlbumBottomModalSheetContentLayoutTotalQuantity.visibility = View.VISIBLE
                binding.layoutInfoAlbumBottomModalSheetContentLayoutSizeTitle.text = frContext.resources.getString(R.string.info_total_size)

                var totalSize = 0L

                this.forEach {
                    totalSize += (it as BaseAlbumItem).totalSize
                }

                binding.layoutInfoAlbumBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, totalSize)
            }
        }
    }

}