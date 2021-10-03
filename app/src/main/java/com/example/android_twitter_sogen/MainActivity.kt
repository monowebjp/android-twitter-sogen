package com.example.android_twitter_sogen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android_twitter_sogen.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import twitter4j.*
import kotlin.coroutines.CoroutineContext

//プロパティファイルを使う場合
class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var binding :ActivityMainBinding

    //Coroutinesを扱うための設定（詳細は後述）
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.showTimelineButton.setOnClickListener {
            onClickShowTimeline()
        }
    }

    private fun onClickShowTimeline() {
        launch {
            async(context = Dispatchers.IO) {
                val twitter = TwitterFactory.getSingleton()
                val statuses = twitter.getHomeTimeline()

                for (status in statuses) {
                    System.out.println(status.getUser().getName())
                }
            }.await()
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}