package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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

class BagActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    val localDatabase = LocalDatabase.getInstance()

    private val playerInv = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0, 0)
    private lateinit var pokeball: TextView
    private lateinit var greatball: TextView
    private lateinit var ultraball: TextView
    private lateinit var premierball: TextView
    private lateinit var masterball: TextView
    private lateinit var repel: TextView
    private lateinit var amuletcoin: TextView
    private lateinit var shinycharm: TextView


    private var currentToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bag)

        //create menu
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.bag_toolbar)
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

        pokeball = findViewById<TextView>(R.id.pbAmount)
        greatball = findViewById<TextView>(R.id.gbAmount)
        ultraball = findViewById<TextView>(R.id.ubAmount)
        premierball = findViewById<TextView>(R.id.prbAmount)
        masterball = findViewById<TextView>(R.id.mbAmount)
        repel = findViewById<TextView>(R.id.rAmount)
        amuletcoin = findViewById<TextView>(R.id.acAmount)
        shinycharm = findViewById<TextView>(R.id.scAmount)


        val itemAmount = listOf(pokeball, greatball, ultraball, premierball, masterball, repel, amuletcoin, shinycharm)

        GlobalScope.launch {
            println("Hiiiii")
            val str = check_bag()
            println(str)
            val bag = JSONArray(str)
            println(bag)
            runOnUiThread {
                for (i in 0..7){
                    itemAmount[i].text = bag[i+1].toString()
                    playerInv[i] = Integer.parseInt(bag[i+1].toString())
                }
                playerInv[8] = Integer.parseInt(bag[9].toString())
            }
        }
    }
    suspend fun check_bag(): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.check_bag)+localDatabase.getUserID())
        return response.body()
    }
    fun toast_maketext(text: String){
        currentToast?.cancel()
        currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
    fun description(v: View?){
        val id = Integer.parseInt(v?.tag.toString())
        val popupView = layoutInflater.inflate(R.layout.item_description, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(popupView)

        val itemIcon = popupView.findViewById<ImageView>(R.id.itemIcon)
        val itemName = popupView.findViewById<TextView>(R.id.itemName)
        val descriptionText = popupView.findViewById<TextView>(R.id.descriptionText)
        //val descriptionText = popupView.findViewById<TextView>(R.id.descriptionText)
        val confirmButton = popupView.findViewById<Button>(R.id.confirmButton)
        val nameList = listOf<String>("Pokéball", "Greatball", "Ultraball", "Premerball", "Masterball", "Repel", "Amulet Coin", "Shiny Charm")
        val itemSprite = listOf(R.drawable.pokeball_1, R.drawable.greatball_1, R.drawable.ultraball_1, R.drawable.premierball_1, R.drawable.masterball_1, R.drawable.repel, R.drawable.amulet_coin, R.drawable.shiny_charm)
        val itemDescription = listOf("The most basic form of Pokéball.\nAn item used to catch wild Pokémon.", "An improved variant of the regular Pokéball.\nProvides a higher success rate for catching Pokémon than a standard Pokéball.", "An improved variant of the Greatball\nProvides a higher success rate for catching Pokémon than a Greatball", "A RARE Ultra-High-Performance Pokéball.\nOnly obtainable by purchasing 100 items at once", "The very best Pokéball with the ultimate level of performance.\nGuarantee to catch any wild Pokémon.", "An item that reduce chance of finding common Pokémon for 30 encounters.", "A speical coin that increases the coins earn from catching wild Pokémon by 10%.", "A lucky charm that increases your chance of finding Shiny Pokémon")

        itemIcon.setImageResource(itemSprite[id-1])
        val name = nameList[id-1]
        itemName.text = name
        confirmButton.visibility = if(id==6) View.VISIBLE else View.GONE
        descriptionText.text = itemDescription[id-1]

        val dialog = builder.create()
        dialog.show()

        confirmButton.setOnClickListener {
            if (playerInv[id-1]>0) {
                use_item(v)
            }
            else{
                toast_maketext("You don't have any repels")
            }
            dialog.dismiss()
        }
    }

    suspend fun use_repel(amount: Int): String {
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.use_repel)+localDatabase.getUserID()+"/$amount")
        return response.body()
    }
    fun use_item(v: View?){
        val popupView = layoutInflater.inflate(R.layout.item_use, null)

        // Create the dialog
        val builder = AlertDialog.Builder(this)
        builder.setView(popupView)

        // Find views within the pop-up layout
        val itemIcon = popupView.findViewById<ImageView>(R.id.itemIcon)
        val decreaseButton = popupView.findViewById<Button>(R.id.decreaseButton)
        val decreaseTen = popupView.findViewById<Button>(R.id.decreaseTen)
        val increaseButton = popupView.findViewById<Button>(R.id.increaseButton)
        val increaseTen = popupView.findViewById<Button>(R.id.increaseTen)
        val quantityText = popupView.findViewById<TextView>(R.id.quantityText)
        val bagQuantitiyBefore = popupView.findViewById<TextView>(R.id.bagQuantityBefore)
        val bagQuantityAfter = popupView.findViewById<TextView>(R.id.bagQuantityAfter)
        val effectBefore = popupView.findViewById<TextView>(R.id.balanceBefore)
        val effectAfter = popupView.findViewById<TextView>(R.id.balanceAfter)
        val confirmButton = popupView.findViewById<Button>(R.id.confirmButton)

        val text = "-10"
        val spannable = SpannableString(text)
        spannable.setSpan(StyleSpan(Typeface.BOLD), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.NORMAL), 1, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        decreaseTen.text = spannable
        itemIcon.setImageResource(R.drawable.repel)
        effectBefore.text = playerInv[8].toString()
        bagQuantitiyBefore.text = playerInv[5].toString()
        val maxQuantity = playerInv[5]
        val minQuantity = if (maxQuantity==0) 0 else 1
        var quantity = minQuantity
        var effect = playerInv[8]+quantity*30
        effectAfter.text = "$effect"
        quantityText.text = if (quantity>1) " Use $quantity Repels" else "Use $quantity Repel"
        bagQuantityAfter.text = (playerInv[5]-quantity).toString()

        val dialog = builder.create()
        dialog.show()

        increaseButton.setOnClickListener {
            if (quantity < maxQuantity) { //max
                quantity++
            }
            else{
                quantity=minQuantity
            }
            quantityText.text = if (quantity>1) " Use $quantity Repels" else "Use $quantity Repel"
            effect = playerInv[8]+quantity*30
            effectAfter.text = "$effect"
            bagQuantityAfter.text = (playerInv[5]-quantity).toString()
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
            quantityText.text = if (quantity>1) " Use $quantity Repels" else "Use $quantity Repel"
            effect = playerInv[8]+quantity*30
            effectAfter.text = "$effect"
            bagQuantityAfter.text = (playerInv[5]-quantity).toString()
        }

        decreaseButton.setOnClickListener {
            if (quantity > minQuantity) {
                quantity--
            }
            else{
                quantity=maxQuantity
            }
            quantityText.text = if (quantity>1) " Use $quantity Repels" else "Use $quantity Repel"
            effect = playerInv[8]+quantity*30
            effectAfter.text = "$effect"
            bagQuantityAfter.text = (playerInv[5]-quantity).toString()
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
            quantityText.text = if (quantity>1) " Use $quantity Repels" else "Use $quantity Repel"
            effect = playerInv[8]+quantity*30
            effectAfter.text = "$effect"
            bagQuantityAfter.text = (playerInv[5]-quantity).toString()
        }

        confirmButton.setOnClickListener {
            if (quantity>0) {
                GlobalScope.launch {
                    try {
                        val str = use_repel(quantity)
                        println(str)
                        val bag = JSONArray(str)
                        for (i in 0..8) playerInv[i] = Integer.parseInt(bag[i+1].toString())
                        runOnUiThread {
                            repel.text=playerInv[5].toString()
                        }
                    } catch (e: Exception) {
                        // Handle the exception, show an error message, or log it
                        println("Exception occurred: ${e.message}")
                        e.printStackTrace()
                    }
                }
                val s = if(quantity>1)"s" else ""
                toast_maketext("You will see rarer Pokémon in you next\n$effect encounters")
            }
            else{
                toast_maketext("You did not use Repel")
            }
            dialog.dismiss()
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
                //mediaPlayer?.stop()
                //mediaPlayer?.release()
                val intent = Intent(this, PCActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_bag -> {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            R.id.nav_shop -> {
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