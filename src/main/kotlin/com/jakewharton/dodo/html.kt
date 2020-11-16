package com.jakewharton.dodo

import com.jakewharton.dodo.db.Search
import kotlinx.html.FlowContent
import kotlinx.html.FormMethod.get
import kotlinx.html.HTML
import kotlinx.html.InputType.submit
import kotlinx.html.InputType.text
import kotlinx.html.a
import kotlinx.html.blockQuote
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.head
import kotlinx.html.input
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.title

fun HTML.renderIndex(query: String?, totalCount: Long, tweets: List<Search>) {
	head {
		meta(charset = "utf-8")
		meta(name = "viewport", content = "width=device-width, initial-scale=1.0")
		title("Dodo Tweet Archive")
		link(rel = "stylesheet", href = "/static/chota.min.css")
		meta(name = "twitter:dnt", content = "on")
		script(src = "https://platform.twitter.com/widgets.js") {
			async = true
			charset = "utf-8"
		}
	}
	body {
		nav(classes = "nav") {
			div(classes = "nav-center") {
				a(href = "/", classes = "brand") {
					+"Dodo 🐦"
				}
			}
		}
		div(classes = "container") {
			div(classes = "row") {
				div(classes = "col") {
					form(action = "/", method = get, classes = "is-center") {
						div(classes = "grouped") {
							input(name = "q", type = text) {
								placeholder = "Search term"
								autoFocus = true
								if (query != null) {
									value = query
								}
							}
							input(type = submit) {
								value = "Search $totalCount tweets"
							}
						}
					}
				}
			}
			div(classes = "row") {
				div(classes = "col") {
					for (tweet in tweets) {
						tweet(tweet)
					}
				}
			}
		}
	}
}

private fun FlowContent.tweet(tweet: Search) {
	blockQuote("twitter-tweet tw-align-center") {
		attributes["data-cards"] = "hidden"
		attributes["data-conversation"] = "none"

		p {
			+tweet.status_text

			if (tweet.quoted_text != null) {
				blockQuote {
					p {
						+tweet.quoted_text
					}
					p {
						+"— "
						twitterHandleLink(tweet.quoted_user_handle!!)
					}
				}
			}
		}
		p {
			+"— "
			if (tweet.retweeted_user_handle != null) {
				twitterHandleLink(tweet.retweeted_user_handle)
				+" retweeted by "
			}
			twitterHandleLink(tweet.status_user_handle)
		}
		p {
			a(href = "https://twitter.com/${tweet.status_user_handle}/status/${tweet.status_id}") {
				+"May 5, 2014" // TODO
			}
		}
	}
}

private fun FlowContent.twitterHandleLink(handle: String) {
	a(href = "https://twitter.com/$handle") {
		rel = "noreferrer noopener"
		+"@$handle"
	}
}
