package com.example.android_twitter_sogen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        binding.timelineSwipeRefreshLayout.setOnRefreshListener {
            onClickShowTimeline()
        }

        binding.showTimelineButton.setOnClickListener {
            onClickShowTimeline()
        }
    }

    private fun onClickShowTimeline() {
        launch {
            async(context = Dispatchers.IO) {
                val twitter = TwitterFactory.getSingleton()

                try {
                    val statuses = twitter.getHomeTimeline()

                    Handler(Looper.getMainLooper()).post(Runnable {
                        for (i in statuses.indices) {
                            val timelineItem = layoutInflater.inflate(R.layout.timeline, null)
                            val timelineIcon = timelineItem.findViewById<ImageView>(R.id.timelineIcon)
                            val timelineDisplayName = timelineItem.findViewById<TextView>(R.id.displayName)
                            val timelineScreenName = timelineItem.findViewById<TextView>(R.id.screenName)
                            val timelineTweet = timelineItem.findViewById<TextView>(R.id.timelineTweet)
                            Picasso.get()
                                .load(statuses[i].user.profileImageURL.replace("http", "https"))
                                .resize(150, 150)
                                .into(timelineIcon)
                            timelineDisplayName.text = statuses[i].user.name
                            timelineScreenName.text = "@${statuses[i].user.screenName}"
                            timelineTweet.text = statuses[i].text
                            binding.timelineLinearLayout.addView(timelineItem, 0)
                        }
                    })
                } catch (e: TwitterException) {
                    e.printStackTrace()
                }
            }.await()
            binding.timelineSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}