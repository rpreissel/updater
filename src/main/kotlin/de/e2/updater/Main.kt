package de.e2.updater

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.swarm.*
import spark.Filter
import spark.Spark.before
import spark.Spark.get

fun main(args: Array<String>) {
    var docker: DefaultDockerClient = DefaultDockerClient.fromEnv().build();
    docker.use {
        val version = docker.version();
        println(version);

        val containers = docker.listContainers(DockerClient.ListContainersParam.withLabel("app"));

        containers.forEach {
            println("${it.id()} - ${it.names()}")

            val info = docker.inspectContainer(it.id());
            if (info.state().running()) {
                println("running")
            }

//            val name = it.names()!!.first()
//            if(name.startsWith("/elas")) {
            docker.removeContainer(it.id(), DockerClient.RemoveContainerParam.forceKill())
//            }
        }

        val serviceFound = docker.listServices().filter { it.spec().name() == "nginx" }.firstOrNull();
        if (serviceFound != null) {
            docker.removeService(serviceFound.id());
        }

        val serviceSpec = ServiceSpec.builder()
                .mode(ServiceMode.withReplicas(2))
                .addLabel("app", "rene")
                .endpointSpec(
                        EndpointSpec.builder()
                                .ports(PortConfig.builder()
                                        .publishedPort(8080)
                                        .targetPort(80)
                                        .build())
                                .build())
                .name("nginx")
                .taskTemplate(TaskSpec.builder()
                        .containerSpec(ContainerSpec.builder()
                                .image("nginx")
                                .build())
                        .build())
                .build()
        val newServiceResponse = docker.createService(serviceSpec);

        val newService = docker.inspectService(newServiceResponse.id());
        newService.spec()

        docker.updateService(newService.id(), newService.version().index(),
                ServiceSpec.builder()
                        .name(newService.spec().name())
                        .labels(newService.spec().labels())
                        .taskTemplate(newService.spec().taskTemplate())
                        .mode(ServiceMode.withReplicas(5))
                        .endpointSpec(newService.spec().endpointSpec())
                        .updateConfig(newService.spec().updateConfig())
                        .build());

        val dockerService = DockerService(DefaultDockerClient.fromEnv().build());

        before(Filter { _, response -> response.type("application/json") })

        get("/hello", { _, _ ->
            dockerService.findService("nginx");
        }, jacksonObjectMapper().writerWithDefaultPrettyPrinter()::writeValueAsString)
    }
}