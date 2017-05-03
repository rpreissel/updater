package de.e2.updater

import spark.Spark.*
import spark.debug.DebugScreen

fun main(args: Array<String>) {
    port(8080)
    DebugScreen.enableDebugScreen()
//    after(Filters.addJsonTypeIfNotPresent)

    get("/hello", { req, resp ->
        "World"
    })
    get("/do/:para", { req, resp ->
        "World ${req.params(":para")}"
    })

    post("/post", { req, resp ->
        val params = req.params();
        "World"
    })
}