package eu.plumbr.integration.plugin

import eu.plumbr.integration.artifactory.ArtifactoryClient
import eu.plumbr.integration.jira.JiraClient
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PlumbrIntegrationPluginTest extends Specification {
  private static final String CLOSE_ISSUES_TASK_NAME = 'closeAgentReleasedIssues'
  Project project

  def setup() {
    project = ProjectBuilder.builder().withName('portal').build()
    System.setProperty(PlumbrVersionTask.SOURCE_VERSION, '1.2.3.1984')
  }

  def cleanup() {
    System.clearProperty(PlumbrVersionTask.SOURCE_VERSION)
  }

  def "close issues task is added"() {
    expect:
    project.tasks.findByName(CLOSE_ISSUES_TASK_NAME) == null

    when:
    project.apply plugin: 'eu.plumbr.integration'

    then:
    def task = project.tasks.findByName(CLOSE_ISSUES_TASK_NAME)
    task != null
    task.description != null
  }

  def "close issues task gets build number from project's version"() {
    when:
    project.apply plugin: 'eu.plumbr.integration'

    then:
    CloseReleasedIssuesTask task = project.tasks.findByName(CLOSE_ISSUES_TASK_NAME) as CloseReleasedIssuesTask
    task != null
    task.buildNumber == '1984'
  }

  def "close issues task closes all resolved issues released in given build"() {
    setup:
    project.apply plugin: 'eu.plumbr.integration'

    CloseReleasedIssuesTask task = project.tasks.findByName(CLOSE_ISSUES_TASK_NAME) as CloseReleasedIssuesTask
    task.jiraClient = Mock(JiraClient)
    task.artifactoryClient = Mock(ArtifactoryClient)

    when:
    task.run()

    then:
    1 * task.artifactoryClient.getReleasedIssues('Agent', '1984') >> ['a', 'c', 'b']
    1 * task.jiraClient.closeResolvedIssue('a')
    1 * task.jiraClient.closeResolvedIssue('b')
    1 * task.jiraClient.closeResolvedIssue('c')
  }

  def "close issues task handles gracefully build without issues"() {
    setup:
    project.apply plugin: 'eu.plumbr.integration'

    CloseReleasedIssuesTask task = project.tasks.findByName(CLOSE_ISSUES_TASK_NAME) as CloseReleasedIssuesTask
    task.jiraClient = Mock(JiraClient)
    task.artifactoryClient = Mock(ArtifactoryClient)

    when:
    task.run()

    then:
    1 * task.artifactoryClient.getReleasedIssues('Agent', '1984') >> []
    0 * task.jiraClient.closeResolvedIssue(_)
  }

  def "close released issues rule should create requested task"() {
    expect:
    project.tasks.findByName("closePortalReleasedIssues") == null

    when:
    project.apply plugin: 'eu.plumbr.integration'

    then:
    CloseReleasedIssuesTask task = project.tasks.findByName("closePortalReleasedIssues") as CloseReleasedIssuesTask
    task != null
    task.description != null
    task.buildName == 'Portal'
  }

  def "delete old builds rule should create requested task"() {
    expect:
    project.tasks.findByName("deleteDashboardStagedBuilds") == null

    when:
    project.apply plugin: 'eu.plumbr.integration'

    then:
    DeleteOldArtifactsTask task = project.tasks.findByName("deleteDashboardStagedBuilds") as DeleteOldArtifactsTask
    task != null
    task.description != null
    task.buildName == 'Dashboard'
  }

  def "build promotion rule should create requested task"() {
    expect:
    project.tasks.findByName("promoteOOMToStaging") == null

    when:
    project.apply plugin: 'eu.plumbr.integration'

    then:
    PromoteBuildTask task = project.tasks.findByName("promoteOOMToStaging") as PromoteBuildTask
    task != null
    task.description != null
    task.buildName == 'OOM'
    task.targetRepo == 'staging'
    task.buildNumber == '1984'
  }

  def "artifactory client is created in project if user is given"() {
    expect:
    !project.hasProperty('artifactoryClient')

    when:
    project.ext.artifactoryUser = 'user'
    project.apply plugin: 'eu.plumbr.integration'

    then:
    project.artifactoryClient instanceof ArtifactoryClient
  }

  def "artifactory client property is not created in project if no artifactory user is given"() {
    expect:
    !project.hasProperty('artifactoryClient')

    when:
    project.apply plugin: 'eu.plumbr.integration'

    then:
    !project.hasProperty('artifactoryClient')
  }

}