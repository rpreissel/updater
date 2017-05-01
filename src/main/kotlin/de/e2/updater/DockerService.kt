package de.e2.updater

import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.messages.swarm.Service


class DockerService(val docker: DefaultDockerClient) {
    fun findService(name:String): Service? {
        val serviceFound = docker.listServices().filter { it.spec().name() == name }.firstOrNull();
        return serviceFound
    }
}