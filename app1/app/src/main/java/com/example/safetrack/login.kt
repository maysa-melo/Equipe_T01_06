package com.example.safetrack

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class login : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseManager = FirebaseManager()

        // Campos de entrada
        val editEmail = findViewById<EditText>(R.id.editTextText)
        val editSenha = findViewById<EditText>(R.id.editTextTextPassword)

        // Botão de login
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener {
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val result = firebaseManager.signIn(email, senha)
                    result.fold(
                        onSuccess = {
                            val intent = Intent(this@login, principal::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onFailure = { e ->
                            Toast.makeText(this@login, "Erro ao fazer login: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@login, "Erro ao fazer login: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Texto "Não tem uma conta? Cadastre-se aqui"
        val textCadastro = findViewById<TextView>(R.id.textCadastro)
        val texto = "Não tem uma conta? Cadastre-se aqui"
        val spannable = SpannableString(texto)

        // Negrito na parte "Cadastre-se aqui"
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable.setSpan(boldSpan, 19, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Clique na parte "Cadastre-se aqui"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@login, cadastro::class.java)
                startActivity(intent)
            }
        }
        spannable.setSpan(clickableSpan, 19, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textCadastro.text = spannable
        textCadastro.movementMethod = LinkMovementMethod.getInstance()
    }
}