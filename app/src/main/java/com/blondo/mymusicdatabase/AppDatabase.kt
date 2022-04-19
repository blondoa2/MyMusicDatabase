package com.blondo.mymusicdatabase

import android.content.Context
import android.graphics.Picture
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteDatabase
import java.sql.RowId

@Entity
data class Music( //makes more sense as "release", but that's a reserved word
    @PrimaryKey(autoGenerate = true) val music_id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val releaseType: String?,
    @ColumnInfo val releaseDate: String?,
    @ColumnInfo val discoveryDate: String?,
    @ColumnInfo val link: String?,
    @ColumnInfo val favorite: Boolean?,
    @ColumnInfo val rating: Int?,
    @ColumnInfo val notes: String?,
    @ColumnInfo val dataFromDiscogs: Boolean
)

@Dao
interface MusicDao {
    @Query("SELECT * FROM music")
    fun getAll(): List<Music>

    @Query("SELECT * FROM music WHERE music_id= :id")
    fun getFromId(id: Int): Music

    @Transaction
    @Query("SELECT * FROM music")
    fun getMusicWithArtists(): List<MusicWithArtists>

    @Transaction
    @Query("SELECT * FROM music")
    fun getMusicWithGenres(): List<MusicWithGenres>

    @Transaction
    @Query("SELECT * FROM music")
    fun getMusicWithArtistsWithGenres(): List<MusicWithArtistsWithGenres>

    @Transaction
    @Query("SELECT * FROM MusicArtistCrossRef WHERE music_id= :id")
    fun getMusicArtistsWithMusicID(id: Int): List<MusicArtistCrossRef>

    fun getArtistsWithMusicId(id: Int): String
    {
        val crossRefs = getMusicArtistsWithMusicID(id)
        var artists: String = ""
        var first = true

        for (i in crossRefs)
        {
            if (first)
            {
                artists += i.artist_name
                first = false
            }
            else
            {
                artists += "\n" + i.artist_name
            }
        }

        return artists
    }

    @Transaction
    @Query("SELECT * FROM MusicGenreCrossRef WHERE music_id= :id")
    fun getMusicGenresWithMusicID(id: Int): List<MusicGenreCrossRef>

    fun getGenresWithMusicId(id: Int): String
    {
        val crossRefs = getMusicGenresWithMusicID(id)
        var genres: String = ""
        var first = true

        for (i in crossRefs)
        {
            if (first)
            {
                genres += i.genre_id
                first = false
            }
            else
            {
                genres += "\n" + i.genre_id
            }
        }

        return genres
    }

    @Insert
    fun insertAll(vararg musics: Music)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(music: Music): Long

    @Insert
    fun insertArtistCrossRef(musicArtistCrossRef: MusicArtistCrossRef)

    @Delete
    fun deleteArtistCrossRef(musicArtistCrossRef: MusicArtistCrossRef)

    @Transaction
    fun updateArtists(musicID: Int, newArtists: List<String>)
    {
        //delete old artists
        val oldArtists = getMusicArtistsWithMusicID(musicID)
        for (i in oldArtists) deleteArtistCrossRef(i)

        //insert new artists
        for (j in newArtists)
        {
            var newArtist = Artist(j, null)
            val newCrossRef = MusicArtistCrossRef(musicID, newArtist.artist_name, "Full")
            insertArtistCrossRef(newCrossRef)
        }
    }

    @Insert
    fun insertGenreCrossRef(musicGenreCrossRef: MusicGenreCrossRef)

    @Delete
    fun deleteGenreCrossRef(musicGenreCrossRef: MusicGenreCrossRef)

    @Transaction
    fun updateGenres(musicID: Int, newGenres: List<String>)
    {
        //delete old genres
        val oldGenres = getMusicGenresWithMusicID(musicID)
        for (i in oldGenres) deleteGenreCrossRef(i)

        //insert new genres
        for (j in newGenres)
        {
            var newGenre = Genre(j)
            val newCrossRef = MusicGenreCrossRef(musicID, newGenre.genre_id)
            insertGenreCrossRef(newCrossRef)
        }
    }

    @Update
    fun update(music: Music)

    @Delete
    fun delete(music: Music)
}

@Entity
data class Genre(
    @PrimaryKey val genre_id: String
)
@Dao
interface GenreDao {
    @Query("SELECT * FROM genre")
    fun getAll(): List<Genre>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg genres: Genre)

    @Delete
    fun delete(genre: Genre)
}

@Entity
data class Artist(
    @PrimaryKey val artist_name: String,
    @ColumnInfo val country: String?
)
@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist")
    fun getAll(): List<Artist>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg artists: Artist)

    @Delete
    fun delete(artist: Artist)
}

//Music_Artist
@Entity(primaryKeys = ["music_id", "artist_name"])
data class MusicArtistCrossRef(
    @ColumnInfo val music_id: Int,
    @ColumnInfo val artist_name: String,
    @ColumnInfo val relationship: String?
)
data class MusicWithArtists(
    @Embedded val music: Music,
    @Relation(
        parentColumn = "music_id",
        entityColumn = "artist_name",
        associateBy = Junction(MusicArtistCrossRef::class)
    )
    val artists: List<Artist>
)

//Music_Genre
@Entity(primaryKeys = ["music_id", "genre_id"])
data class MusicGenreCrossRef(
    @ColumnInfo val music_id: Int,
    @ColumnInfo val genre_id: String
)
data class MusicWithGenres(
    @Embedded val music: Music,
    @Relation(
        parentColumn = "music_id",
        entityColumn = "genre_id",
        associateBy = Junction(MusicGenreCrossRef::class)
    )
    val genres: List<Genre>
)

data class MusicWithArtistsWithGenres(
    @Embedded val musicWithArtists: MusicWithArtists,
    @Relation(
        parentColumn = "music_id",
        entityColumn = "genre_id",
        associateBy = Junction(MusicGenreCrossRef::class)
    )
    val genres: List<Genre>
)

//Genre_Genre
@Entity(primaryKeys = ["parent", "child"])
data class GenreGenreCrossRef(
    @ColumnInfo val parent: String,
    @ColumnInfo val child: String
)

//todo: add compositions
//@Entity
//data class Composition(
//    @PrimaryKey(autoGenerate = true) val id: Int,
//    @ColumnInfo val name: String,
//    @ColumnInfo val releaseDate: String,
//    @ColumnInfo val discoveryDate: String
//)
//@Dao
//interface CompositionDao {
//    @Query("SELECT * FROM composition")
//    fun getAll(): List<Composition>
//
//    @Insert
//    fun insertAll(vararg compositions: Composition)
//
//    @Delete
//    fun delete(composition: Composition)
//}
//
//@Entity(primaryKeys = ["music_id", "composition_id"])
//data class MusicCompositionCrossRef(
//    @ColumnInfo val music_id: Integer,
//    @ColumnInfo val composition_id: Integer
//)

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE GENRE RENAME COLUMN name TO genre_id")
    }
}

@Database(entities = [Music::class, Genre::class, Artist::class, MusicArtistCrossRef::class, MusicGenreCrossRef::class, GenreGenreCrossRef::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
    abstract fun genreDao(): GenreDao
    abstract fun artistDao(): ArtistDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "music.db").addMigrations(MIGRATION_1_2).build()
                }
            }
            return INSTANCE
        }

        fun closeDown(context: Context) {
            INSTANCE?.close()
        }
    }
}
