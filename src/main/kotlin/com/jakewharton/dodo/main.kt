@file:JvmName("Main")

package com.jakewharton.dodo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import kotlinx.coroutines.runBlocking
import twitter4j.Paging
import twitter4j.Status
import twitter4j.TwitterFactory

fun main(vararg args: String) {
	NoOpCliktCommand(name = "dodo")
		.subcommands(SyncCommand())
		.main(args)
}

private class SyncCommand : CliktCommand(name = "sync") {
	private val dbPath by option("--db", metavar = "FILE")
		.required()
		.help("Sqlite database file")
	private val accessToken by option(metavar = "KEY")
		.required()
		.help("OAuth access token")
	private val accessSecret by option(metavar = "KEY")
		.required()
		.help("OAuth access token secret")
	private val apiKey by option(metavar = "KEY")
		.required()
		.help("OAuth consumer API key")
	private val apiSecret by option(metavar = "KEY")
		.required()
		.help("OAuth consumer API secret")

	override fun run() = runBlocking {
		withDatabase(dbPath) { db ->
			val config = configuration {
				setOAuthAccessToken(accessToken)
				setOAuthAccessTokenSecret(accessSecret)
				setOAuthConsumerKey(apiKey)
				setOAuthConsumerSecret(apiSecret)
			}
			val twitter = TwitterFactory(config).instance

			val newestStatusId = db.tweetQueries.newest().executeAsOne().status_id ?: 1L

			val tweets = twitter.timelines().getHomeTimeline(Paging(newestStatusId))
			println("Fetched ${tweets.size} tweets")

			for (tweet in tweets) {
				val text: String
				val quotedStatus: Status?
				if (tweet.isRetweet) {
					text = tweet.retweetedStatus.displayText
					quotedStatus = tweet.retweetedStatus.quotedStatus
				} else {
					text = tweet.text
					quotedStatus = tweet.quotedStatus
				}
				db.tweetQueries.insert(
					status_id = tweet.id,
					status_user_id = tweet.user.id,
					status_user_name = tweet.user.screenName,
					status_text = text,
					retweet_id = tweet.retweetedStatus?.id,
					retweet_user_id = tweet.retweetedStatus?.user?.id,
					retweet_user_name = tweet.retweetedStatus?.user?.screenName,
					quoted_id = quotedStatus?.id,
					quoted_user_id = quotedStatus?.user?.id,
					quoted_user_name = quotedStatus?.user?.screenName,
					quoted_text = quotedStatus?.displayText)
			}
		}
	}
}
