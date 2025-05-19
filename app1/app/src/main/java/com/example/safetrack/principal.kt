package com.example.safetrack

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.EditText

class principal : AppCompatActivity() {
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var locationManager: LocationManager
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private var selectedImageUri: Uri? = null // Variável para armazenar a imagem selecionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_principal)

        val autoCompleteCategoria = findViewById<AutoCompleteTextView>(R.id.autoCompleteCategoria)
        val categorias = listOf("Químico", "Elétrico", "Infraestrutura")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        autoCompleteCategoria.setAdapter(adapter)
        autoCompleteCategoria.setOnClickListener {
            autoCompleteCategoria.showDropDown()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseManager = FirebaseManager()
        locationManager = LocationManager(this)

        checkLocationPermission()

        val textCadastro = findViewById<TextView>(R.id.textCadastro)
        textCadastro.setOnClickListener {
            // Quando clicado, volta para a MainActivity
            val intent = Intent(this@principal, MainActivity::class.java)
            startActivity(intent)
            finish() // Opcional: Fecha a atividade atual para não deixar o usuário voltar com o botão de voltar
        }

        val btnAnexarFoto = findViewById<AppCompatButton>(R.id.btnAnexarFoto)
        btnAnexarFoto.setOnClickListener {
            if (selectedImageUri != null) {
                lifecycleScope.launch {
                    try {
                        // Upload da imagem
                        val imageUrlResult = firebaseManager.uploadImagem(selectedImageUri!!)
                        if (imageUrlResult.isSuccess) {
                            val imageUrl = imageUrlResult.getOrNull()

                            // Obter localização do usuário
                            val location = locationManager.getCurrentLocation()
                            location?.let { loc ->
                                val user = firebaseManager.getCurrentUser()
                                user?.let { currentUser ->
                                    val timestamp = getCurrentTimestamp()
                                    val incidenteResult = firebaseManager.registrarIncidente(
                                        currentUser.uid,
                                        "Categoria", // Substitua com a categoria real
                                        "Descrição", // Substitua com a descrição real
                                        imageUrl,
                                        "Local", // Substitua com o local real
                                        loc.latitude,
                                        loc.longitude,
                                        "Setor", // Substitua com o setor real
                                        "Status" // Substitua com o status real
                                    )

                                    if (incidenteResult.isSuccess) {
                                        Toast.makeText(this@principal, "Incidente registrado com sucesso", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@principal, "Falha ao registrar incidente", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@principal, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@principal, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Selecione uma imagem primeiro", Toast.LENGTH_SHORT).show()
            }
        }

        val btnRegistrarRisco = findViewById<AppCompatButton>(R.id.button2)
        btnRegistrarRisco.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Por favor, anexe uma imagem antes de registrar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoria = findViewById<EditText>(R.id.autoCompleteCategoria).text.toString()
            val descricao = findViewById<EditText>(R.id.editTextText4).text.toString()
            val local = findViewById<EditText>(R.id.editTextText).text.toString()
            val setor = findViewById<EditText>(R.id.editTextText6).text.toString()
            val status = findViewById<EditText>(R.id.editTextText7).text.toString()

            lifecycleScope.launch {
                try {
                    val imageUrlResult = firebaseManager.uploadImagem(selectedImageUri!!)
                    if (imageUrlResult.isSuccess) {
                        val imageUrl = imageUrlResult.getOrNull()
                        val location = locationManager.getCurrentLocation()
                        location?.let { loc ->
                            val user = firebaseManager.getCurrentUser()
                            user?.let { currentUser ->
                                val timestamp = getCurrentTimestamp()
                                val incidenteResult = firebaseManager.registrarIncidente(
                                    currentUser.uid,
                                    categoria,
                                    descricao,
                                    imageUrl,
                                    local,
                                    loc.latitude,
                                    loc.longitude,
                                    setor,
                                    status
                                )

                                if (incidenteResult.isSuccess) {
                                    Toast.makeText(this@principal, "Incidente registrado com sucesso", Toast.LENGTH_SHORT).show()

                                    // Limpar campos
                                    findViewById<EditText>(R.id.autoCompleteCategoria).text.clear()  // Limpa o campo de categoria
                                    findViewById<EditText>(R.id.editTextText4).text.clear()  // Limpa o campo de descrição
                                    findViewById<EditText>(R.id.editTextText).text.clear()   // Limpa o campo de local
                                    findViewById<EditText>(R.id.editTextText6).text.clear()  // Limpa o campo de setor
                                    findViewById<EditText>(R.id.editTextText7).text.clear()  // Limpa o campo de status

                                    // Limpar imagem
                                    val imageView = findViewById<ImageView>(R.id.imageView3)
                                    imageView.setImageURI(null) // Limpa a imagem
                                    imageView.visibility = ImageView.GONE // Oculta o ImageView

                                    // Limpar o texto de imagem anexada
                                    val textView = findViewById<TextView>(R.id.textImagemAnexada)
                                    textView.visibility = TextView.GONE // Oculta o texto

                                    // Limpar a URI da imagem selecionada
                                    selectedImageUri = null
                                } else {
                                    val errorMsg = incidenteResult.exceptionOrNull()?.message ?: "Erro desconhecido"
                                    Toast.makeText(this@principal, "Erro ao registrar: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@principal, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@principal, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val btnSelecionarFoto  = findViewById<AppCompatButton>(R.id.btnAnexarFoto)
        btnSelecionarFoto .setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 100)
        }

        updateLocation()
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun updateLocation() {
        lifecycleScope.launch {
            try {
                val location = locationManager.getCurrentLocation()
                location?.let { loc -> // nome diferente aqui
                    val user = firebaseManager.getCurrentUser()
                    user?.let { currentUser ->
                        val timestamp = getCurrentTimestamp()
                        firebaseManager.saveUserLocationDetailed(
                            currentUser.uid,
                            loc.latitude,
                            loc.longitude,
                            timestamp
                        )
                    }
                }
            } catch (e: Exception) {
                if (!isFinishing && !isDestroyed) {
                    Toast.makeText(this@principal, "Erro ao obter localização: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateLocation()
            } else {
                Toast.makeText(this, "Permissão de localização necessária", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            val imageView = findViewById<ImageView>(R.id.imageView3)
            val textView = findViewById<TextView>(R.id.textImagemAnexada)

            imageView.setImageURI(selectedImageUri)
            imageView.visibility = ImageView.VISIBLE
            textView.visibility = TextView.VISIBLE
        }
    }
}
