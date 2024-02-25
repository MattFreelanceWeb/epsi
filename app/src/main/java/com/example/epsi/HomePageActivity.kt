package com.example.epsi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class PokemonResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<Pokemon>
)

data class Pokemon(
    val number: Int,
    val name: String,
    val url: String,
    var isCaptured: Boolean = false
)

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(): Response<PokemonResponse>
}

class HomePageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PokemonAdapter
    private lateinit var service: PokeApiService
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = PokemonAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val logoutButton: Button = findViewById(R.id.btn_logout)

        logoutButton.setOnClickListener {
            // Appel de la fonction de déconnexion
            logout()
        }
        service = retrofit.create(PokeApiService::class.java)

        getPokemonListFromApi()

        adapter.setOnItemClickListener { pokemon ->
            val intent = Intent(this, PokemonDetailActivity::class.java)
            intent.putExtra("POKEMON_URL", pokemon.url) // Transmettre l'URL du Pokémon
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getPokemonListFromApi()
    }

    private fun logout() {
        // Déconnexion de l'utilisateur
        FirebaseAuth.getInstance().signOut()

        // Rediriger vers l'activité de connexion (ou une autre activité)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        // Terminer l'activité actuelle pour empêcher l'utilisateur de revenir en arrière
        finish()
    }

    private fun getPokemonListFromApi() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getPokemonList()
                if (response.isSuccessful) {
                    val pokemonList = response.body()?.results?.map { Pokemon(it.number,it.name, it.url) }
                    fetchPokemonDataFromFirebase(pokemonList ?: emptyList())
                } else {
                    showToast("Failed to fetch Pokemon list")
                }
            } catch (e: Exception) {
                showToast("Error: ${e.message}")
            }
        }
    }

    private suspend fun fetchPokemonDataFromFirebase(pokemonList: List<Pokemon>) {
        val userUid = FirebaseAuth.getInstance().currentUser?.uid
        if (userUid != null) {
            for (pokemon in pokemonList) {
                try {
                    val querySnapshot = firestore.collection("capturedPokemons")
                        .whereEqualTo("pokemonName", pokemon.name)
                        .whereEqualTo("userId", userUid)
                        .get()
                        .await()

                    pokemon.isCaptured = !querySnapshot.isEmpty
                } catch (e: Exception) {
                    showToast("Error fetching Pokemon data: ${e.message}")
                }
            }
            updateRecyclerView(pokemonList)
        }
    }

    private fun updateRecyclerView(pokemonList: List<Pokemon>) {
        runOnUiThread {
            adapter.setData(pokemonList)
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@HomePageActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}

class PokemonAdapter : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    private var pokemonList = listOf<Pokemon>()
    private var onItemClick: ((Pokemon) -> Unit)? = null

    fun setData(newPokemonList: List<Pokemon>) {
        pokemonList = newPokemonList
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (Pokemon) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pokemon, parent, false)
        return PokemonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val pokemon = pokemonList[position]
        holder.bind(pokemon, position) // Passer la position à la méthode bind

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(pokemon)
        }
    }

    override fun getItemCount(): Int = pokemonList.size



    inner class PokemonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNumber: TextView = itemView.findViewById(R.id.textViewNumber)
        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        private val imageViewPokeball: ImageView = itemView.findViewById(R.id.imageViewPokeball)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(pokemonList[adapterPosition])
            }
        }

        fun bind(pokemon: Pokemon, position: Int) {
            val pokemonNumber = position + 1 // Ajouter 1 pour obtenir le numéro de position réel
            textViewName.text = "#$pokemonNumber ${pokemon.name}"
            val imageSize = calculateImageSize(textViewName.textSize)
            val layoutParams = imageViewPokeball.layoutParams
            layoutParams.width = imageSize
            layoutParams.height = imageSize
            imageViewPokeball.layoutParams = layoutParams

            if (pokemon.isCaptured) {
                imageViewPokeball.visibility = View.VISIBLE
            } else {
                imageViewPokeball.visibility = View.GONE
            }
        }

        private fun calculateImageSize(textSize: Float): Int {
            // Convertir la taille du texte en pixels
            val textSizePixels = (textSize * itemView.context.resources.displayMetrics.density).toInt()
            // La taille de l'image sera égale à la taille du texte
            return textSizePixels
        }
    }

}
