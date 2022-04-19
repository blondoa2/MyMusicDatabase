package com.blondo.mymusicdatabase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import java.sql.RowId
import kotlin.concurrent.thread

class NewMusic : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_music)
    }

    fun cancel(view: View) {
        finish()
    }

    fun submit(view: View) {

        try {
            //inputs
            var inReleaseName = findViewById<EditText>(R.id.input_release_txt).text.toString()
            var inReleaseType = findViewById<EditText>(R.id.input_releaseType_txt).text.toString()
            var inLink = findViewById<EditText>(R.id.input_link_txt).text.toString()
            var inReleaseDate = findViewById<EditText>(R.id.input_releaseDate_txt).text.toString()
            var inDiscoveryDate =
                findViewById<EditText>(R.id.input_discoveryDate_txt).text.toString()
            var inNotes = findViewById<EditText>(R.id.input_notes_txt).text.toString()

            thread {
                val music: MusicDao = AppDatabase.getInstance(applicationContext!!)?.musicDao()!!

                var currentRelease: Music = Music(
                    music_id = 0,
                    name = inReleaseName,
                    releaseType = inReleaseType,
                    releaseDate = inReleaseDate,
                    discoveryDate = inDiscoveryDate,
                    link = inLink,
                    favorite = false,
                    rating = null,
                    notes = inNotes,
                    dataFromDiscogs = false
                )

                val newMusicId = music.insert(currentRelease)

                val artist: ArtistDao = AppDatabase.getInstance(applicationContext!!)?.artistDao()!!
                val genre: GenreDao = AppDatabase.getInstance(applicationContext!!)?.genreDao()!!

                //get artists
                var inArtists = findViewById<EditText>(R.id.input_artist_txt).text.toString()
                val listArtists = inArtists.lines()

                //insert artists
                for (i in listArtists) {
                    var newArtist: Artist = Artist(i, null)
                    artist.insertAll(newArtist)

                    var currCrossRef: MusicArtistCrossRef =
                        MusicArtistCrossRef(newMusicId.toInt(), i, "Full")
                    music.insertArtistCrossRef(currCrossRef)
                }

                //get genres
                var inGenres = findViewById<EditText>(R.id.input_genre_txt).text.toString()
                val listGenres = inGenres.lines()

                //insert genres
                for (i in listGenres) {
                    var newGenre: Genre = Genre(i)
                    genre.insertAll(newGenre)

                    var currCrossRef: MusicGenreCrossRef = MusicGenreCrossRef(newMusicId.toInt(), i)
                    music.insertGenreCrossRef(currCrossRef)
                }
            }
        }
        catch(e: Exception) { }

        finish()
    }


}