import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: NPSApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://developer.nps.gov/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NPSApi::class.java)
    }
}
