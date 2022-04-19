package com.blondo.mymusicdatabase.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.marginLeft
import com.blondo.mymusicdatabase.NewMusic
import com.blondo.mymusicdatabase.R
import com.blondo.mymusicdatabase.ViewMusic

class MusicCard(context: Context) : CardView(context){

    private var musicName: String? = null
    private var artistName: String? = null
    private var musicID: Int? = null

    private val padUD: Int = 10
    private val padLR: Int = 20
    private val musictxt: TextView = TextView(context)
    private val artisttxt: TextView = TextView(context)
    private val cardLin: LinearLayout = LinearLayout(context)

    init {

        this.setOnClickListener { clicked() }

        this.useCompatPadding = true
    }

    fun setValues(id: Int, music: String, artist: String)
    {
        musicID = id
        musicName = music
        artistName = artist

        musictxt.text = musicName
        musictxt.setTypeface(null, Typeface.BOLD)
        musictxt.textSize = 30f
        musictxt.setPadding(padLR, padUD, padLR, padUD)

        artisttxt.text = artistName
        artisttxt.textSize = 20f
        artisttxt.setPadding(padLR, 0, padLR, padUD)

        cardLin.orientation = LinearLayout.VERTICAL
        cardLin.addView(musictxt)
        cardLin.addView(artisttxt)
    }

    override fun onDraw(canvas: Canvas)
    {
        removeView(cardLin)
        this.addView(cardLin)
    }

    fun clicked()
    {
        val intent = Intent(context, ViewMusic::class.java)
        intent.putExtra("id", musicID)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}