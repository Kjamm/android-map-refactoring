package campus.tech.kakao.map.repository

import android.content.Context
import android.content.SharedPreferences
import campus.tech.kakao.map.data.DatabaseHelper
import campus.tech.kakao.map.data.Keyword
import campus.tech.kakao.map.data.KakaoApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(private val context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)
    private val db = dbHelper.writableDatabase
    private val sharedPreferences = context.getSharedPreferences("search_prefs", Context.MODE_PRIVATE)

    private val kakaoApiService = KakaoApiClient.createService()

    suspend fun search(query: String): List<Keyword> = withContext(Dispatchers.IO) {
        val response = kakaoApiService.searchPlaces(query).execute()
        if (response.isSuccessful) {
            response.body()?.documents?.map {
                Keyword(it.id.toInt(), it.place_name, it.address_name, it.x.toDouble(), it.y.toDouble())
            } ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun populateInitialData() {
        val dataCategories = listOf("cafe", "pharmacy", "cinema")

        db.beginTransaction()
        for (category in dataCategories) {
            for (i in 1..10) {
                val name = "$category$i"
                val address = "대전 유성구 봉명동 $i"
                if (!isDataExist(name)) {
                    val insertData = "INSERT INTO ${DatabaseHelper.TABLE_NAME} (${DatabaseHelper.COLUMN_NAME}, ${DatabaseHelper.COLUMN_ADDRESS}) " +
                            "VALUES ('$name', '$address')"
                    db.execSQL(insertData)
                }
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
        db.close()
    }

    private fun isDataExist(name: String): Boolean {
        val cursor = db.rawQuery("SELECT 1 FROM ${DatabaseHelper.TABLE_NAME} WHERE ${DatabaseHelper.COLUMN_NAME} = ?", arrayOf(name))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun saveKeywordToPrefs(keyword: Keyword) {
        val savedKeywords = getAllSavedKeywordsFromPrefs().toMutableList()
        savedKeywords.add(0, keyword)
        val editor = sharedPreferences.edit()
        editor.putString("saved_keywords", Gson().toJson(savedKeywords))
        editor.apply()
    }

    fun deleteKeywordFromPrefs(keyword: Keyword) {
        val savedKeywords = getAllSavedKeywordsFromPrefs().toMutableList()
        savedKeywords.remove(keyword)
        val editor = sharedPreferences.edit()
        editor.putString("saved_keywords", Gson().toJson(savedKeywords))
        editor.apply()
    }

    fun getAllSavedKeywordsFromPrefs(): List<Keyword> {
        val savedKeywordsJson = sharedPreferences.getString("saved_keywords", null) ?: return emptyList()
        val type = object : TypeToken<List<Keyword>>() {}.type
        return Gson().fromJson(savedKeywordsJson, type)
    }

    fun saveLastMarkerPosition(latitude: Double, longitude: Double, placeName: String, roadAddressName: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putFloat(PREF_LATITUDE, latitude.toFloat())
            putFloat(PREF_LONGITUDE, longitude.toFloat())
            putString(PREF_PLACE_NAME, placeName)
            putString(PREF_ROAD_ADDRESS_NAME, roadAddressName)
            apply()
        }
    }

    fun loadLastMarkerPosition(): Keyword? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (sharedPreferences.contains(PREF_LATITUDE) && sharedPreferences.contains(PREF_LONGITUDE)) {
            val latitude = sharedPreferences.getFloat(PREF_LATITUDE, 0.0f).toDouble()
            val longitude = sharedPreferences.getFloat(PREF_LONGITUDE, 0.0f).toDouble()
            val placeName = sharedPreferences.getString(PREF_PLACE_NAME, "") ?: ""
            val roadAddressName = sharedPreferences.getString(PREF_ROAD_ADDRESS_NAME, "") ?: ""

            if (placeName.isNotEmpty() && roadAddressName.isNotEmpty()) {
                Keyword(0, placeName, roadAddressName, longitude, latitude)
            } else {
                null
            }
        } else {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "LastMarker"
        private const val PREF_LATITUDE = "lastX"
        private const val PREF_LONGITUDE = "lastY"
        private const val PREF_PLACE_NAME = "lastName"
        private const val PREF_ROAD_ADDRESS_NAME = "lastAdress"
    }
}