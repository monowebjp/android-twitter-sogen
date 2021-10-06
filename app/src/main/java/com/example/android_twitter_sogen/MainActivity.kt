package com.example.android_twitter_sogen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import com.example.android_twitter_sogen.databinding.ActivityMainBinding
import com.fasterxml.jackson.databind.JsonNode
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

//プロパティファイルを使う場合
class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding :ActivityMainBinding
    private var sinceId: Int = 0

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

    private fun createTimeline(statuses: JsonNode) {
        Handler(Looper.getMainLooper()).post(Runnable {
            for (status in statuses) {
                val timelineItem = layoutInflater.inflate(R.layout.timeline, null)
                val timelineIcon = timelineItem.findViewById<ImageView>(R.id.timelineIcon)
                val timelineDisplayName = timelineItem.findViewById<TextView>(R.id.displayName)
                val timelineScreenName = timelineItem.findViewById<TextView>(R.id.screenName)
                val timelineTweet = timelineItem.findViewById<TextView>(R.id.timelineTweet)
                Picasso.get()
                    .load(status.get("user").get("profile_image_url_https").asText())
                    .resize(150, 150)
                    .into(timelineIcon)
                timelineDisplayName.text = status.get("user").get("name").asText()
                timelineScreenName.text = "@${status.get("user").get("screen_name").asText()}"
                timelineTweet.text = status.get("text").asText()
                binding.timelineLinearLayout.addView(timelineItem, 0)
            }
        })
    }

    private fun onClickShowTimeline() {
        launch {
            async(context = Dispatchers.IO) {
                // API呼び出し準備
                var params: String = ""
                if (sinceId != 0) { params = "?since_id=$sinceId" }
                createTimeline(CallTwitterAPI().getHomeTimeline(params))
            }.await()
            binding.timelineSwipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }
}