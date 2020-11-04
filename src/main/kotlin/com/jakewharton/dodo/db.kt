package com.jakewharton.dodo

import com.jakewharton.dodo.db.Database
import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver

inline fun withDatabase(path: String, crossinline block: (db: Database) -> Unit) {
	JdbcSqliteDriver("jdbc:sqlite:$path").use { driver ->
		migrateIfNeeded(driver)
		block(Database(driver))
	}
}

private const val versionPragma = "user_version"

fun migrateIfNeeded(driver: JdbcSqliteDriver) {
	val oldVersion =
		driver.executeQuery(null, "PRAGMA $versionPragma", 0).use { cursor ->
			if (cursor.next()) {
				cursor.getLong(0)?.toInt()
			} else {
				null
			}
		} ?: 0

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
}
