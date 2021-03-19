package com.fransrudolf.pdftextimage

import StorageService
import com.fransrudolf.pdftextimage.storage.StorageProperties
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties::class)
@RestController
class PdftextimageApplication

fun main(args: Array<String>) {
	runApplication<PdftextimageApplication>(*args)

	@Bean
	fun init(storageService: StorageService): CommandLineRunner? {
		return CommandLineRunner { args: Array<String?>? ->
			storageService.deleteAll()
			storageService.init()
		}
	}
}
