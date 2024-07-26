package campus.tech.kakao.map.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keywords")
data class Keyword(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val address: String,
    val x: Double,
    val y: Double
)