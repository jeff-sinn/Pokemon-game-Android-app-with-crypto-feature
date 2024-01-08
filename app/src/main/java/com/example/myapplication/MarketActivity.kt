package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
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


class MarketItem(
    val offerUserID: Int,
    val offerUsername : String,         //offer user id (change variable name)
    val pokemonCard: Int,        //pokemonCard
    val pokemonCost: Int,       //pokemonCost
    var status: String,
    val pokemonCardID: Int
) {
    var pokemonName: String = ""
    init {
        pokemonName = Pokedex().pokedex[pokemonCard].name
    }
}

class MarketList {
    var content = mutableListOf<MarketItem>()

    fun addItem(marketItem: MarketItem) {
        content.add(marketItem)
    }

    fun buyItem(marketItem: MarketItem) {
        println("Change " + marketItem.offerUsername + " 's market item to sold")
        marketItem.status = "Sold"
    }
    fun AddContent(content: MutableList<MarketItem>){
        println("Add to MarketList")
        this.content = content
        for(item in content){
            println("-- MaketList item: " + item.offerUsername)
        }
    }
}

class PokemonCardWithID(
    val card:Int,
    val id: Int
){}


class MarketActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout

    private var itemID: EditText? = null
    private var itemCost: EditText? = null
    var marketList = MarketList()
    var addItemToMarketBtn: Button? = null
    var addItemToMarketAlert: AlertDialog? = null
    var marketListLayout: LinearLayout? = null
    var ownedPokemonCards = mutableListOf<PokemonCardWithID>()
    val localDatabase = LocalDatabase.getInstance()
    var pokedex = Pokedex().pokedex


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch {
            LoadMarketList()
            LoadPokemonCards()
        }
        setContentView(R.layout.activity_market)


        //create menu
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<Toolbar>(R.id.market_toolbar)
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

        //Get database form local

        //Initialisation

