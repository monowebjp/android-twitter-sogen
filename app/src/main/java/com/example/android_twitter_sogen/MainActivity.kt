package com.example.android_twitter_sogen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android_twitter_sogen.databinding.ActivityMainBinding
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeType
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


//プロパティファイルを使う場合
class MainActivity : AppCompatActivity(),
    CoroutineScope {
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
            onClickTweet("")
        }
    }


    private fun onClickTweet (tweetId: String) {
        val newTweetIntent = Intent(applicationContext, NewTweetActivity::class.java)

        newTweetIntent.putExtra("REPLY_ID", tweetId)

        startActivity(newTweetIntent)
    }

    private fun createTimeline(statuses: JsonNode) {
        if (statuses.size() > 0) {
            sinceId = statuses[0].get("id").asText()
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            for (i in statuses.size() - 1 downTo 0) {
                if (statuses[i].get("text").asText().take(4) == "RT @"
                    // TODO: またはツイートしている人本人に対しての自己リプライの場合は表示する
                    || (statuses[i].get("text").asText().take(1) == "@" && statuses[i].get("text").asText().take(10) != "@bithitkit")) {
                    continue
                }

                val timelineItem = layoutInflater.inflate(R.layout.timeline, null)
                val timelineIcon = timelineItem.findViewById<ImageView>(R.id.timelineIcon)
                val timelineDisplayName = timelineItem.findViewById<TextView>(R.id.displayName)
                val timelineScreenName = timelineItem.findViewById<TextView>(R.id.screenName)
                val timelineTweet = timelineItem.findViewById<TextView>(R.id.timelineTweet)
                val timelineFavoriteStatusIcon =
                    timelineItem.findViewById<ImageView>(R.id.favoriteStatusIcon)
                val tweetDeleteButton = timelineItem.findViewById<ImageButton>(R.id.deleteTweetButton)

                Picasso.get()
                    .load(statuses[i].get("user").get("profile_image_url_https").asText())
                    .resize(150, 150)
                    .into(timelineIcon)
                timelineDisplayName.text = statuses[i].get("user").get("name").asText()
                timelineScreenName.text =
                    "@${statuses[i].get("user").get("screen_name").asText()}"

                // TODO: URLの短縮を解除して、サムネイル画像等を表示する
                var tweet = statuses[i].get("text").asText()
                tweet = decodeTweetShortUrl(tweet, statuses[i].get("entities").findPath("urls"))

                println("----- えんてぃてぃ -----")
                println(statuses[i].get("text").asText())
                println(statuses[i].get("entities").findPath("media"))
                timelineTweet.text = decodeImageUrl(tweet, statuses[i].get("entities").findPath("media"))


                val detector = GestureDetector(
                    applicationContext,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onLongPress(event: MotionEvent) {
                            println("ロングプレスされたよ")
                            onClickTweet(statuses[i].get("id").asText())
                            // TODO: ここで引用か返信か選べるようにする
                        }
                        override fun onDoubleTap(e: MotionEvent?): Boolean {
                            println("ダブルタップされたよ\uD83D\uDE02")
                            onClickFavoriteTweet(
                                statuses[i].get("id").asText(),
                                timelineFavoriteStatusIcon
                            )
                            return super.onDoubleTap(e)
                        }
                    })

                if (statuses[i].get("user").get("screen_name").asText() == "bithitkit" ) {
                    // TODO: 削除ボタンの機能を追加しなきゃいけない
                    tweetDeleteButton.visibility = View.VISIBLE
                }

                timelineItem.setOnTouchListener { _, event ->
                    detector.onTouchEvent(event)
                }


                showFavoriteIcon(
                    timelineFavoriteStatusIcon,
                    statuses[i].get("favorited").asBoolean()
                )

                // TODO: 引用RTを実装する

                binding.timelineLinearLayout.addView(timelineItem, 0)
            }
        })
    }

    private fun decodeTweetShortUrl (text: String, entitiesURL: JsonNode): String {
        var tweet = text
        for (entity in entitiesURL) {
            tweet = tweet.replace(entity.get("url").asText(), entity.get("expanded_url").asText())
        }

        return tweet
    }

    private fun decodeImageUrl (text: String, entitiesMedia: JsonNode): String {
        var tweet = text

        for (entity in entitiesMedia) {
            tweet = tweet.replace(entity.get("url").asText(), entity.get("media_url_https").asText())
        }

        return text
    }

    private fun showFavoriteIcon (statusIcon: ImageView, nowStatus: Boolean) {
        if (nowStatus) {
            statusIcon.visibility = View.VISIBLE
        } else {
            statusIcon.visibility = View.GONE
        }
    }

    private fun toBoolean (status: Int): Boolean {
        // 8 = View.GONE
        // 0 = View.VISIBLE
        return 0 == status
    }

    private fun onClickShowTimeline() {
        launch {
            async(context = Dispatchers.IO) {
                // API呼び出し準備
                var params: String = ""
                if (sinceId != "0") { params = "&since_id=${sinceId}" }
                createTimeline(CallTwitterAPI().getHomeTimeline(params))
            }.await()
            binding.timelineSwipeRefreshLayout.isRefreshing = false
        }
    }

    private fun onClickFavoriteTweet (
        tweetId: String,
        statusIcon: ImageView
    ) {
        var favoritedStatus = false
        val nowStatus = toBoolean(statusIcon.visibility)
        launch {
            async(context = Dispatchers.IO) {
                val jsonNode = CallTwitterAPI().favoriteTweet("id=${tweetId}", nowStatus)

                if (jsonNode.size() > 0) {
                    favoritedStatus = jsonNode.get("favorited").asBoolean()
                }
            }.await()

            showFavoriteIcon(statusIcon, favoritedStatus)
        }
    }

//    override fun onDestroy() {
//        job.cancel()
//        super.onDestroy()
//    }
}