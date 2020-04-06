package com.app.webviewprecache

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.webviewprecache.utility.isInternetConnected
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.URL


class MainActivity : AppCompatActivity() {
    private var webUrlCoroutineJob: Job? = null
    private var websiteToLoad: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        load_web_view_button.setOnClickListener {
            WebViewActivity.launchActivity(this, websiteToLoad)
        }

        start_pre_loading_url_button.setOnClickListener {
            websiteToLoad = url_edit_text.text.toString()
            if (isInternetConnected()) {
                saveUrlDataToFile()
            } else {
                Toast.makeText(this, "No Internet connected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUrlDataToFile() {
        if (websiteToLoad?.isNotBlank() == true) {
            webUrlCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
                loadAndSaveDataFromUrlToFile(websiteToLoad)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Loading finished", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Enter website", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAndSaveDataFromUrlToFile(websiteToLoad: String?) {
        val google = URL(websiteToLoad)
        val bufferedReader = BufferedReader(InputStreamReader(google.openStream()))
        var input: String?
        val stringBuffer = StringBuffer()
        while (bufferedReader.readLine().also { input = it } != null) {
            stringBuffer.append(input)
        }
        bufferedReader.close()
        val htmlData = stringBuffer.toString()
        val path: File = filesDir
        val file = File(path, "offlineFile.txt")
        val stream = FileOutputStream(file)
        stream.use { stream ->
            stream.write(htmlData.toByteArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webUrlCoroutineJob?.cancel()
    }
}