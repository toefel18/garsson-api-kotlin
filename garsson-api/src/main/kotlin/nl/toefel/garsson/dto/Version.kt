package nl.toefel.garsson.dto

import nl.toefel.garsson.BUILD_DATE
import nl.toefel.garsson.GIT_BRANCH
import nl.toefel.garsson.GIT_SHA
import nl.toefel.garsson.MAVEN_NAME
import nl.toefel.garsson.util.now

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