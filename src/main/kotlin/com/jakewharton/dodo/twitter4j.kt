package com.jakewharton.dodo

import twitter4j.v1.Status

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
