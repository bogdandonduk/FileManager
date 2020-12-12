package pro.filemanager.core.tools.info

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import pro.filemanager.ApplicationLoader
import pro.filemanager.R
import pro.filemanager.core.base.BaseBottomSheetDialogFragment
import pro.filemanager.core.base.BaseItem
import pro.filemanager.databinding.LayoutInfoItemBottomModalSheetBinding
import java.text.SimpleDateFormat
import java.util.*

class InfoItemBottomModalSheetFragment : BaseBottomSheetDialogFragment() {

    lateinit var binding: LayoutInfoItemBottomModalSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutInfoItemBottomModalSheetBinding.inflate(inflater, container, false)

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
                binding.layoutInfoBottomModalSheetContentLayoutName.text = (this[0] as BaseItem).displayName
                binding.layoutInfoBottomModalSheetContentLayoutPath.text = (this[0] as BaseItem).data
                binding.layoutInfoBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, (this[0] as BaseItem).size)
                binding.layoutInfoBottomModalSheetContentLayoutDateAdded.text = SimpleDateFormat("HH:mm:ss, dd.MM.yyyy", Locale.ROOT).format(Date((this[0] as BaseItem).dateAdded * 1000))
            } else {
                binding.layoutInfoBottomModalSheetContentLayoutNameTitle.visibility = View.GONE
                binding.layoutInfoBottomModalSheetContentLayoutName.visibility = View.GONE
                binding.layoutInfoBottomModalSheetContentLayoutPathTitle.visibility = View.GONE
                binding.layoutInfoBottomModalSheetContentLayoutPath.visibility = View.GONE
                binding.layoutInfoBottomModalSheetContentLayoutDateAddedTitle.visibility = View.GONE
                binding.layoutInfoBottomModalSheetContentLayoutDateAdded.visibility = View.GONE

                binding.layoutInfoBottomModalSheetContentLayoutTotalQuantityTitle.visibility = View.VISIBLE
                binding.layoutInfoBottomModalSheetContentLayoutTotalQuantity.text = String.format(frContext.resources.getString(R.string.info_total_quantity_content), this.size)
                binding.layoutInfoBottomModalSheetContentLayoutTotalQuantity.visibility = View.VISIBLE
                binding.layoutInfoBottomModalSheetContentLayoutSizeTitle.text = frContext.resources.getString(R.string.info_total_size)

                var totalSize = 0L

                this.forEach {
                    totalSize += (it as BaseItem).size
                }

                binding.layoutInfoBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, totalSize)
            }
        }
    }

}