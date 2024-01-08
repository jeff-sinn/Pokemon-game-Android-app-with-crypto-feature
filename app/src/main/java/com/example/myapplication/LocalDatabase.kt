package com.example.myapplication

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LocalDatabase {
    companion object {
        private var instance: LocalDatabase? = null

        @Synchronized
        fun getInstance(): LocalDatabase {
            if (instance == null) {
                instance = LocalDatabase()
            }
            return instance!!
        }
    }


    data class User
        (var userID: String, var username: String, var password: String, var e_id: String){
            var currency: Int = 0
            var bank: Int = 0
        }


    private var currentUser: User? = User("0", "dummy", "dummypassword", "0")
    fun postUserID(userID: String) {
        currentUser?.userID = userID
    }

    fun postUsername(username: String) {
        currentUser?.username = username
    }

    fun postPassword(password: String) {
        currentUser?.password = password
    }
    fun postEID(eid: String) {
        currentUser?.e_id=eid
    }
    fun postCurrency(currency: Int){
        currentUser?.currency = currency
    }

    fun postBank(bank: Int){
        currentUser?.bank = bank
    }

    fun getUserID(): String? {
        return currentUser?.userID
    }

    fun getUsername(): String? {
        return currentUser?.username
    }

    fun getPassword(): String? {
        return currentUser?.password
    }
    fun getEID(): String? {
        return currentUser?.e_id
    }
    fun getBank(): Int?{
        return currentUser?.bank
    }
    fun getCurrency(): Int?{
        return currentUser?.currency
    }

    fun bankToCurrency(){
        currentUser!!.currency += currentUser!!.bank
        currentUser!!.bank = 0
    }
}
