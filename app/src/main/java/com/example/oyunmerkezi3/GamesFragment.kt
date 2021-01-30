package com.example.oyunmerkezi3

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.widget.ListView
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.oyunmerkezi3.bottomSheet.GamePriceAdapter
import com.example.oyunmerkezi3.database.GamesViewModel
import com.example.oyunmerkezi3.databinding.FragmentGamesBinding
import com.example.oyunmerkezi3.recycling.GameAdapter
import com.example.oyunmerkezi3.recycling.GameListener
import com.example.oyunmerkezi3.service.NotificationService
import com.example.oyunmerkezi3.utils.GameFilter
import com.example.oyunmerkezi3.utils.NotificationTask
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip

class GamesFragment : Fragment() {
    private lateinit var adapter1: GamePriceAdapter
    private lateinit var adapter2: GamePriceAdapter
    private lateinit var adapter: GameAdapter
    private lateinit var gamesViewModel:GamesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding: FragmentGamesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_games, container, false
        )

        //TODO do not call this every time we start the application
        // only if the service has been killed

        //notification and service
        val showNotification = Intent(requireContext(), NotificationService::class.java)
        showNotification.action = NotificationTask().actionShowNotification
        NotificationService.enqueueWork(requireContext(), showNotification)

        val activity: MainActivity = activity as MainActivity
        gamesViewModel = activity.gamesViewModel
        val platformSharedPreferences = activity.platformSharedPreferences
        val editor: SharedPreferences.Editor = activity.editor

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.priceBottomSheet.parentView)
        val soldGameListView = binding.priceBottomSheet.soldListView
        val boughtGameListView = binding.priceBottomSheet.boughtListView

        bottomSheetFunction(bottomSheetBehavior, soldGameListView, boughtGameListView, activity, binding, container)

        performFiltering()

        inflateChips(platformSharedPreferences, binding, editor)

        sendWhatsAppMessage(binding)

        adapter =
            GameAdapter(
                GameListener { game -> gamesViewModel.onGameClicked(game) },
                gamesViewModel,
                binding.priceBottomSheet.root
            )
        binding.gameList.adapter = adapter
        gamesViewModel.games2.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })
        gamesViewModel.totalPriceLiveData.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.priceBottomSheet.total.text = it.toString()
            }
        })
        binding.priceBottomSheet.clearImageButton.setOnClickListener{
            gamesViewModel.clearTheListOfSelectedGame()
            soldGameListView.adapter = null
            boughtGameListView.adapter = null

        }

        //observing the navigateToDetails value in gamesViewModel to navigate to detail view when clicking on a game
        gamesViewModel.navigateToDetails.observe(viewLifecycleOwner, Observer { game ->
            game?.let {
                this.findNavController().navigate(
                    GamesFragmentDirections
                        .actionGamesFragmentToDetailActivity(game)
                )
                gamesViewModel.onGameDetailsNavigated()
            }
        })

        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        return binding.root
    }

    private fun inflateChips(
        platformSharedPreferences: SharedPreferences,
        binding: FragmentGamesBinding,
        editor: SharedPreferences.Editor
    ) {
        val platformsArray: Array<String> = resources.getStringArray(R.array.platforms)
        val activePlatforms = arrayListOf<String>()
        platformSharedPreferences.let { it1 ->
            for (item in platformsArray) {
                if (it1.getBoolean(item, true))
                    activePlatforms.add(item)
            }
        }
        val chipGroup = binding.platformList
        val inflater2 = LayoutInflater.from(chipGroup.context)
        val currentPlatform = platformSharedPreferences.getString("current", "PS4")

        val children: List<Chip>
        if (activePlatforms.size > 1) {
            children = activePlatforms.map { regionName ->
                val chip = inflater2.inflate(R.layout.platform, chipGroup, false) as Chip
                chip.text = regionName
                chip.tag = regionName
                chipGroup.addView(chip)
                if (regionName == currentPlatform) {
                    chip.isChecked = true
                }
                chip.setOnCheckedChangeListener { button, isChecked ->
                    if (isChecked) {
                        editor.putString("current", button.text as String)
                        editor.apply()
                        gamesViewModel.onSelectedPlatformChange(button.text as String)
                        gamesViewModel.games2.observe(viewLifecycleOwner, Observer {
                            it?.let {
                                adapter.submitList(it)
                            }
                        })
                    }
                }
                chip
            }
            chipGroup.removeAllViews()
            for (chip in children) {
                chipGroup.addView(chip)
            }
        }
    }

    private fun performFiltering() {
        val filter: GameFilter? = this.arguments?.getParcelable<GameFilter>("filter")
        filter?.let { gamesViewModel.filter(it) }
    }

    private fun bottomSheetFunction(
        bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>,
        listView1: ListView,
        listView2: ListView,
        activity: MainActivity,
        binding: FragmentGamesBinding,
        container: ViewGroup?
    ) {
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //inflate the chosen games in the bottom sheet
                        adapter1 = GamePriceAdapter(
                            requireContext(),
                            gamesViewModel.sellingCheckBoxArray,
                            gamesViewModel,
                            true
                        )
                        listView1.adapter = adapter1
                        adapter2 = GamePriceAdapter(
                            requireContext(),
                            gamesViewModel.buyingCheckBoxArray,
                            gamesViewModel,
                            false
                        )
                        listView2.adapter = adapter2
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {

                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })
        val gesture = GestureDetector(
            activity,
            object : SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onFling(
                    e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (binding.priceBottomSheet.root.visibility == View.VISIBLE) {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                    return super.onFling(e1, e2, velocityX, velocityY)
                }
            })
        container!!.setOnTouchListener { v, event ->
            if (event!!.action == MotionEvent.ACTION_UP)
                v!!.performClick()

            gesture.onTouchEvent(event)
        }
    }



    private fun sendWhatsAppMessage(binding: FragmentGamesBinding) {
        //whatsApp button
        val number = "+905465399410"
        val url = "https://api.whatsapp.com/send?phone=$number"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        if (null == intent.resolveActivity(requireActivity().packageManager)) {
            binding.sendButton.visibility = View.GONE
            //TODO need test by uninstalling whatsapp
        }

        binding.sendButton.setOnClickListener() {
            getShareIntent(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
        val item = menu.findItem(R.id.search)
        val searchView: SearchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                adapter.filter.filter(newText)
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    // Creating our Share Intent
    private fun getShareIntent(intent: Intent) {
        startActivity(intent)
    }

}