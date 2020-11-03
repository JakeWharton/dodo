package com.jakewharton.dodo

import com.jakewharton.dodo.db.Database
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

private const val versionPragma = "user_version"

suspend fun withDatabase(path: String, block: suspend (db: Database) -> Unit) {
	JdbcSqliteDriver("jdbc:sqlite:$path").use { driver ->
		val oldVersion = queryOldVersion(driver)
		val newVersion = Database.Schema.version

		if (oldVersion == 0) {
			println("Creating DB version $newVersion!")
			Database.Schema.create(driver)
			driver.execute(null, "PRAGMA $versionPragma=$newVersion", 0)
		} else if (oldVersion < newVersion) {
			println("Migrating DB from version $oldVersion to $newVersion!")
			Database.Schema.migrate(driver, oldVersion, newVersion)
			driver.execute(null, "PRAGMA $versionPragma=$newVersion", 0)
		}

		block(Database(driver))
	}
}

/** 0 means no version */
private fun queryOldVersion(driver: JdbcSqliteDriver): Int {
	return driver.executeQuery(null, "PRAGMA $versionPragma", 0).use { cursor ->
		if (cursor.next()) {
			cursor.getLong(0)?.toInt()
		} else {
			null
		}
	} ?: 0
}
