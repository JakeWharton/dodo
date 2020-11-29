package com.jakewharton.dodo

import com.jakewharton.dodo.db.Search
import com.jakewharton.dodo.db.TweetIndexQueries
import com.jakewharton.dodo.db.TweetQueries
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import twitter4j.Paging
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterObjectFactory

class Dodo(
	private val twitter: Twitter,
	private val tweetQueries: TweetQueries,
	private val searchQueries: TweetIndexQueries
) {
	init {
		require(twitter.configuration.isJSONStoreEnabled) {
			"JSON store must be enabled"
		}
	}

	fun sync() {
		val newestStatusId = tweetQueries.newest().executeAsOne().status_id ?: 1L

		val tweets = twitter.timelines().getHomeTimeline(Paging(newestStatusId))
		logger.info("Retrieved ${tweets.size} new tweets")

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
			tweetQueries.insert(
				status_id = tweet.id,
				status_user_id = tweet.user.id,
				status_user_handle = tweet.user.screenName,
				status_user_name = tweet.user.name,
				status_text = text,
				status_unix_time = tweet.createdAt.time, // Twitter4J parses in UTC.
				retweeted_id = tweet.retweetedStatus?.id,
				retweeted_user_id = tweet.retweetedStatus?.user?.id,
				retweeted_user_handle = tweet.retweetedStatus?.user?.screenName,
				retweeted_user_name = tweet.retweetedStatus?.user?.name,
				retweeted_unix_time = tweet.retweetedStatus?.createdAt?.time,  // Twitter4J parses in UTC.
				quoted_id = quotedStatus?.id,
				quoted_user_id = quotedStatus?.user?.id,
				quoted_user_handle = quotedStatus?.user?.screenName,
				quoted_user_name = quotedStatus?.user?.name,
				quoted_text = quotedStatus?.displayText,
				quoted_unix_time = quotedStatus?.createdAt?.time, // Twitter4J parses in UTC.
				json = TwitterObjectFactory.getRawJSON(tweet), // LMAO WTF thread local cache?!?
			)
		}
	}

	suspend fun totalCount(): Long {
		return withContext(IO) {
			tweetQueries.totalCount().executeAsOne()
		}
	}

	suspend fun search(query: String): List<Search> = withContext(IO) {
		searchQueries.search(query).executeAsList()
	}

	private companion object {
		private val logger = LoggerFactory.getLogger(Dodo::class.java)
	}
}
