data class NPSResponse(
    val total: String,
    val data: List<Park>
)

data class Park(
    val fullName: String,
    val description: String,
    val latitude: String,
    val longitude: String
)
