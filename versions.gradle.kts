val buildSrcKotlinVersion: String by extra(findProperty("buildSrc.kotlin.version")?.toString() ?: embeddedKotlinVersion)

val kotlinVersion by extra("1.3.11")
val dokkaVersion by extra("0.9.17")
val agpVersion by extra("3.2.1")
