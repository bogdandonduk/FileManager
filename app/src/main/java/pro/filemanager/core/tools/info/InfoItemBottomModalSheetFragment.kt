package pro.filemanager.core.tools.info

import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
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
import pro.filemanager.core.generics.BaseBottomSheetDialogFragment
import pro.filemanager.core.generics.BaseItem
import pro.filemanager.databinding.LayoutInfoItemBottomModalSheetBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
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

        val items = mutableListOf<BaseItem>().apply {
            addAll(InfoTool.lastItems)
        }

        InfoTool.lastItems.clear()

        items.apply {
            if(this.size == 1) {
                binding.layoutInfoBottomModalSheetContentLayoutName.text = this[0].displayName
                binding.layoutInfoBottomModalSheetContentLayoutPath.text = this[0].data
                binding.layoutInfoBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, this[0].size)
                binding.layoutInfoBottomModalSheetContentLayoutDateAdded.text = SimpleDateFormat("HH:mm:ss, dd.MM.yyyy", Locale.ROOT).format(Date(this[0].dateModified * 1000))
            } else {
                ApplicationLoader.ApplicationDefaultScope.launch {
                    var totalSize = 0L

                    forEach {
                        totalSize += it.size
                    }

                    withContext(Main) {
                        binding.layoutInfoBottomModalSheetContentLayoutSize.text = Formatter.formatFileSize(frContext, totalSize)
                    }
                }

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
            }
        }
    }
}