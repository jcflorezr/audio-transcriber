package net.jcflorezr.config

import com.mongodb.MongoClient
import net.jcflorezr.dao.TestMongoInitializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.mongodb.config.AbstractMongoConfiguration

@Configuration
@PropertySource(value = ["classpath:config/mongo.properties"])
class TestMongoConfig : AbstractMongoConfiguration() {

    @Value("\${mongo.database-name}")
    private lateinit var databaseName: String

    override fun getDatabaseName() = databaseName

    override fun mongoClient(): MongoClient {
        val mongoContainer = TestMongoInitializer.mongoDockerContainer
        return MongoClient(
            mongoContainer.containerIpAddress,
            mongoContainer.getMappedPort(TestMongoInitializer.mongoPort)
        )
    }

    @Bean fun transcriberMongoClient() = mongoClient()

    @Bean fun transcriberMongoTemplate() = MongoTemplate(transcriberMongoClient(), getDatabaseName())
}