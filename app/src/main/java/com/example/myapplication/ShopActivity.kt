package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
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
import kotlinx.coroutines.launch
import org.json.JSONArray

class ShopActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    val localDatabase = LocalDatabase.getInstance()

    private var mediaPlayer: MediaPlayer? = null
    val item = listOf("Pokéball", "Greatball", "Ultraball", "Premerball", "Masterball", "Repel", "Amulet Coin", "Shiny Charm")
    val itemPrice = listOf(200, 500, 1500, 20000, 100000, 1500, 1000000, 2500000)
    val itemSprite = listOf(R.drawable.pokeball_1, R.drawable.greatball_1, R.drawable.ultraball_1, R.drawable.premierball_1, R.drawable.masterball_1, R.drawable.repel, R.drawable.amulet_coin, R.drawable.shiny_charm)
    val playerInv = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
    private lateinit var balance: TextView
    private var currentToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop)

        //create menu
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.shop_toolbar)
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

        balance = findViewById<TextView>(R.id.balance)

        GlobalScope.launch {
            val str = check_bag()
            println(str)
            val bag = JSONArray(str)
            for (i in 0..8) playerInv[i] = Integer.parseInt(bag[i].toString())
            runOnUiThread {
                balance.text = playerInv[0].toString()
            }
        }


        mediaPlayer = MediaPlayer.create(this, R.raw.shop)
        mediaPlayer?.isLooping = true // This will loop the audio
        mediaPlayer?.start()


    }
    suspend fun check_bag(): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.check_bag)+localDatabase.getUserID())
        return response.body()
    }
    suspend fun buy(id: String, amount: String): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.shop_buy)+localDatabase.getUserID() + "/" + id + "/" + amount)
        return response.body()
    }

    fun toast_maketext(text: String){
        currentToast?.cancel()
        currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    fun shop_buy(v: View?){
        val id = v?.tag.toString()
        val popupView = layoutInflater.inflate(R.layout.shop_popup, null)

        // Create the dialog
        val builder = AlertDialog.Builder(this)
        builder.setView(popupView)

        // Find views within the pop-up layout
        val itemIcon = popupView.findViewById<ImageView>(R.id.itemIcon)
        val itemName = popupView.findViewById<TextView>(R.id.itemName)
        val decreaseButton = popupView.findViewById<Button>(R.id.decreaseButton)
        val decreaseTen = popupView.findViewById<Button>(R.id.decreaseTen)
        val increaseButton = popupView.findViewById<Button>(R.id.increaseButton)
        val increaseTen = popupView.findViewById<Button>(R.id.increaseTen)
        val quantityText = popupView.findViewById<TextView>(R.id.quantityText)
        val bagQuantitiyBefore = popupView.findViewById<TextView>(R.id.bagQuantityBefore)
        val bagQuantityAfter = popupView.findViewById<TextView>(R.id.bagQuantityAfter)
        val balanceBefore = popupView.findViewById<TextView>(R.id.balanceBefore)
        val balanceAfter = popupView.findViewById<TextView>(R.id.balanceAfter)
        val confirmButton = popupView.findViewById<Button>(R.id.confirmButton)

        val text = "-10"
        val spannable = SpannableString(text)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.NORMAL), 1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        decreaseTen.text = spannable
        itemIcon.setImageResource(itemSprite[Integer.parseInt(id)-1])
        itemName.text = item[Integer.parseInt(id)-1]
        balanceBefore.text = playerInv[0].toString()
        bagQuantitiyBefore.text = playerInv[Integer.parseInt(id)].toString()

        val maxQuantity = playerInv[0]/itemPrice[Integer.parseInt(id)-1]
        println(maxQuantity)
        val minQuantity = if (maxQuantity==0) 0 else 1
        var quantity = minQuantity
        quantityText.text = quantity.toString()
        balanceAfter.text = (playerInv[0]-itemPrice[Integer.parseInt(id)-1]*quantity).toString()
        bagQuantityAfter.text = (playerInv[Integer.parseInt(id)]+quantity).toString()

        val dialog = builder.create()
        dialog.show()

        increaseButton.setOnClickListener {
            if (quantity < maxQuantity) { //max
                quantity++
            }
            else{
                quantity=minQuantity
            }
            quantityText.text = quantity.toString()
            balanceAfter.text = (playerInv[0]-itemPrice[Integer.parseInt(id)-1]*quantity).toString()
            bagQuantityAfter.text = (playerInv[Integer.parseInt(id)]+quantity).toString()
        }

        increaseTen.setOnClickListener {
            if (quantity <maxQuantity-10) { //max
                quantity+=10
            }
            else if (quantity<maxQuantity){
                quantity=maxQuantity
            }
            else{
                quantity= minQuantity
            }
            quantityText.text = quantity.toString()
            balanceAfter.text = (playerInv[0]-itemPrice[Integer.parseInt(id)-1]*quantity).toString()
            bagQuantityAfter.text = (playerInv[Integer.parseInt(id)]+quantity).toString()
        }

        decreaseButton.setOnClickListener {
            if (quantity > minQuantity) {
                quantity--
            }
            else{
                quantity=maxQuantity
            }
            quantityText.text = quantity.toString()
            balanceAfter.text = (playerInv[0]-itemPrice[Integer.parseInt(id)-1]*quantity).toString()
            bagQuantityAfter.text = (playerInv[Integer.parseInt(id)]+quantity).toString()
        }
        decreaseTen.setOnClickListener {
            if (quantity > 10) {
                quantity-=10
            }
            else if (quantity>minQuantity){
                quantity=minQuantity
            }
            else{
                quantity= maxQuantity //max, to be implimented
            }
            quantityText.text = quantity.toString()
            balanceAfter.text = (playerInv[0]-itemPrice[Integer.parseInt(id)-1]*quantity).toString()
            bagQuantityAfter.text = (playerInv[Integer.parseInt(id)]+quantity).toString()
        }

        confirmButton.setOnClickListener {
            if (quantity>0) {
                GlobalScope.launch {
                    try {
                        val str = buy(id, quantity.toString())
                        println(str)
                        val bag = JSONArray(str)
                        for (i in 0..8) playerInv[i] = Integer.parseInt(bag[i].toString())
                        runOnUiThread {
                            balance.text = playerInv[0].toString()

                        }
                    } catch (e: Exception) {
                        // Handle the exception, show an error message, or log it
                        println("Exception occurred: ${e.message}")
                        e.printStackTrace()
                    }
                }
                val itemName = item[Integer.parseInt(id)-1]
                val s = if (quantity>1) "s" else ""
                toast_maketext("You bought $quantity $itemName$s")
            }
            else{
                toast_maketext("You don't have enough coins")
            }
            dialog.dismiss()
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.nav_encounter -> {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
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
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_market -> {
                mediaPlayer?.stop()
                mediaPlayer?.release()
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