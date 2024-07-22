package campus.tech.kakao.map.data

data class KakaoSearchResponse(
    val documents: List<Document>
)

data class Document(
    val id: String,
    val place_name: String,
    val address_name: String,
    val x: Double,
    val y: Double
)