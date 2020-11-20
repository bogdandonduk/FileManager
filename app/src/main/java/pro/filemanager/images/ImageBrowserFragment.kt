package pro.filemanager.images

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import pro.filemanager.ApplicationLoader
import pro.filemanager.HomeActivity
import pro.filemanager.R
import pro.filemanager.core.SimpleInjector
import pro.filemanager.core.UIManager
import pro.filemanager.core.tools.SelectionTool
import pro.filemanager.databinding.FragmentImageBrowserBinding
import pro.filemanager.images.albums.ImageAlbumsFragment
import pro.filemanager.images.gallery.ImageGalleryAdapter
import pro.filemanager.images.gallery.ImageGalleryFragment
import java.lang.IllegalStateException

class ImageBrowserFragment : Fragment(), Observer<MutableList<Fragment>> {

    lateinit var binding: FragmentImageBrowserBinding
    lateinit var activity: HomeActivity
    lateinit var viewModel: ImageBrowserViewModel
    var mainPagerAdapter: ImageBrowserPagerAdapter? = null

    val IOScope = CoroutineScope(Dispatchers.IO)
    val MainScope = CoroutineScope(Main)

    override fun onChanged(t: MutableList<Fragment>?) {
        mainPagerAdapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity = requireActivity() as HomeActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageBrowserBinding.inflate(inflater, container, false)

        activity.requestExternalStoragePermission {

            IOScope.launch {
                viewModel = ViewModelProviders.of(this@ImageBrowserFragment, SimpleInjector.provideImageBrowserViewModelFactory()).get(ImageBrowserViewModel::class.java)

                withContext(Main) {
                    viewModel.getPagerFragmentsLive().observe(viewLifecycleOwner, this@ImageBrowserFragment)

                    initPagerAdapter(viewModel.getPagerFragmentsLive().value!!)
                }

            }

            ApplicationLoader.ApplicationIOScope.launch {
                ApplicationLoader.loadImages()
                ApplicationLoader.loadVideos()
                ApplicationLoader.findExternalRoots()
                ApplicationLoader.loadDocs()
                ApplicationLoader.loadAudios()
            }
        }

        binding.fragmentImageBrowserPager.isUserInputEnabled = false

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity.setSupportActionBar(binding.fragmentImageBrowserLayoutBaseToolbarInclude.layoutBaseToolbar)
        activity.supportActionBar?.title = requireContext().resources.getString(R.string.title_images)

    }

    private fun initPagerAdapter(fragments: MutableList<Fragment>) {
        mainPagerAdapter = ImageBrowserPagerAdapter(fragments, requireActivity().supportFragmentManager, lifecycle)

        binding.fragmentImageBrowserPager.adapter = mainPagerAdapter

        TabLayoutMediator(binding.fragmentImageBrowserTabLayout, binding.fragmentImageBrowserPager) { tab: TabLayout.Tab, i: Int ->
            when(i) {
                0 -> tab.text = requireContext().getString(R.string.title_image_gallery)
                1 -> tab.text = requireContext().getString(R.string.title_image_albums)
            }
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()

        IOScope.cancel()
        MainScope.cancel()
    }

}