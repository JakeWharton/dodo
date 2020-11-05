@file:JvmName("Main")

package com.jakewharton.dodo

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.application.call
import io.ktor.html.respondHtml
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respondBytes
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.FormMethod.get
import kotlinx.html.InputType
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
import twitter4j.TwitterFactory

fun main(vararg args: String) {
	NoOpCliktCommand(name = "dodo")
		.subcommands(SyncCommand(), RunCommand())
		.main(args)
}

private abstract class DodoCommand(
	name: String,
	help: String,
) : CliktCommand(
	name = name,
	help = help
) {
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

	override fun run() {
		val config = configuration {
			setOAuthAccessToken(accessToken)
			setOAuthAccessTokenSecret(accessSecret)
			setOAuthConsumerKey(apiKey)
			setOAuthConsumerSecret(apiSecret)
			setJSONStoreEnabled(true)
		}
		val twitter = TwitterFactory(config).instance

		withDatabase(dbPath) { db ->
			val dodo = Dodo(twitter, db.tweetQueries)
			run(dodo)
		}
	}

	abstract fun run(dodo: Dodo)
}

private class SyncCommand : DodoCommand(
	name = "sync",
	help = "Perform a one-time sync of the latest tweets",
) {
	override fun run(dodo: Dodo) {
		dodo.sync()
	}
}

private class RunCommand : DodoCommand(
	name = "run",
	help = "Start an HTTP server for displaying tweets and performing syncs",
) {
	private val port by option(metavar = "PORT").int()
		.default(defaultPort)
		.help("Port for the HTTP server (default $defaultPort)")

	override fun run(dodo: Dodo) {
		embeddedServer(Netty, port) {
			routing {
				static("/static") {
					resources("static")
				}
				get("/") {
					val query = call.request.queryParameters["q"]
					val count = dodo.count()
					val tweets = if (query != null) dodo.search(query) else emptyList()
					call.respondHtml {
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
												input(type = InputType.submit) {
													value = "Search $count tweets"
												}
											}
										}
									}
								}
								div(classes = "row") {
									div(classes = "col") {
										for (tweet in tweets) {
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
																+"— @${tweet.quoted_user_name} "
															}
														}
													}
												}
												p {
													+"— @${tweet.status_user_name} "
												}
												p {
													a(href = "https://twitter.com/${tweet.status_user_name}/status/${tweet.status_id}") {
														+"May 5, 2014" // TODO
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				post("/sync") {
					dodo.sync()
					call.respondBytes(byteArrayOf())
				}
			}
		}.start(wait = true)
	}

	companion object {
		private const val defaultPort = 8098
	}
}
