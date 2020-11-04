package com.jakewharton.dodo

import com.jakewharton.dodo.db.Overview
import com.jakewharton.dodo.db.TweetQueries
import org.slf4j.LoggerFactory
import twitter4j.Paging
import twitter4j.Status
import twitter4j.Twitter
import twitter4j.TwitterObjectFactory

class Dodo(
	private val twitter: Twitter,
	private val queries: TweetQueries,
) {
	fun sync() {
		val newestStatusId = queries.newest().executeAsOne().status_id ?: 1L

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
			queries.insert(
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
				quoted_text = quotedStatus?.displayText,
				json = TwitterObjectFactory.getRawJSON(tweet), // LMAO WTF thread local cache?!?
			)
		}
	}

	fun tweets(): List<Overview> {
		return queries.overview().executeAsList()
	}

	private companion object {
		private val logger = LoggerFactory.getLogger(Dodo::class.java)
	}
}
