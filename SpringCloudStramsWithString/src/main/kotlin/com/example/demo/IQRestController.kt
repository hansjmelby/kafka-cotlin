package com.example.demo

import org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class IQRestController {

    @Autowired
    private lateinit var iqService: InteractiveQueryService

    @GetMapping("/iq/count/{word}")
    fun getCount(@PathVariable word: String): Long {
        val store =
            iqService.getQueryableStore("word-count-state-store", keyValueStore<String, Long>())
        return store[word]
    }
    @GetMapping("/iq/countTop")
    fun getCount2(): List<Word> {
        val store =
            iqService.getQueryableStore("word-count-state-store", keyValueStore<String, Long>())

        var list = mutableListOf<Word>()
        store.all().forEach {
            Word(it.key,it.value)
            list.add(Word(it.key,it.value))
        }
        return list.filter { it.word?.length!! >2 }.sortedBy { it.count }.reversed().take(5);
    }
        data class Word(var word: String?,var count:Long)

}


