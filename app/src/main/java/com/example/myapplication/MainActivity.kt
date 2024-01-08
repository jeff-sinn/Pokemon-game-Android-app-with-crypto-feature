package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout

    val localDatabase = LocalDatabase.getInstance()

    private var mediaPlayer: MediaPlayer? = null
    private var additionalMediaPlayer: MediaPlayer? = null

    private lateinit var sprite: ImageView
    private lateinit var capture: ImageView
    private lateinit var runaway: ImageView

    private lateinit var spawner: ImageView
    private lateinit var spawner2: ImageView
    private lateinit var pokeball: ImageView
    private lateinit var greatball: ImageView
    private lateinit var ultraball: ImageView
    private lateinit var premierball: ImageView
    private lateinit var masterball: ImageView
    private lateinit var pokeballAmount: TextView
    private lateinit var greatballAmount: TextView
    private lateinit var ultraballAmount: TextView
    private lateinit var premierballAmount: TextView
    private lateinit var masterballAmount: TextView
    private var rarity = 0
    private var spriteId = 0
    private var pokemonName = ""
    val pokedex = Pokedex().pokedex


    private lateinit var encounterText: TextView
    private lateinit var rarityText: TextView
    private lateinit var resultText: TextView
    private lateinit var coinText: TextView
    private lateinit var encounterFrame: LinearLayout
    private lateinit var dummyframe: LinearLayout


    private var currentToast: Toast? = null
    val ballAmount = mutableListOf(0, 0, 0, 0, 0)

    //in your OnCreate() method
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.bgm1)
        mediaPlayer?.isLooping = true // This will loop the audio
        mediaPlayer?.start()

        additionalMediaPlayer = MediaPlayer.create(this, R.raw.capture)

        setContentView(R.layout.activity_main)
        sprite = findViewById<ImageView>(R.id.image1)
        capture = findViewById<ImageView>(R.id.imageView2)
        runaway = findViewById<ImageView>(R.id.imageView)
        spawner = findViewById<ImageView>(R.id.button)
        spawner2 = findViewById<ImageView>(R.id.button2)

        pokeball = findViewById<ImageView>(R.id.pokeball)
        greatball = findViewById<ImageView>(R.id.greatball)
        ultraball = findViewById<ImageView>(R.id.ultraball)
        premierball = findViewById<ImageView>(R.id.premerball)
        masterball = findViewById<ImageView>(R.id.masterball)

        pokeballAmount = findViewById(R.id.pokeballAmount)
        greatballAmount = findViewById(R.id.greatballAmount)
        ultraballAmount = findViewById(R.id.ultraballAmount)
        premierballAmount = findViewById(R.id.premerballAmount)
        masterballAmount = findViewById(R.id.masterballAmount)
        pokeballAmount.text = ""
        greatballAmount.text = ""
        ultraballAmount.text = ""
        premierballAmount.text = ""
        masterballAmount.text = ""

        val pokeballList = listOf(pokeball, greatball, ultraball, premierball, masterball)
        val pokeballAmountList = listOf(pokeballAmount, greatballAmount, ultraballAmount, premierballAmount, masterballAmount)

        encounterText = findViewById<TextView>(R.id.encounterText)
        rarityText = findViewById<TextView>(R.id.rarityText)
        resultText = findViewById<TextView>(R.id.resultText)
        coinText = findViewById<TextView>(R.id.coinText)
        encounterText.text = ""
        resultText.text = ""
        resultText.visibility = View.GONE
        encounterFrame = findViewById<LinearLayout>(R.id.encounterFrame)
        dummyframe = findViewById<LinearLayout>(R.id.placeholder)
        encounterFrame.visibility = View.GONE

        sprite.visibility = View.GONE
        capture.visibility = View.GONE
        runaway.visibility = View.GONE
        spawner.visibility = View.VISIBLE
        spawner2.visibility = View.GONE

        for(ball in pokeballList) ball.visibility = View.GONE
        for(ball in pokeballAmountList) ball.visibility = View.GONE


        //create menu
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.encounter_toolbar)
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

        //LoadCurrency and Bank to LocalDatabase
        GlobalScope.launch {
            var currency = LoadCurrency()
            println("Currency: "+ currency)
            if(currency == "None"){
                LocalDatabase.getInstance().postCurrency(0)
            }else{
                LocalDatabase.getInstance().postCurrency(currency.toInt())
            }

            var bank = LoadBank()
            println("Bank: "+ bank)
            if(bank == "None"){
                LocalDatabase.getInstance().postBank(0)
            }else{
                LocalDatabase.getInstance().postBank(bank.toInt())
            }
        }


    }
    suspend fun LoadCurrency() : String{
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.get_currency) + LocalDatabase.getInstance().getUserID())
        println("LOADCURRENCY: " + response.body())
        return response.body()
    }
    suspend fun LoadBank() : String{
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.get_bank) + LocalDatabase.getInstance().getUserID())
        println("LOADBANK: " + response.body())
        if(response.body<String>() == "None"){
            return "0"
        }else{
            return response.body()
        }

    }
    suspend fun encounter(): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.encounter)+localDatabase.getUserID())
        if(response.body<String>() == "None"){
            return "0"
        }else{
            return response.body()
        }
        return response.body()
    }
    suspend fun check_bag(): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.check_bag)+localDatabase.getUserID())
        return response.body()
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    fun spawn(v: View?) {
        GlobalScope.launch {
            val str = encounter()
            println(str)
            val parts = str.trim().split("\\s+".toRegex())
            localDatabase.postEID(parts[0])
            rarity = Integer.parseInt(parts[1])
            spriteId = Integer.parseInt(parts[2])
            pokemonName = pokedex[spriteId].name

            val bag = check_bag()
            println(bag)
            val jsonArray = JSONArray(bag)
            for (i in 0..4) ballAmount[i] = Integer.parseInt(jsonArray[i+1].toString())
            println(ballAmount)

            runOnUiThread {
                encounterFrame.foreground = if(rarity==0) resources.getDrawable(R.drawable.common_frame) else if(rarity==1) resources.getDrawable(R.drawable.uncommon_frame) else if(rarity==2) resources.getDrawable(R.drawable.rare_frame) else if(rarity==3) resources.getDrawable(R.drawable.epic_frame) else resources.getDrawable(R.drawable.legendary_frame)
                val pokemonSprite = pokedex[spriteId].sprite
                println(rarity)
                println(spriteId)
                val trainerName = localDatabase.getUsername()
                val t = "$trainerName found a wild $pokemonName"
                encounterText.text = t
                sprite.setImageResource(pokemonSprite)
                dummyframe.visibility= View.GONE
                encounterFrame.visibility = View.VISIBLE
                sprite.visibility = View.VISIBLE
                capture.visibility = View.GONE
                runaway.visibility = View.GONE
                spawner.visibility = View.GONE
                spawner2.visibility = View.GONE
                resultText.visibility = View.GONE
                coinText.visibility = View.GONE

                val pokeballList = listOf(pokeball, greatball, ultraball, premierball, masterball)
                val pokeballAmountList = listOf(pokeballAmount, greatballAmount, ultraballAmount, premierballAmount, masterballAmount)
                for(i in 0..4) {
                    pokeballList[i].visibility = View.VISIBLE
                    pokeballList[i].isClickable = true
                    pokeballAmountList[i].text = ballAmount[i].toString()
                    pokeballAmountList[i].visibility = View.VISIBLE

                    pokeballList[i].setOnClickListener(object : View.OnClickListener {
                            override fun onClick(v: View) {
                                if (ballAmount[i]>0) {
                                    capture(v)
                                }
                                else {
                                    no_pokeball(v)
                                }
                            }
                        })
                }

            }
        }
        spawner.isClickable = false
        spawner2.isClickable = false
    }


    suspend fun catch(ball:String): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.capture)+localDatabase.getEID()+"/"+ball)
        return response.body()
    }
    fun changeAdditionalAudio(newAudioResource: Int) {
        additionalMediaPlayer?.release()
        additionalMediaPlayer = MediaPlayer.create(this, newAudioResource)
        additionalMediaPlayer?.start()
    }
    fun no_pokeball(v: View?){
        val ball = v?.tag.toString()
        currentToast?.cancel()
        currentToast = Toast.makeText(this, "You don't have any $ball", Toast.LENGTH_SHORT)
        currentToast?.show()
    }
    fun capture(v: View?){
        val pbList = listOf(pokeball, greatball, ultraball, premierball, masterball)
        for(balls in pbList) balls.isClickable = false

        val ball = v?.tag.toString()
        var b = ball
        if (b=="Pokéball") b = "Pokeball"
        GlobalScope.launch {
            val str = catch(b)
            val jsonArray = JSONArray(str)
            val coinEarned = Integer.parseInt(jsonArray[0].toString())
            val roll = jsonArray[1].toString()
            val catchRate = jsonArray[2].toString()

            val bag = check_bag()
            val inv = JSONArray(bag)
            val bal = Integer.parseInt(inv[0].toString())
            for (i in 0..4) ballAmount[i] = Integer.parseInt(inv[i+1].toString())
            println(ballAmount)


            runOnUiThread {
                sprite.visibility = View.GONE
                if(coinEarned>0){
                    changeAdditionalAudio(R.raw.capture)
                    capture.visibility = View.VISIBLE
                    val trainerName = localDatabase.getUsername()
                    val t1 = "$trainerName caught a $pokemonName with an $ball!"
                    val t2 = "You earned\n$$coinEarned"
                    encounterText.text = t1
                    resultText.text = t2
                    resultText.visibility = View.VISIBLE
                    println(t1)
                    println(t2)
                }
                else {
                    runaway.visibility = View.VISIBLE
                    val t1 = "$pokemonName broke out of the $ball!"
                    val t2 = "Pokemon roll: $roll\nYour catch rate: $catchRate"
                    encounterText.text = t1
                    resultText.text = t2
                    resultText.visibility = View.VISIBLE
                    println(t1)
                    println(t2)
                }
                val t3 = "You currently have\n$$bal"
                println(t3)
                coinText.text = t3
                coinText.visibility = View.VISIBLE
                val pokeballList = listOf(pokeball, greatball, ultraball, premierball, masterball)
                for(balls in pokeballList) balls.visibility = View.GONE
                val pokeballAmountList = listOf(pokeballAmount, greatballAmount, ultraballAmount, premierballAmount, masterballAmount)
                for(balls in pokeballAmountList) balls.visibility = View.GONE

                spawner2.isClickable = true
                spawner2.visibility = View.VISIBLE
            }
        }

    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_encounter -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_pc -> {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                val intent = Intent(this, PCActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_bag -> {
                mediaPlayer?.stop()
                mediaPlayer?.release()
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
                mediaPlayer?.stop()
                mediaPlayer?.release()
                val intent = Intent(this, MarketActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                mediaPlayer?.stop()
                mediaPlayer?.release()
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

