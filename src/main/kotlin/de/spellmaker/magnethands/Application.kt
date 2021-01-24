package de.spellmaker.magnethands

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("de.spellmaker.magnethands")
		.start()
}

