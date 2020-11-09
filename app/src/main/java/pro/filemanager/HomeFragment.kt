package pro.filemanager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import pro.filemanager.databinding.FragmentHomeBinding
import pro.filemanager.files.FileManager

class HomeFragment() : Fragment() {

    lateinit var binding: FragmentHomeBinding
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(binding.root)

        binding.fragmentHomeInternalBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf("path" to FileManager.internalRootPath))
        }

        binding.fragmentHomeExternalBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_fileBrowserFragment, bundleOf("path" to FileManager.externalRootPath))
        }

        binding.fragmentHomeAudiosBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_audioBrowserFragment)
        }

        binding.fragmentHomeVideosBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_videoBrowserFragment)
        }

        binding.fragmentHomeDocsBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_docBrowserFragment)
        }

        binding.fragmentHomeImagesBtn.layoutHomeTileRootLayoutContent.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_imageBrowserFragment)
        }

    }

}