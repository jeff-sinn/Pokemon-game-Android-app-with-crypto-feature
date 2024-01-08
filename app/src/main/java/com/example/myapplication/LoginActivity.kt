package com.example.myapplication

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray

class LoginActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var eevee: ImageView
    private var currentToast: Toast? = null
    val localDatabase = LocalDatabase.getInstance()
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        username = findViewById<EditText>(R.id.username)
        password = findViewById<EditText>(R.id.password)
        eevee = findViewById(R.id.eevee_login)
        password.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                eevee.setImageResource(R.drawable.eevee_eyesclosed)
            } else {
                eevee.setImageResource(R.drawable.eevee_peeking)
            }
        }
        mediaPlayer = MediaPlayer.create(this, R.raw.login_bgm)
        mediaPlayer?.isLooping = true // This will loop the audio
        mediaPlayer?.start()
    }
    suspend fun login_client(): String {
        val client = HttpClient()
        val str =username.text.toString()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.login)+str+"/"+password.text.toString())
        return response.body()
    }
    suspend fun register_client(): String {
        val client = HttpClient()
        val str =username.text.toString()
        val response = client.get(getString(R.string.server_ip)+getString(R.string.register)+str+"/"+password.text.toString())
        return response.body()
    }
    fun toast_maketext(text: String){
        currentToast?.cancel()
        currentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        currentToast?.show()
    }

    fun login(v: View?) {
        GlobalScope.launch {
            val str = login_client()
//            val jsonArray = JSONArray(str)
            runOnUiThread {
                if(str != "Username not exist!" && str != "Password incorrect"){
                    val jsonArray = JSONArray(str)
                    toast_maketext("Welcome back Trainer " + username.text.toString())
                    localDatabase.postUserID(jsonArray[0].toString())
                    localDatabase.postUsername(jsonArray[1].toString())
                    localDatabase.postPassword(jsonArray[2].toString())

                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }
                else{
                    toast_maketext(str)
                }

            }
        }
    }
    fun register(v: View?) {
        GlobalScope.launch {
            val str = register_client()
//            val jsonArray = JSONArray(str)
            runOnUiThread {
                if (str != "Username already exist!") {
                    val jsonArray = JSONArray(str)
                    toast_maketext("Welcome to the world of Pokémon " + username.text.toString())
                    localDatabase.postUserID(jsonArray[0].toString())
                    localDatabase.postUsername(jsonArray[1].toString())
                    localDatabase.postPassword(jsonArray[2].toString())

                    println(localDatabase.getPassword())
                    mediaPlayer?.stop()
                    mediaPlayer?.release()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                }
                else{
                    toast_maketext(str)
                }
            }

        }
    }
}