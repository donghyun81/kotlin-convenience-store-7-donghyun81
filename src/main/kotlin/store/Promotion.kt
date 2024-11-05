package store

data class Promotion(
    val name: String,
    val buy: Int,
    val get: Int,
    val startDate: String,
    val endDate: String
)