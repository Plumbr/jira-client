package eu.plumbr.integration.jira

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class JiraIntegrationTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
    project.ext.jiraUser = 'jiraUser'
    project.ext.jiraPassword = 'jiraPassword'
  }

  def "plugin can be applied without any configuration"() {
    expect:
    !project.hasProperty(JiraIntegration.JIRA_VERSION_PROPERTY_NAME)

    when:
    def project = ProjectBuilder.builder().build()
    project.apply plugin: 'eu.plumbr.integration'

    then:
    noExceptionThrown()
    project.property(JiraIntegration.JIRA_VERSION_PROPERTY_NAME) == null
  }

  def "jira version property is populated from jira client"() {
    def propertyName = JiraIntegration.JIRA_VERSION_PROPERTY_NAME
    expect:
    !project.hasProperty(propertyName)

    when:
    GroovySpy(JiraIntegration, global: true)
    def jiraClient = Mock(JiraClient)
    JiraIntegration.makeJiraClient(_, _, _) >> jiraClient
    jiraClient.currentVersion() >> '42'
    project.apply plugin: 'eu.plumbr.integration'

    then:
    project.property(propertyName) == '42'
  }
}
