package eu.plumbr.integration.artifactory

import eu.plumbr.integration.plugin.Util
import org.gradle.api.Project

class ArtifactoryIntegration {

  static ArtifactoryClient apply(Project project) {
    def extraProperties = project.getExtensions().getExtraProperties()

    def artifactoryCredentials = [username: Util.propertySafe(project, 'artifactoryUser'),
                                  password: Util.propertySafe(project, 'artifactoryPassword')]
    extraProperties.set('artifactoryCredentials', artifactoryCredentials)
    return artifactoryCredentials.username ? getArtifactoryClient(artifactoryCredentials.username, artifactoryCredentials.password) : null
  }

  static ArtifactoryClient getArtifactoryClient(String artifactoryUser, String artifactoryPassword) {
    return new ArtifactoryRestClient(artifactoryUser, artifactoryPassword)
  }
}
