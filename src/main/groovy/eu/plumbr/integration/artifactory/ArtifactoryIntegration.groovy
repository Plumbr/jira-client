package eu.plumbr.integration.artifactory

import eu.plumbr.integration.plugin.Util
import org.gradle.api.Project

class ArtifactoryIntegration {

  static ArtifactoryClient apply(Project project) {
    def extraProperties = project.getExtensions().getExtraProperties()

    def artifactoryUser = Util.propertySafe(project, 'artifactoryUser')
    def artifactoryPassword = Util.propertySafe(project, 'artifactoryPassword')
    def artifactoryCredentials = {
      username = artifactoryUser
      password = artifactoryPassword
    }
    extraProperties.set('artifactoryCredentials', artifactoryCredentials)
    return artifactoryUser ? makeArtifactoryClient(artifactoryUser, artifactoryPassword) : null
  }

  static ArtifactoryClient makeArtifactoryClient(String artifactoryUser, String artifactoryPassword) {
    return new ArtifactoryRestClient(artifactoryUser, artifactoryPassword)
  }
}
