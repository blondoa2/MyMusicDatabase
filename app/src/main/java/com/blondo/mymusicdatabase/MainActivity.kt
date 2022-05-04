package com.blondo.mymusicdatabase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.sqlite.db.SimpleSQLiteQuery
import com.blondo.mymusicdatabase.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener {
            val intent = Intent(this, NewMusic::class.java)
            startActivity(intent)
        }

        val homeLinLay: LinearLayout? = findViewById(R.id.HomeLinearLayout)

        //todo: implement drawer
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_backup
            ), drawerLayout
        )



        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun backupButton(view: View) {

        thread {
            //db checkpoint
            val dbDao: DBDao = AppDatabase.getInstance(applicationContext!!)?.dbDao()!!
            dbDao.checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"));

            val originalFile =
                File(AppDatabase.getInstance(applicationContext)!!.openHelper.writableDatabase.getPath())
            val newFile = File(applicationContext.getFilesDir().toString() + "/music.db")
            originalFile.copyTo(newFile, true)

            val filePath = File(applicationContext.getFilesDir().parent, "files")
            val myFile = File(filePath, "music.db")
            val contentUri: Uri =
                getUriForFile(applicationContext, "com.blondo.mymusicdatabase.fileprovider", myFile)

            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, contentUri)
                setDataAndType(contentUri, "application/vnd.sqlite3")
            }
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, null))

        }
    }
}