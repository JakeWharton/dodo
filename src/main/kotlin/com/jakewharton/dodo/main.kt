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
import io.ktor.response.respond
import io.ktor.response.respondBytes
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import twitter4j.Twitter
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
		.default(8098)
		.help("Port for the HTTP server (default 8098)")

	override fun run(dodo: Dodo) {
		embeddedServer(Netty, port) {
			routing {
				get("/") {
					call.respondText {
						withContext(Dispatchers.IO) {
							val tweets = dodo.tweets()
							buildString {
								append(tweets.size)
								appendLine(" tweets\n")
								tweets.joinTo(this, separator = "\n", prefix = "\n")
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
}
