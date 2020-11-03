package com.jakewharton.dodo

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class Twitter4jTest {
	@Test fun displayTextNoUrlEntitiesReturnsOriginal() {
		val status = TestStatus(
			"This is some text",
			emptyList(),
		)
		assertThat(status.displayText).isEqualTo("This is some text")
	}

	@Test fun displayTextUrlFirst() {
		val status = TestStatus(
			"This is some text",
			listOf(TestURLEntity("Expanded", 0, 4))
		)
		assertThat(status.displayText).isEqualTo("Expanded is some text")
	}

	@Test fun displayTextUrlLast() {
		val status = TestStatus(
			"This is some text",
			listOf(TestURLEntity("expanded", 13, 17))
		)
		assertThat(status.displayText).isEqualTo("This is some expanded")
	}

	@Test fun displayTextUrlOutOfOrder() {
		val status = TestStatus(
			"This is some text",
			listOf(
				TestURLEntity("expanded", 13, 17),
				TestURLEntity("Expanded", 0, 4),
			)
		)
		assertThat(status.displayText).isEqualTo("Expanded is some expanded")
	}
}
