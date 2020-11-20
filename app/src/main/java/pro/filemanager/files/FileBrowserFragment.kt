package pro.filemanager.files

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.databinding.FragmentFileBrowserBinding
import java.io.File
import java.lang.Exception

class FileBrowserFragment() : Fragment() {

    lateinit var binding: FragmentFileBrowserBinding
    lateinit var navController: NavController

    lateinit var activity: HomeActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFileBrowserBinding.inflate(inflater, container, false)

        binding.fragmentFileBrowserList.layoutManager = LinearLayoutManager(context)

        activity.requestExternalStoragePermission {

            ApplicationLoader.ApplicationIOScope.launch {

                if(requireArguments().getString(FileCore.KEY_ARGUMENT_PATH) == FileCore.KEY_INTERNAL_STORAGE) {
                    val path = FileCore.getInternalRootPath()

                    withContext(Dispatchers.Main) {
                        activity.supportActionBar?.title = requireArguments().getString(FileCore.KEY_ARGUMENT_APP_BAR_TITLE)

                        binding.fragmentFileBrowserPathTitle.text = path

                        try {
                            initAdapter(File(path).listFiles()!!)
                        } catch (e: Exception) {

                        }
                    }
                } else if(requireArguments().getString(FileCore.KEY_ARGUMENT_PATH) == FileCore.KEY_EXTERNAL_STORAGE) {
                    Log.d("TAG", "onCreateView: " + FileCore.getInternalDownMostRootPath())

                    try {
                        val externalPaths: MutableList<String> = FileCore.findExternalRoots(requireContext())

                        withContext(Dispatchers.Main) {
                            activity.supportActionBar?.title = requireArguments().getString(FileCore.KEY_ARGUMENT_APP_BAR_TITLE)

                            if(externalPaths.isNotEmpty()) {
                                if(externalPaths.size == 1) {
                                    binding.fragmentFileBrowserPathTitle.text = externalPaths[0]
                                    initAdapter(File(externalPaths[0]).listFiles()!!)
                                } else if(externalPaths.size > 1) {
                                    binding.fragmentFileBrowserPathTitle.text = FileCore.getInternalDownMostRootPath()
                                    val multipleExternals: Array<File> = Array(externalPaths.size) {
                                        File(externalPaths[it])
                                    }
                                    initAdapter(multipleExternals)
                                }
                            } else {
//                              TODO: No SD card found
                            }
                        }
                    } catch (thr: Throwable) {

                    }
                } else {
                    binding.fragmentFileBrowserPathTitle.text = requireArguments().getString(FileCore.KEY_ARGUMENT_PATH)

                    initAdapter(File(requireArguments().getString(FileCore.KEY_ARGUMENT_PATH)!!).listFiles()!!)
                }
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(binding.root)

    }

    fun initAdapter(files: Array<File>) {

        binding.fragmentFileBrowserList.adapter = FileBrowserAdapter(requireActivity(), files, layoutInflater, this@FileBrowserFragment)

    }

    fun navigate(path: String, appBarTitle: String = activity.supportActionBar?.title.toString()) {
        navController.navigate(R.id.action_fileBrowserFragment_self, bundleOf(
                FileCore.KEY_ARGUMENT_PATH to path,
                FileCore.KEY_ARGUMENT_APP_BAR_TITLE to appBarTitle
        ))
    }
}