package com.example.android_twitter_sogen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.android_twitter_sogen.databinding.ActivityNewTweetBinding
import kotlinx.coroutines.*
import java.net.URLEncoder
import kotlin.coroutines.CoroutineContext

class NewTweetActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding :ActivityNewTweetBinding

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewTweetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("----- intent check -----")
        println(intent.getStringExtra("REPLY_ID"))

        binding.submitTweetButton.setOnClickListener {
            onSubmitTweet()
        }

        binding.newTweetArea.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.countNewTweet.text = p3.toString()
            }
        })
    }

    private fun onSubmitTweet () {
        launch {
            val replyId = intent.getStringExtra("REPLY_ID")
            var toastMessage = ""
            var code = 0

            async(context = Dispatchers.IO) {
                // API呼び出し準備
                val status = URLEncoder.encode("${binding.newTweetArea.text}", "utf-8")
                var params = "?status=$status"
                if (replyId != "") {
                    params = "${params}&in_reply_to_status_id=${replyId}"
                }
                println("----- param check -----")
                println(params)
                code = CallTwitterAPI().tweetStatus(params)
            }.await()

            if (code == 200) {
                toastMessage = "成功"
                binding.newTweetArea.text.clear()
                finish()
            } else {
                toastMessage = "失敗"
            }

            Toast.makeText(applicationContext, toastMessage, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}