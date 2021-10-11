package com.example.android_twitter_sogen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
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
    private var sinceId: String = "0"

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

        binding.newTweetButton.setOnClickListener {
            onClickTweet()
        }

        binding.showTimelineButton.setOnClickListener {
            onClickShowTimeline()
        }
    }

    private fun onClickTweet () {
        val newTweetIntent = Intent(applicationContext, NewTweetActivity::class.java)
        startActivity(newTweetIntent)
    }

    private fun createTimeline(statuses: JsonNode) {
        if (statuses.size() > 0) {
            sinceId = statuses[0].get("id").asText()
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            for (i in statuses.size() - 1 downTo 0) {
                println(statuses[i].get("id").asText())
                val timelineItem = layoutInflater.inflate(R.layout.timeline, null)
                val timelineIcon = timelineItem.findViewById<ImageView>(R.id.timelineIcon)
                val timelineDisplayName = timelineItem.findViewById<TextView>(R.id.displayName)
                val timelineScreenName = timelineItem.findViewById<TextView>(R.id.screenName)
                val timelineTweet = timelineItem.findViewById<TextView>(R.id.timelineTweet)
                val timelineFavoriteButton = timelineItem.findViewById<ImageButton>(R.id.favoriteButton)
                Picasso.get()
                    .load(statuses[i].get("user").get("profile_image_url_https").asText())
                    .resize(150, 150)
                    .into(timelineIcon)
                timelineDisplayName.text = statuses[i].get("user").get("name").asText()
                timelineScreenName.text = "@${statuses[i].get("user").get("screen_name").asText()}"
                timelineTweet.text = statuses[i].get("text").asText()

                timelineFavoriteButton.setOnClickListener {
                    onClickFavoriteTweet(statuses[i].get("id").asText(), timelineFavoriteButton)
                }

                timelineItem.findViewById<ImageButton>(R.id.replyButton).setOnClickListener {
                    println("${statuses[i].get("id").asText()}返信！")
                }

                binding.timelineLinearLayout.addView(timelineItem, 0)
            }
        })
    }

    private fun onClickShowTimeline() {
        launch {
            async(context = Dispatchers.IO) {
                // API呼び出し準備
                var params: String = ""
                if (sinceId != "0") { params = "since_id=$sinceId" }
                createTimeline(CallTwitterAPI().getHomeTimeline(params))
            }.await()
            binding.timelineSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun getFavoriteStatus(jsonNode: JsonNode): Boolean {
        if (jsonNode.size() > 0) {
            return jsonNode.get("favorited").asText() == "true"
        }

        return false
    }

    private fun onClickFavoriteTweet (id: String, timelineFavoriteButton: ImageButton) {
        var favoritedStatus = false
        launch {
            async(context = Dispatchers.IO) {
                favoritedStatus = getFavoriteStatus(CallTwitterAPI().favoriteTweet("id=${id}"))
            }.await()

            println(favoritedStatus)

            if (favoritedStatus) {
                timelineFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_24)
            } else {
                timelineFavoriteButton.setImageResource(R.drawable.ic_baseline_favorite_border_24)
            }
        }
    }

//    override fun onDestroy() {
//        job.cancel()
//        super.onDestroy()
//    }
}