//        itemID = findViewById<EditText>(R.id.itemID)
//        itemCost = findViewById<EditText>(R.id.itemCost)
//        addItemToMarket(this.view, 10, 20)
//        addItemToMarket(10, 30)
//        addItemToMarket(5,15)


        Handler().postDelayed(Runnable {
            //do something
            showMarketList()
            SetPokemonCardSpinner()
        }, 500)

        SetBankValue()

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
                //mediaPlayer?.stop()
                //mediaPlayer?.release()
                val intent = Intent(this, BagActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_shop -> {


                val intent = Intent(this, ShopActivity::class.java)
                startActivity(intent)

            }
            R.id.nav_market -> {
                drawerLayout.closeDrawer(GravityCompat.START)

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
    fun SetPokemonCardSpinner() {
        println("SetPokemonCardSpinner")
        val spinner = findViewById<View>(R.id.market_pokemonCard) as Spinner
        var pokemonCardList = listOf<String>()
        pokemonCardList += ""
        for (ownedPokemonCard in ownedPokemonCards) {

            pokemonCardList += ownedPokemonCard.card.toString() + " - " + pokedex[ownedPokemonCard.card].name
        }
        val adapter = ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item, pokemonCardList)
        spinner.setAdapter(adapter)
    }

    suspend fun  getMarket(): String{
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.get_market_list))

        return response.body()
    }
    suspend fun getPokemon(): String{
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.get_user_pokemon) + LocalDatabase.getInstance().getUserID())

        return response.body()
    }


    suspend fun market(offerUserID: Int,pokemonCard: Int, pokemonCost: Int):String{
        val client = HttpClient()

        val response = client.get(getString(R.string.server_ip)+getString(R.string.market_list)+offerUserID+"/"+pokemonCard+"/"+pokemonCost+"/" + "Listed")

        return response.body()
    }

    fun SaveMarketList(offerUserID: Int,pokemonCard: Int, pokemonCost: Int){
        GlobalScope.launch {
            val str = market(offerUserID, pokemonCard, pokemonCost)
        }
    }

    fun offer(v: View?){

        var pokemonCard = 0
        if(findViewById<Spinner>(R.id.market_pokemonCard).getSelectedItem().toString().split("-")[0].replace(" ","") != ""){
            pokemonCard = findViewById<Spinner>(R.id.market_pokemonCard).getSelectedItem().toString().split("-")[0].replace(" ","").toInt()
        }
        val pokemonCost = findViewById<TextView>(R.id.market_pokemonCost).text.toString().toInt()
        if(pokemonCard != 0 && pokemonCost != 0){
            val popupView = layoutInflater.inflate(R.layout.confirm_offer, null)

            val builder = android.app.AlertDialog.Builder(this)
            builder.setView(popupView)

            val itemIcon = popupView.findViewById<ImageView>(R.id.offer_icon)
            val itemName = popupView.findViewById<TextView>(R.id.offer_name)
            val itemPrice = popupView.findViewById<TextView>(R.id.offer_price)
            val password = popupView.findViewById<EditText>(R.id.offer_password)
            val confirmButton = popupView.findViewById<Button>(R.id.confirmButton)
            itemIcon.setImageResource(pokedex[pokemonCard].icon)
            val name = pokedex[pokemonCard].name
            itemName.text = name
            val price = "$$pokemonCost"
            itemPrice.text = price

            val dialog = builder.create()
            dialog.show()

            confirmButton.setOnClickListener {
                if (password.text.toString()==localDatabase.getPassword()) {
                    toast_maketext("Successfully added $name to the Market!")
                    addItemToMarket(v)
                }
                else{
                    toast_maketext("Incorrect Password")
                }
                dialog.dismiss()
            }
        }

    }
    fun addItemToMarket(v: View?){
        val offerUserID = LocalDatabase.getInstance().getUserID()!!.toInt()
        val offerUsername = LocalDatabase.getInstance().getUsername()!!
        val pokemonCost = findViewById<TextView>(R.id.market_pokemonCost).text.toString().toInt()
        var pokemonCard = 0
        if(findViewById<Spinner>(R.id.market_pokemonCard).getSelectedItem().toString().split("-")[0].replace(" ","") != ""){
            pokemonCard = findViewById<Spinner>(R.id.market_pokemonCard).getSelectedItem().toString().split("-")[0].replace(" ","").toInt()
        }
        var pokemonCardID = 0
        println("AddITEMTOMARKET : "+ findViewById<Spinner>(R.id.market_pokemonCard).getSelectedItem())


        //offerUserName or offerUserID?
//        GlobalScope.launch{
//            showMarketList()
//        }
        if(pokemonCard != 0 && pokemonCost != 0){


            for(ownedPokemonCard in ownedPokemonCards){
                if(ownedPokemonCard.card == pokemonCard){
                    pokemonCardID = ownedPokemonCard.id
                }
            }



            marketList.addItem(MarketItem(offerUserID, offerUsername, pokemonCard,pokemonCost,"Listed", pokemonCardID))
            SaveMarketList(offerUserID, pokemonCardID, pokemonCost)
            LoadPokemonCards()
            println("--- Done Saving" + offerUserID + " -- " + offerUsername + " -- " + pokemonCard + " -- " + pokemonCost + " --  "+ pokemonCardID)


            Handler().postDelayed(Runnable {
                //do something
                showMarketList()
                SetPokemonCardSpinner()
            }, 500)
        }

    }

    fun SetBankValue(){
        println("SetBankValue: " + LocalDatabase.getInstance().getBank().toString())
        findViewById<TextView>(R.id.BankValue).text = LocalDatabase.getInstance().getBank().toString()
    }

    fun AddBankToCurrency(v: View?){
        GlobalScope.launch {
            println("Before, Currency: "+ LocalDatabase.getInstance().getCurrency())
            BankToCurrency()
            LocalDatabase.getInstance().bankToCurrency()
            println("After, Currency: "+ LocalDatabase.getInstance().getCurrency())
        }
        SetBankValue()
    }
    suspend fun BankToCurrency(){
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.bank_to_currency)+LocalDatabase.getInstance().getUserID().toString())

        return response.body()
    }

    public fun LoadMarketList(){
        GlobalScope.launch {
            val marketItems = mutableListOf<MarketItem>()
            val str = getMarket()
            println(str)
            val responseArray = JSONArray(str)
            println(responseArray)
            for (i in 0 until responseArray.length()){
                val jsonObject = responseArray.getJSONObject(i)
                val marketItem = MarketItem(
                    jsonObject.getInt("u_id"),
                    jsonObject.getString("u_username"),
                    jsonObject.getInt("p_id"),
                    jsonObject.getInt("m_price"),
                    jsonObject.getString("m_status"),
                    jsonObject.getInt("pc_id")

                )
                println("LoadMarketList() -> jsonObject m_status: "+ jsonObject.getString("m_status"))
                marketItems.add(marketItem)
            }

            marketList.AddContent(marketItems)
            println("Done Loading Market List")
        }
    }

    public fun LoadPokemonCards(){
        GlobalScope.launch {
            val str = getPokemon()
            println("LoadPokemonCards" + str)
            val responseArray = JSONArray(str)
            ownedPokemonCards.clear()
            for (i in 0 until responseArray.length()){
                val response= responseArray[i].toString().split(",")
                ownedPokemonCards.add(PokemonCardWithID(response[0].toString().replace("[", "").toInt(), response[1].toString().toInt()) )
            }
        }
    }

    public fun showMarketList(){
        findViewById<LinearLayout>(R.id.marketList).removeAllViews()
        findViewById<LinearLayout>(R.id.ListedPokemonLayout).removeAllViews()
        findViewById<LinearLayout>(R.id.claimHeading).visibility = View.GONE
        println("Start showMarketList")

        for(marketitem in marketList.content){
            if(marketitem.status != "Sold" && marketitem.offerUserID != LocalDatabase.getInstance().getUserID()!!.toInt()){
                val inflater = LayoutInflater.from(this)
                val view = inflater.inflate(R.layout.market_list_item, null)
                view.findViewById<ImageView>(R.id.marketItem_icon).setImageResource(pokedex[marketitem.pokemonCard].icon)
                view.findViewById<TextView>(R.id.marketItem_itemCost).text = marketitem.pokemonCost.toString()
                view.findViewById<TextView>(R.id.marketItem_name).text = pokedex[marketitem.pokemonCard].name
                val parentLayout = findViewById<LinearLayout>(R.id.marketList)
                parentLayout.addView(view)
            }

            if(marketitem.status != "Sold" && marketitem.offerUserID == LocalDatabase.getInstance().getUserID()!!.toInt()){
                findViewById<LinearLayout>(R.id.claimHeading).visibility = View.VISIBLE
                val inflater = LayoutInflater.from(this)
                val view = inflater.inflate(R.layout.market_user_listed_item, null)
                view.findViewById<ImageView>(R.id.marketItem_icon).setImageResource(pokedex[marketitem.pokemonCard].icon)
                view.findViewById<TextView>(R.id.marketItem_name).text = pokedex[marketitem.pokemonCard].name
                view.findViewById<TextView>(R.id.marketItem_cost).text = marketitem.pokemonCost.toString()
                val parentLayout = findViewById<LinearLayout>(R.id.ListedPokemonLayout)
                parentLayout.addView(view)
            }
//            view.findViewById<TextView>(R.id.marketItem_offer).text = marketitem.offerUsername
        }

    }
    var currentToast:Toast? = null
    fun toast_maketext(text: String){
        currentToast?.cancel()
        currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    suspend fun AddBankChangeMarket(offerUserID: Int, pokemonCard: Int, pokemonCost: Int, getUserID: Int){
        val client = HttpClient()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.add_bank_change_market) + offerUserID + "/" + pokemonCard + "/" + pokemonCost + "/" + getUserID)

        return response.body()
    }

    fun purchase(v: View?){

        if(v != null){
            val parentView = v.parent as? View
            if (parentView != null) {
                for(marketitem in marketList.content) {
                    if (marketitem.pokemonName == parentView.findViewById<TextView>(R.id.marketItem_name).text.toString()
                        && marketitem.pokemonCost == parentView.findViewById<TextView>(R.id.marketItem_itemCost).text.toString()
                            .toInt()) {
                        val pokemonCost = marketitem.pokemonCost
                        val bal = localDatabase.getCurrency()!!
                        if(bal>=pokemonCost){
                            val popupView = layoutInflater.inflate(R.layout.confirm_purchase, null)

                            val builder = android.app.AlertDialog.Builder(this)
                            builder.setView(popupView)
                            var pokemonID = 0
                            for(i in 0 .. 152){
                                if (pokedex[i].name==marketitem.pokemonName){
                                    pokemonID = i
                                }
                            }
                            val itemIcon = popupView.findViewById<ImageView>(R.id.purchase_icon)
                            val itemName = popupView.findViewById<TextView>(R.id.purchase_name)
                            val itemPrice = popupView.findViewById<TextView>(R.id.purchase_price)
                            val password = popupView.findViewById<EditText>(R.id.purchase_password)
                            val confirmButton = popupView.findViewById<Button>(R.id.confirmButton)
                            itemIcon.setImageResource(pokedex[pokemonID].icon)
                            val name = pokedex[pokemonID].name
                            itemName.text = name
                            val price = "$$pokemonCost"
                            itemPrice.text = price

                            val dialog = builder.create()
                            dialog.show()

                            confirmButton.setOnClickListener {
                                if (password.text.toString()==localDatabase.getPassword()) {
                                    toast_maketext("Successfully purchased $name from the Market!")
                                    market_buy(v)
                                }
                                else{
                                    toast_maketext("Incorrect Password")
                                }
                                dialog.dismiss()
                            }
                        }
                        else{
                            toast_maketext("Not enough money!!!")
                        }

                    }
                }
            }
        }
    }
    fun market_buy(v:View?){
        println("Clicked Purchase")

        if(v != null){
            val parentView = v.parent as? View
            if (parentView != null) {
                for(marketitem in marketList.content) {
                    if (marketitem.pokemonName == parentView.findViewById<TextView>(R.id.marketItem_name).text.toString()
                        && marketitem.pokemonCost == parentView.findViewById<TextView>(R.id.marketItem_itemCost).text.toString()
                            .toInt()
                        && LocalDatabase.getInstance().getCurrency()!! >= parentView.findViewById<TextView>(R.id.marketItem_itemCost).text.toString().toInt()!!
                    ) {

                        marketList.buyItem(marketitem)
                        println("Before buy" + LocalDatabase.getInstance().getCurrency())
                        LocalDatabase.getInstance().postCurrency(LocalDatabase.getInstance().getCurrency()!!.toInt() - parentView.findViewById<TextView>(R.id.marketItem_itemCost).text.toString().toInt()!!)
                        println("After buy" + LocalDatabase.getInstance().getCurrency())
                        GlobalScope.launch {
                            var str = AddBankChangeMarket(marketitem.offerUserID, marketitem.pokemonCardID, marketitem.pokemonCost, LocalDatabase.getInstance().getUserID()!!.toInt())
                        }

                    }
                }
            }
        }
        showMarketList()
//            println((v.parent).findViewById<TextView>(R.id.marketItem_offer).text.toString())
    }

