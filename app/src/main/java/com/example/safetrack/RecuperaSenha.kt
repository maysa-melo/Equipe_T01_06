package com.example.safetrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class RecuperaSenha : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperas)

        auth = FirebaseAuth.getInstance()


        val emailEditText = findViewById<EditText>(R.id.editTextcaixaE)
        val btnEnviar = findViewById<Button>(R.id.btnLink)
        val btnVoltarLogin = findViewById<TextView>(R.id.textvoltaLogin)


        // Enviar link de recuperação
        btnEnviar.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Digite um e-mail válido", Toast.LENGTH_SHORT).show()
            } else {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Link enviado para o e-mail.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Erro ao enviar link: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        // Voltar para tela de login
        btnVoltarLogin.setOnClickListener {
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish() // Encerra a tela atual
        }
    }
}
