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
import pro.filemanager.videos.VideoRepo
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

                val path: String = if(requireArguments().getString(FileManager.KEY_ARGUMENT_PATH) == FileManager.KEY_INTERNAL_STORAGE) {
                    FileManager.getInternalRootPath()
                } else if(requireArguments().getString(FileManager.KEY_ARGUMENT_PATH) == FileManager.KEY_EXTERNAL_STORAGE) {

                    if(FileManager.externalRootPath != null) {

                        FileManager.externalRootPath!!

                    } else {

                        FileManager.findExternalRoot(requireContext())

                        FileManager.externalRootPath!!

//                        if(!FileManager.findingExternalRootInProgress) {
//                            FileManager.findExternalRoot(requireContext())
//
//                            FileManager.externalRootPath!!
//                        } else {
//
//                            while(FileManager.findingExternalRootInProgress && FileManager.externalRootPath == null) {
//                                delay(25)
//                            }
//
//                            FileManager.externalRootPath!!
//                        }

                    }
                } else {
                    requireArguments().getString(FileManager.KEY_ARGUMENT_PATH)!!
                }

                withContext(Dispatchers.Main) {
                    activity.supportActionBar?.title = requireArguments().getString(FileManager.KEY_ARGUMENT_APP_BAR_TITLE)

                    binding.fragmentFileBrowserPathTitle.text = path

                    try {
                        initAdapter(File(path).listFiles()!!)
                    } catch (e: Exception) {
                        Log.d("TAG", "onCreateView: NOPE")
                    }

                }

                if(FileManager.externalRootPath == null && !FileManager.findingExternalRootInProgress) {
                    ApplicationLoader.findExternalRoot()
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
                FileManager.KEY_ARGUMENT_PATH to path,
                FileManager.KEY_ARGUMENT_APP_BAR_TITLE to appBarTitle
        ))
    }
}