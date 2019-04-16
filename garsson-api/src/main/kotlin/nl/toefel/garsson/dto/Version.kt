package nl.toefel.garsson.dto

import nl.toefel.garsson.util.now
import nl.toefel.garsson.*

data class Version(
        val application: String,
        val buildTime: String,
        val git_commit: String,
        val git_branch: String,
        val serverTime: String = now()) {

    companion object {
        fun fromBuildInfo() = Version(
                application = MAVEN_NAME,
                git_commit = if (GIT_SHA.length > 7) GIT_SHA.substring(0, 7) else "unknown",
                buildTime = BUILD_DATE,
                git_branch = GIT_BRANCH,
                serverTime = now())
    }

}