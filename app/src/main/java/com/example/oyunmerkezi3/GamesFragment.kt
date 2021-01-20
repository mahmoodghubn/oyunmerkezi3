package com.example.oyunmerkezi3

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.oyunmerkezi3.databinding.FragmentGamesBinding
import com.example.oyunmerkezi3.recycling.GameAdapter
import com.example.oyunmerkezi3.recycling.GameListener
import com.example.oyunmerkezi3.utils.GameFilter
import com.google.android.material.chip.Chip

class GamesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val binding: FragmentGamesBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_games, container, false
        )
        val activity: MainActivity = activity as MainActivity
        val gamesViewModel = activity.gamesViewModel
        val platformSharedPreferences = activity.platformSharedPreferences
        val editor: SharedPreferences.Editor = activity.editor

        val filter: GameFilter? = this.arguments?.getParcelable<GameFilter>("filter")
        filter?.let { gamesViewModel.filter(it) }

        val platformsArray: Array<String> = resources.getStringArray(R.array.platforms)
        val platformsArrayFromSharedPreference = arrayListOf<Pair<String, Boolean>>()

        platformSharedPreferences.let { it1 ->
            for (item in platformsArray) {
                platformsArrayFromSharedPreference.add(Pair(item, it1.getBoolean(item, false)))
            }
        }
        val currentPlatform = platformSharedPreferences.getString("current", "PS4")

        val adapter = GameAdapter(GameListener { game -> gamesViewModel.onGameClicked(game) },gamesViewModel)
        binding.gameList.adapter = adapter
        gamesViewModel.games.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitList(it)
            }
        })

        val chipGroup = binding.platformList
        val inflater2 = LayoutInflater.from(chipGroup.context)
        val activePlatforms = arrayListOf<String>()
        for (item in platformsArrayFromSharedPreference) {
            if (item.second) {
                activePlatforms.add(item.first)
            }
        }
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
                        gamesViewModel.onFilterChanged(button.text as String)
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
        binding.lifecycleOwner = this

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

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
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