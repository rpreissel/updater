package de.e2.updater

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.Event

fun main(args: Array<String>) {
    var docker: DefaultDockerClient = DefaultDockerClient.fromEnv().build();
    docker.use {
        val version = docker.version();
        println(version);

        val eventStream = docker.events()
        eventStream.forEach {
            println(it)
            if (it.type() == Event.Type.CONTAINER && it.action() == "create") {
                val container = docker.inspectContainer(it.actor()!!.id())
                val labels = container.config().labels()
                println(labels)
                val swarmId: String? = labels!!.get("com.docker.swarm.service.id");
                if(swarmId != null) {
                    val service = docker.inspectService(swarmId);
                    println(service)
                }
            }
        }

        println("Ende");
    }
}