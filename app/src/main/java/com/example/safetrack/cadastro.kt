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

class cadastro : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseManager = FirebaseManager()

        // Campos de entrada
        val editNome = findViewById<EditText>(R.id.editTextText2)
        val editEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val editSenha = findViewById<EditText>(R.id.editTextTextPassword2)

        // Botão de cadastro
        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener {
            val nome = editNome.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val senha = editSenha.text.toString().trim()


            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val result = firebaseManager.signUp(nome, email, senha)
                    result.fold(
                        onSuccess = {
                            Toast.makeText(this@cadastro, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@cadastro, login::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onFailure = { e ->
                            Toast.makeText(this@cadastro, "Erro ao fazer cadastro: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                } catch (e: Exception) {
                    Toast.makeText(this@cadastro, "Erro ao fazer cadastro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Texto "Já tem uma conta? Entre aqui" com parte clicável
        val textLogin = findViewById<TextView>(R.id.textLogin)
        val texto = "Já tem uma conta? Entre aqui"
        val spannable = SpannableString(texto)

        // Negrito na parte "Entre aqui"
        val boldSpan = StyleSpan(Typeface.BOLD)
        spannable.setSpan(boldSpan, 18, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Clique na parte "Entre aqui"
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@cadastro, login::class.java)
                startActivity(intent)
            }
        }
        spannable.setSpan(clickableSpan, 18, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        textLogin.text = spannable
        textLogin.movementMethod = LinkMovementMethod.getInstance()
    }
}
