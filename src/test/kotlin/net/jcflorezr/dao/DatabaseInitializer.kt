package net.jcflorezr.dao

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.testcontainers.containers.GenericContainer

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

class TestMongoInitializer : TestRule {

    companion object {
        private const val mongoDockerImageName = "mongo:4.1-bionic"
        const val mongoPort = 27017
        val mongoDockerContainer: KGenericContainer = KGenericContainer(mongoDockerImageName).withExposedPorts(mongoPort)
    }

    override fun apply(statement: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                mongoDockerContainer.start()
                // Giving some time while the database is up
                Thread.sleep(1000L)
                try {
                    statement.evaluate()
                } finally {
                    mongoDockerContainer.stop()
                }
            }
        }
    }
}