package com.example.android_twitter_sogen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import kotlinx.coroutines.*
import twitter4j.TwitterFactory
import kotlin.coroutines.CoroutineContext

//プロパティファイルを使う場合
class MainActivity : AppCompatActivity(), CoroutineScope {

    //Coroutinesを扱うための設定（詳細は後述）
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {                  //ID:buttonのボタンをクリックした際の処理
            onClick()
        }
    }

    private fun onClick() {
        launch {
//            ツイートを入力する
            val editTweet = findViewById<EditText>(R.id.editTweet)
            val textview = findViewById<TextView>(R.id.textView)
            textview.text = "Now Sending"            //ここはメインスレッドで動作するのでViewの変更ができる

            async(context = Dispatchers.IO) {
                val twitter = TwitterFactory().getInstance()
//                twitter.updateStatus(editTweet.text.toString())      //ツイートの投稿
            }.await()                                //.await()で通信処理が終わるまで待機

            editTweet.text = null
            textview.text = "finish"
        }
    }

    override fun onDestroy() {
        job.cancel()                                 //すべてのコルーチンキャンセル用
        super.onDestroy()
    }
}