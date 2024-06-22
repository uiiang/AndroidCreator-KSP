import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.*

object RepoConfig {
  const val group = "uii.ang"
  const val version = "1.0"
  const val artifactId = "creator"
}

apply(plugin = "maven-publish")

configure<PublishingExtension> {
  repositories {
    mavenLocal()
  }
}


afterEvaluate {
  extensions.configure<PublishingExtension>("publishing") {
    publications {
      create<MavenPublication>("mavenJava") { //对应release 版 build variant
        groupId = RepoConfig.group
        artifactId = RepoConfig.artifactId
        version = RepoConfig.version

        from(components["java"])
      }
    }
  }
}