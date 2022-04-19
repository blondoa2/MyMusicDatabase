package com.blondo.mymusicdatabase.ui.home

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.blondo.mymusicdatabase.*
import com.blondo.mymusicdatabase.databinding.FragmentHomeBinding
import com.blondo.mymusicdatabase.ui.MusicCard
import kotlin.concurrent.thread

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var musicWithInfo: List<MusicWithArtistsWithGenres> = emptyList()
    private var applicationContext: Context? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        applicationContext = activity?.applicationContext


        root.findViewById<Button>(R.id.filterButton).setOnClickListener{displayAll()}
        root.findViewById<EditText>(R.id.input_filter_txt).setOnKeyListener{ v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                displayAll()
                true
            }
            false
        }

        val filters = resources.getStringArray(R.array.filters)
        val spinner = root.findViewById<Spinner>(R.id.filterSpinner)
        if (spinner != null)
        {
            val adapter = ArrayAdapter(applicationContext!!, R.layout.spinner_item, filters)
            spinner.adapter = adapter
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        closeKeyboard()
        loadData()
    }

    private fun loadData() {

        thread {
            val music: MusicDao = AppDatabase.getInstance(applicationContext!!)?.musicDao()!!
            musicWithInfo = music.getMusicWithArtistsWithGenres()
            displayAll()
        }
    }

    //only call directly if you already know that the database hasn't been updated yet. Use loadData() to update & display
    private fun displayAll() {

        val root: View = binding.root
        val homeLinLay: LinearLayout? = root.findViewById(R.id.HomeLinearLayout)

        closeKeyboard()

        activity?.runOnUiThread {
            homeLinLay?.removeAllViews()
        }

        thread {

            //obtain filter controls
            val filter: String = root.findViewById<EditText>(R.id.input_filter_txt).text.toString()
            val filterType: String = root.findViewById<Spinner>(R.id.filterSpinner).getSelectedItem().toString()

            //copy database
            var music = musicWithInfo

            //filter music as needed
            if (filterType == "Release")
                music = music.filter {it.musicWithArtists.music.name.contains(filter, ignoreCase = true)}
            else if (filterType == "Artist")
                music = music.filter {it.musicWithArtists.artists.any{it.artist_name.contains(filter, ignoreCase = true)}}
            else if (filterType == "Genre")
                music = music.filter {it.genres.any{it.genre_id.contains(filter, true)}}

            //display music
            for (i in music) {

                //get artist string
                var artistOut = ""
                for (j in i.musicWithArtists.artists)
                {
                    artistOut += ", " + j.artist_name
                }
                artistOut = artistOut.removePrefix(", ")

                //create card
                val newCard = MusicCard(applicationContext!!)
                newCard.setValues(i.musicWithArtists.music.music_id, i.musicWithArtists.music.name, artistOut)

                //adds card on ui thread
                activity?.runOnUiThread {
                    homeLinLay?.addView(newCard)
                }
            }
        }
    }

    fun closeKeyboard() {
        val view = activity?.currentFocus

        if (view != null)
        {
            val inputManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(this.view?.windowToken, 0)
        }
    }
}