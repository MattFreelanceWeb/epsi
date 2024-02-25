package com.example.epsi

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.epsi.databinding.ActivityPokemonDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

class PokemonDetailActivity : AppCompatActivity() {

    data class PokemonResponse(
        val count: Int,
        val next: String?,
        val previous: String?,
        val results: List<Pokemon>
    )

    data class Pokemon(
        val name: String,
        val url: String
    )

    data class Sprites(
        val front_default: String?
    )

    data class PokemonDetails(
        val name: String,
        val height: Int,
        val weight: Int,
        val sprites: Sprites
    )

    interface PokeApiService {
        @GET("pokemon")
        suspend fun getPokemonList(): Response<PokemonResponse>

        @GET
        suspend fun getPokemonDetails(@Url url: String): Response<PokemonDetails>
    }

    private lateinit var binding: ActivityPokemonDetailBinding
    private lateinit var service: PokeApiService
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonCapture: Button
    private lateinit var pokemonName: String
    private var isPokemonCaptured: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPokemonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonCapture = findViewById(R.id.buttonCapture)

        service = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val pokemonUrl = intent.getStringExtra("POKEMON_URL")
        pokemonUrl?.let { fetchPokemonDetails(it) }

        buttonCapture.setOnClickListener {
            if (auth.currentUser != null) {
                if (isPokemonCaptured) {
                    removePokemonFromFirestore()
                } else {
                    savePokemonToFirestore()
                }
            } else {
                Toast.makeText(
                    this,
                    "Vous devez vous connecter pour capturer des Pokémon.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val buttonBackToList = findViewById<Button>(R.id.buttonBack)
        buttonBackToList.setOnClickListener {
            finish() // Termine l'activité et retourne à l'écran précédent
        }
    }

    private fun fetchPokemonDetails(url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getPokemonDetails(url)
                if (response.isSuccessful) {
                    val pokemonDetails = response.body()
                    withContext(Dispatchers.Main) {
                        pokemonDetails?.let {
                            displayPokemonDetails(it)
                            pokemonName = it.name
                            updateButtonLabel()
                        }
                    }
                } else {
                    // Gérer les erreurs
                }
            } catch (e: Exception) {
                // Gérer les erreurs
            }
        }
    }

    private fun displayPokemonDetails(pokemonDetails: PokemonDetails) {
        binding.apply {
            textViewName.text = pokemonDetails.name
            textViewHeight.text = "Height: ${pokemonDetails.height}"
            textViewWeight.text = "Weight: ${pokemonDetails.weight}"
            pokemonDetails.sprites.front_default?.let { url ->
                Glide.with(this@PokemonDetailActivity)
                    .load(url)
                    .into(imageViewPokemon)
            }
        }
    }

    private fun savePokemonToFirestore() {
        val userUid = auth.currentUser?.uid ?: return
        val capturedPokemonRef = firestore.collection("capturedPokemons")
        capturedPokemonRef.add(mapOf("pokemonName" to pokemonName, "userId" to userUid))
            .addOnSuccessListener {
                Toast.makeText(this, "Pokémon capturé avec succès.", Toast.LENGTH_SHORT).show()
                isPokemonCaptured = true
                updateButtonLabel()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erreur lors de la capture du Pokémon : ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun removePokemonFromFirestore() {
        val userUid = auth.currentUser?.uid ?: return
        val capturedPokemonRef = firestore.collection("capturedPokemons")
        capturedPokemonRef.whereEqualTo("pokemonName", pokemonName)
            .whereEqualTo("userId", userUid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Pokémon libéré avec succès.", Toast.LENGTH_SHORT).show()
                            isPokemonCaptured = false
                            updateButtonLabel()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Erreur lors de la libération du Pokémon : ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Erreur lors de la recherche du Pokémon : ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateButtonLabel() {
        val userUid = auth.currentUser?.uid
        if (userUid != null) {
            val capturedPokemonRef = firestore.collection("capturedPokemons")
            capturedPokemonRef.whereEqualTo("pokemonName", pokemonName)
                .whereEqualTo("userId", userUid)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        buttonCapture.text = "Libérer"
                        isPokemonCaptured = true
                    } else {
                        buttonCapture.text = "Capturer"
                        isPokemonCaptured = false
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Erreur lors de la récupération de l'état de capture du Pokémon : ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
