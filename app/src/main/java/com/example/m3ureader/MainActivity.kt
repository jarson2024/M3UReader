package com.example.m3ureader

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ChannelAdapter
    private lateinit var status: TextView

    private val openFile =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) loadFromFile(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.txtStatus)
        val urlInput = findViewById<EditText>(R.id.editUrl)
        val recycler = findViewById<RecyclerView>(R.id.recyclerChannels)

        adapter = ChannelAdapter { channel ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_URL, channel.url)
                putExtra(PlayerActivity.EXTRA_NAME, channel.name)
            }
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnLoadUrl).setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, getString(R.string.erro_url_vazia), Toast.LENGTH_SHORT).show()
            } else {
                loadFromUrl(url)
            }
        }

        findViewById<Button>(R.id.btnOpenFile).setOnClickListener {
            openFile.launch(arrayOf("*/*"))
        }
    }

    private fun loadFromUrl(url: String) {
        status.text = getString(R.string.carregando)
        Thread {
            try {
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 15000
                    requestMethod = "GET"
                }
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val channels = M3uParser.parse(text)
                runOnUiThread { showChannels(channels) }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = getString(R.string.erro_carregar, e.message ?: "")
                }
            }
        }.start()
    }

    private fun loadFromFile(uri: Uri) {
        status.text = getString(R.string.carregando)
        Thread {
            try {
                val text = contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()
                val channels = M3uParser.parse(text)
                runOnUiThread { showChannels(channels) }
            } catch (e: Exception) {
                runOnUiThread {
                    status.text = getString(R.string.erro_carregar, e.message ?: "")
                }
            }
        }.start()
    }

    private fun showChannels(channels: List<Channel>) {
        adapter.submit(channels)
        status.text = if (channels.isEmpty()) {
            getString(R.string.nenhum_canal)
        } else {
            getString(R.string.canais_encontrados, channels.size)
        }
    }
}
