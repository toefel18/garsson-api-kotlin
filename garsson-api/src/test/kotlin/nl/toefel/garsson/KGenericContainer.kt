package nl.toefel.garsson

import org.testcontainers.containers.GenericContainer

/**
 * Workaround for issue: https://github.com/testcontainers/testcontainers-java/issues/318
 */
class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)