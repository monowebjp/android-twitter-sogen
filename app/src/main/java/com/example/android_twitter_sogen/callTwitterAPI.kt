package com.example.android_twitter_sogen

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.scribejava.core.model.Verb

import com.github.scribejava.core.model.OAuthRequest
import com.github.scribejava.apis.TwitterApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth1AccessToken
import java.lang.NullPointerException


class CallTwitterAPI {
    private val service = ServiceBuilder(BuildConfig.TWITTER_CONSUMER_KEY)
        .apiSecret(BuildConfig.TWITTER_CONSUMER_SECRET)
        .build(TwitterApi.instance())
    private val accessToken = OAuth1AccessToken(BuildConfig.TWITTER_ACCESS_TOKEN, BuildConfig.TWITTER_ACCESS_TOKEN_SECRET)

    private fun requestApi (url: String): String {
        val request = OAuthRequest(Verb.GET, url)
        service.signRequest(accessToken, request)

        try {
            service.execute(request).use { response ->
                return response.body
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "[]"
    }

    // ツイートする
    fun tweetStatus(params: String): Int {
        val url = "https://api.twitter.com/1.1/statuses/update.json$params"
        var code = 0

        val request = OAuthRequest(Verb.POST, url)
        service.signRequest(accessToken, request)

        try {
            service.execute(request).use { response ->
                code = response.code
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        return code
    }

    // いいねする
    fun favoriteTweet (params: String, nowStatus: Boolean): JsonNode {
        var url = "https://api.twitter.com/1.1/favorites/"
        println("----- nowStatus1 ------")
        println(nowStatus)
        if (nowStatus) {
            url = "${url}destroy.json?${params}"
        } else {
            url = "${url}create.json?$params"
        }

        val request = OAuthRequest(Verb.POST, url)
        var jsonStr = "[]"
        service.signRequest(accessToken, request)

        try {
            service.execute(request).use { response ->
                println(response.body)
                jsonStr = response.body
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return createJson(jsonStr)
    }

    // ホームタイムライン更新
    fun getHomeTimeline(params: String): JsonNode {
        val url = "https://api.twitter.com/1.1/statuses/home_timeline.json?count=200${params}"
        val jsonStr = requestApi(url)

        return createJson(jsonStr)
    }

    private fun createJson (jsonStr: String):JsonNode {
        val mapper = ObjectMapper()
        val statuses : JsonNode

        try {
            statuses = mapper.readTree(jsonStr)

            // Timelineかどうかの判定（タイムラインだったらソートする）
            if (statuses.isArray) {
                statuses.sortedBy { it.get("id").asText().toIntOrNull() }
            }

            return statuses
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        return mapper.readTree("[]")
    }
}