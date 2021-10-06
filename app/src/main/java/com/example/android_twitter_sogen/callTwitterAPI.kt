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
        }

        return "[]"
    }

    fun tweetStatus(params: String): Int {
        val url = "https://api.twitter.com/1.1/statuses/update.json$params"
        var code = 0

        println(url)
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

    fun getHomeTimeline(params: String): JsonNode {
        val url = "https://api.twitter.com/1.1/statuses/home_timeline.json?$params"
        val jsonStr = requestApi(url)
        val mapper = ObjectMapper()
        val statuses : JsonNode

        try {
            statuses = mapper.readTree(jsonStr)
            return statuses
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        return mapper.readTree("[]")
    }
}