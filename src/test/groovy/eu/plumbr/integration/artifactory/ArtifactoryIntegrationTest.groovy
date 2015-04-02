package eu.plumbr.integration.artifactory

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ArtifactoryIntegrationTest extends Specification {
  private static final String ARTIFACTORY_CREDENTIALS_PROPERTY_NAME = 'artifactoryCredentials'

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.ext.artifactoryUser = 'artifactoryUser'
    project.ext.artifactoryPassword = 'artifactoryPassword'
  }

  def "artifactory credentials are added to the project"() {
    expect:
    !project.hasProperty(ARTIFACTORY_CREDENTIALS_PROPERTY_NAME)

    when:
    project.apply plugin: 'jira'

    then:
    def credentials = project.property(ARTIFACTORY_CREDENTIALS_PROPERTY_NAME)
    credentials != null
    credentials.username == 'artifactoryUser'
    credentials.password == 'artifactoryPassword'
  }
}
