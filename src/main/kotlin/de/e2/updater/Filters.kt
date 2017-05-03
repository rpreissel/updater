package de.e2.updater

import spark.Filter


object Filters {
    val addJsonTypeIfNotPresent = Filter { _, response ->
        if (response.type() == null) {
            response.type("application/json")
        }
    }
}