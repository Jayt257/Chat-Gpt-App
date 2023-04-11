package com.jaytaravia.chatgptclone

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val etQuestion=findViewById<EditText>(R.id.etQuestion)
        val btnSubmit=findViewById<Button>(R.id.btnSubmit)
        val btnCopy=findViewById<Button>(R.id.btnCopy)
        val txtResponse=findViewById<TextView>(R.id.txtResponse)

        btnSubmit.setOnClickListener {
            val question=etQuestion.text.toString().trim()
            Toast.makeText(this,question,Toast.LENGTH_SHORT).show()
            if(question.isNotEmpty()){
            getResponse(question) { response ->
                runOnUiThread {
                    txtResponse.text = response
                    etQuestion.setText("")
                }
            }
            }
        }

        btnCopy.setOnClickListener {
            copyTextToClipboard(txtResponse)
        }
    }

    private fun copyTextToClipboard(txtResponse: TextView) {
        val textToCopy = txtResponse.text
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_LONG).show()
    }

    fun getResponse(question: String, callback: (String) -> Unit){
        val apiKey="Your_API_KEY"
        val url="https://api.openai.com/v1/engines/text-davinci-003/completions"

        val requestBody="""
            {
            "prompt": "$question",
            "max_tokens": 500,
            "temperature": 0
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("error","API failed",e)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                val body=response.body?.string()
                if (body != null) {
                    Log.v("data",body)
                }
                else{
                    Log.v("data","empty")
                }
                val jsonObject=JSONObject(body)
                val jsonArray:JSONArray=jsonObject.getJSONArray("choices")
                val textResult=jsonArray.getJSONObject(0).getString("text")
                callback(textResult)
            }

        })
    }

}