//        if(v!= null){
//            for(marketitem in marketList.content) {
//                if (marketitem.offerUsername == v.findViewById<TextView>(R.id.marketItem_offer).text.toString()
//                    && marketitem.pokemonCard == v.findViewById<TextView>(R.id.marketItem_itemID).text.toString()
//                        .toInt()
//                    && marketitem.pokemonCost == v.findViewById<TextView>(R.id.marketItem_itemCost).text.toString()
//                        .toInt()
//                ) {
//                    if (marketitem.status == "sold") {
//                        //show item have been sold already
//
//                    } else {
//                        marketList.buyItem(marketitem)
//                    }
//                }
//            }
//        }



//
//                break
//            }
//        }
        //Add the pokemon to user

    }

//    fun isPurchaseValid(buyerAcc: Int, marketItem: MarketItem): Boolean {
//        if (checkTransitionValidation(
//                blockChain.chain.last(),
//                Record(buyerAcc, 0, marketItem.itemID, marketItem.itemCost)
//            )
//        ) {
//            println("Transition Valid")
//            print("Enter the buyer's private key: ")
//            val pw = readLine()?.toIntOrNull()
//            if (pw == publicToPrivate(buyerAcc)) {
//                blockChain.chain.last().addRecord(Record(buyerAcc, 0, marketItem.itemID, marketItem.itemCost))
//            } else {
//                return false
//            }
//            return true
//        } else {
//            return false
//        }
//    }

//    fun addCoinToBank(offerAcc: Int, marketItem: MarketItem) {
//        for (user in users) {
//            if (user.publicKey == offerAcc) {
//                user.bank += marketItem.itemCost
//                blockChain.chain.last().addRecord(Record(0, offerAcc, marketItem.itemID, marketItem.itemCost))
//            }
//        }
//    }


//    fun checkBlockFull() {
//        if (blockChain.chain.last().isFull()) {
//            println("\n---New Add a new block since the current block is full\n")
//            val newBlock = Block(blockChain.chain.size)
//            blockChain.addBlock(newBlock)
//        }
//    }
//
//    fun verifyBlock() {
//        for (block in blockChain.chain) {
//            while (!block.isVerified && block.isFull()) {
//                print("Which user are you? ")
//                val userNum = readLine()?.toIntOrNull()
//                if (userNum != null) {
//                    block.guessOrGettingPokemon(users[userNum - 1])
//                }
//
//                println("\n\n\nYou Have successfully become the validator of this block.\n")
//                // verifyAwaitBlockID
//            }
//        }
//    }




