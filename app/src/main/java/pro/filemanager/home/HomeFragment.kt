package pro.filemanager.home

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import pro.filemanager.R
import pro.filemanager.core.base.BaseFragment
import pro.filemanager.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment() {

    lateinit var binding: FragmentHomeBinding

    override fun launchCore() {
        binding.fragmentHomeSectionsList.layoutManager = LinearLayoutManager(frContext, LinearLayoutManager.HORIZONTAL, false)

        binding.fragmentHomeSectionsList.adapter = HomeSectionsAdapter(HomeCore.getHomeSectionItems(frContext, activity.supportFragmentManager, activity.binding.activityHomeRootDrawerLayout.id), layoutInflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAppBar(frContext, binding.fragmentHomeAppBarInclude.baseToolbar, null, R.drawable.ic_baseline_menu_24, frContext.resources.getString(R.string.menu)) {
            hideKeyboard(frContext, view)
            activity.binding.activityHomeRootDrawerLayout.openDrawer(activity.binding.activityHomeMainNavView, true)
        }

        launchCore()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_app_bar_menu, menu)
    }
}