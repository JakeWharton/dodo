package com.jakewharton.dodo

import twitter4j.v1.Status
import twitter4j.v1.URLEntity

/** A minimal working subset of [Status] for testing purposes only. */
data class TestStatus(
	private val text: String,
	private val urlEntities: List<URLEntity>
) : Status {
	override fun getText() = text
	override fun getURLEntities() = urlEntities.toTypedArray()

	override fun getId() = TODO("Not yet implemented")
	override fun getRateLimitStatus() = TODO("Not yet implemented")
	override fun getAccessLevel() = TODO("Not yet implemented")
	override fun getUserMentionEntities() = TODO("Not yet implemented")
	override fun getHashtagEntities() = TODO("Not yet implemented")
	override fun getMediaEntities() = TODO("Not yet implemented")
	override fun getSymbolEntities() = TODO("Not yet implemented")
	override fun getCreatedAt() = TODO("Not yet implemented")
	override fun getDisplayTextRangeStart() = TODO("Not yet implemented")
	override fun getDisplayTextRangeEnd() = TODO("Not yet implemented")
	override fun getSource() = TODO("Not yet implemented")
	override fun isTruncated() = TODO("Not yet implemented")
	override fun getInReplyToStatusId() = TODO("Not yet implemented")
	override fun getInReplyToUserId() = TODO("Not yet implemented")
	override fun getInReplyToScreenName() = TODO("Not yet implemented")
	override fun getGeoLocation() = TODO("Not yet implemented")
	override fun getPlace() = TODO("Not yet implemented")
	override fun isFavorited() = TODO("Not yet implemented")
	override fun isRetweeted() = TODO("Not yet implemented")
	override fun getFavoriteCount() = TODO("Not yet implemented")
	override fun getUser() = TODO("Not yet implemented")
	override fun isRetweet() = TODO("Not yet implemented")
	override fun getRetweetedStatus() = TODO("Not yet implemented")
	override fun getContributors() = TODO("Not yet implemented")
	override fun getRetweetCount() = TODO("Not yet implemented")
	override fun isRetweetedByMe() = TODO("Not yet implemented")
	override fun getCurrentUserRetweetId() = TODO("Not yet implemented")
	override fun isPossiblySensitive() = TODO("Not yet implemented")
	override fun getLang() = TODO("Not yet implemented")
	override fun getScopes() = TODO("Not yet implemented")
	override fun getWithheldInCountries() = TODO("Not yet implemented")
	override fun getQuotedStatusId() = TODO("Not yet implemented")
	override fun getQuotedStatus() = TODO("Not yet implemented")
	override fun getQuotedStatusPermalink() = TODO("Not yet implemented")

	override fun compareTo(other: Status?) = TODO("Not yet implemented")
}

data class TestURLEntity(
	private val expandedUrl: String,
	private val start: Int,
	private val end: Int,
) : URLEntity {
	override fun getStart() = start
	override fun getEnd() = end
	override fun getExpandedURL() = expandedUrl

	override fun getText() = TODO("Not yet implemented")
	override fun getURL() = TODO("Not yet implemented")
	override fun getDisplayURL() = TODO("Not yet implemented")
}
