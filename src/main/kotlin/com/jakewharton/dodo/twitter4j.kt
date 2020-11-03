package com.jakewharton.dodo

import twitter4j.Status
import twitter4j.conf.Configuration
import twitter4j.conf.ConfigurationBuilder

inline fun configuration(body: ConfigurationBuilder.() -> Unit): Configuration {
	return ConfigurationBuilder().apply(body).build()
}

val Status.displayText: String get() {
	return buildString {
		var lastIndex = 0
		for (urlEntity in urlEntities.sortedBy { it.start }) {
			append(text, lastIndex, urlEntity.start)
			append(urlEntity.expandedURL)
			lastIndex = urlEntity.end
		}
		append(text, lastIndex, text.length)
	}
}
