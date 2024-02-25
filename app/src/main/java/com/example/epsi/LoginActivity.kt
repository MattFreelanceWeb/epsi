package com.example.epsi


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val btn_login = findViewById<Button>(R.id.btn_login)

        btn_login.setOnClickListener {
            val et_email = findViewById<EditText>(R.id.et_email)
            val email = et_email.text.toString()

            val et_password = findViewById<EditText>(R.id.et_password)
            val password = et_password.text.toString()
            signIn(email, password)
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // L'authentification a réussi
                    Toast.makeText(this, "Connexion réussie!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                    finish()
                    // Rediriger vers l'écran suivant, par exemple le tableau de bord
                } else {
                    // L'authentification a échoué
                    Toast.makeText(this, "Échec de la connexion. Veuillez réessayer.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}