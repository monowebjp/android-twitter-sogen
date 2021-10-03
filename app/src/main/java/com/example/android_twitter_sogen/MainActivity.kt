package com.example.android_twitter_sogen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import com.example.android_twitter_sogen.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
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
        val handler = Handler()
        launch {
            async(context = Dispatchers.IO) {
                val twitter = TwitterFactory.getSingleton()

                try {
                    val statuses = twitter.getHomeTimeline()

                    handler.post(Runnable {
                        for (status in statuses) {
                            val icon = ImageView(this@MainActivity)
                            Picasso.get()
                                .load(status.user.profileImageURL.replace("http", "https"))
                                .resize(100, 100)
                                .into(icon)
                            val accountName = TextView(this@MainActivity)
                            accountName.text = status.user.name
                            val tweet = TextView(this@MainActivity)
                            tweet.text = status.text
                            binding.timelineLinearLayout.addView(icon)
                            binding.timelineLinearLayout.addView(accountName)
                            binding.timelineLinearLayout.addView(tweet)
                        }
                    })
                } catch (e: TwitterException) {
                    e.printStackTrace()
                }
            }.await()
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}