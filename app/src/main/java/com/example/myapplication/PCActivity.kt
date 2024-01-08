package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class PCActivity :  AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private var mediaPlayer: MediaPlayer? = null
    private var currentToast: Toast? = null
    val localDatabase = LocalDatabase.getInstance()
    val pokedex = Pokedex().pokedex

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pc)
        //create menu
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.pc_toolbar)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0) // Get the first header view
        val usernameTextView = headerView.findViewById<TextView>(R.id.Username)
        val userIcon = headerView.findViewById<ImageView>(R.id.user_icon)
        userIcon.setImageResource(R.drawable.red_icon)
        val trainername = localDatabase.getUsername()
        usernameTextView.text = "$trainername"
        navigationView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        for(i in 0 .. 5){
            var menuItem = navigationView.menu.getItem(i)
            var s = SpannableString(menuItem.getTitle())
            s.setSpan(ForegroundColorSpan(Color.BLACK), 0, s.length, 0)
            menuItem.setTitle(s)
        }
        navigationView.setItemIconTintList(null)
//        mediaPlayer = MediaPlayer.create(this, R.raw.shop)
//        mediaPlayer?.isLooping = true // This will loop the audio
//        mediaPlayer?.start()

        show_pc_list()
    }

    suspend fun check_box(): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+"/check_pokebox/"+localDatabase.getUserID())
        return response.body()
    }

    public fun show_pc_list(){
        GlobalScope.launch{
            var str = check_box()
            Log.i("yess", str)
            runOnUiThread {
                if (str != "[]") {
                    findViewById<LinearLayout>(R.id.pcList).removeAllViews()
                    val jsonArray = JSONArray(str)
                    val rowCount = jsonArray.length()
                    for (i in 0 until rowCount) {
                        val rowArray = jsonArray.getJSONArray(i)
                        val pokemon_id = Integer.parseInt(rowArray.getString(0))
                        val inflater = LayoutInflater.from(this@PCActivity)
                        val view = inflater.inflate(R.layout.pc_list_item, null)
                        view.findViewById<ImageView>(R.id.icon).setImageResource(pokedex[pokemon_id].icon)
                        view.findViewById<TextView>(R.id.pc_name).text = pokedex[pokemon_id].name
                        view.findViewById<TextView>(R.id.pc_rarity).text = rowArray.getString(1)
                        view.findViewById<TextView>(R.id.pc_amount).text = rowArray.getString(2)
                        val parentLayout = findViewById<LinearLayout>(R.id.pcList)
                        parentLayout.addView(view)
                    }

                }
            }

        }



    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_encounter -> {
//                mediaPlayer?.stop()
//                mediaPlayer?.release()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_pc -> {
                drawerLayout.closeDrawer(GravityCompat.START)

            }
            R.id.nav_bag -> {
                //mediaPlayer?.stop()
                //mediaPlayer?.release()
                val intent = Intent(this, BagActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_shop -> {

                mediaPlayer?.stop()
                mediaPlayer?.release()
                val intent = Intent(this, ShopActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_market -> {
                //mediaPlayer?.stop()
                //mediaPlayer?.release()
                val intent = Intent(this, MarketActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
//            mediaPlayer?.stop()
//            mediaPlayer?.release()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            onBackPressedDispatcher.onBackPressed()
        }

    }
}