package net.jcflorezr.config

import com.mongodb.MongoClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

@Configuration
@PropertySource(value = ["classpath:net.jcflorezr.config/mongo.properties"])
class MongoConfig : AbstractMongoConfiguration() {

    @Value("\${mongo.database-name}")
    private lateinit var databaseName: String
    @Value("\${mongo.host}")
    private lateinit var host: String
    @Value("\${mongo.port}")
    private lateinit var port: String

    override fun getDatabaseName() = databaseName

    override fun mongoClient() = MongoClient(host, port.toInt())

    @Bean fun transcriberMongoClient() = mongoClient()

    @Bean fun transcriberMongoTemplate() = MongoTemplate(transcriberMongoClient(), getDatabaseName())
}