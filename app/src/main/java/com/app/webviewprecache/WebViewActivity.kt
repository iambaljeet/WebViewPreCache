package com.app.webviewprecache

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewClientCompat
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream


class WebViewActivity : AppCompatActivity() {
    private var webUrlCoroutineJob: Job? = null
    private var websiteToLoad: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        websiteToLoad = intent.extras?.getString("URL")

        val settings: WebSettings = webview.settings
        settings.loadWithOverviewMode = true
        settings.databaseEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        settings.setAppCacheEnabled(false)

        setupWebViewClient()
        loadUrlIntoWebView()
    }

    fun setupWebViewClient() {
        val webViewClient = WebViewClientCompat()
        webview.webViewClient = webViewClient
    }

    fun loadUrlIntoWebView() {
        webUrlCoroutineJob = CoroutineScope(Dispatchers.IO).launch {
            val path: File = filesDir
            val file = File(path, "offlineFile.txt")
            val length = file.length().toInt()
            val bytes = ByteArray(length)
            val fileInputStream = FileInputStream(file)
            try {
                fileInputStream.read(bytes)
            } finally {
                fileInputStream.close()
            }
            val htmlString = String(bytes)
            withContext(Dispatchers.Main) {
                webview.loadDataWithBaseURL(websiteToLoad, htmlString,
                        "text/html", "base64", null)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webUrlCoroutineJob?.cancel()
    }

    companion object {
        fun launchActivity(context: Context, websiteToLoad: String?) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                extras?.apply {
                    putString("URL", websiteToLoad)
                }
            }
            context.startActivity(intent)
        }
    }
}