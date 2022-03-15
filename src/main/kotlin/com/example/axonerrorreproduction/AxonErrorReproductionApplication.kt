package com.example.axonerrorreproduction

import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.*

@SpringBootApplication
class AxonErrorReproductionApplication

fun main(args: Array<String>) {
    runApplication<AxonErrorReproductionApplication>(*args)
}


@RestController
@CrossOrigin
@RequestMapping
class TestController(
    private val queryGateway: QueryGateway,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
    @GetMapping("/")
    fun letsSeeIfThisWorks(): Flux<ServerSentEvent<UUID>> {
        val result = subscribe(SampleQuery(UUID.randomUUID())) ?: error("An query result would be expected here.")

        val initial = result.initialResult().map { response ->
            ServerSentEvent
                .builder<UUID>()
                .data(response)
                .event("initial")
                .id(response.toString())
                .build()
        }

        val updates = result.updates().map { response ->
            ServerSentEvent
                .builder<UUID>()
                .data(response)
                .event("update")
                .id(response.toString())
                .build()
        }

        generateRandomUUIDs()
        return Flux.concat(initial, updates)
    }

    fun subscribe(query: SampleQuery): SubscriptionQueryResult<UUID, UUID>? {
        return queryGateway.subscriptionQuery(
            query,
            ResponseTypes.instanceOf(UUID::class.java),
            ResponseTypes.instanceOf(UUID::class.java)
        )
    }

    fun generateRandomUUIDs() {
        Flux
            .interval(Duration.ofSeconds(1))
            .take(100)
            .map {
                queryUpdateEmitter.emit(SampleQuery::class.java, { true }, UUID.randomUUID())
            }.subscribe()
    }
}

@Service
@Suppress("unused")
class RandomGenerationService {
    @QueryHandler
    fun generate(query: SampleQuery): UUID {
        return UUID.randomUUID()
    }
}

data class SampleQuery(val result: UUID)