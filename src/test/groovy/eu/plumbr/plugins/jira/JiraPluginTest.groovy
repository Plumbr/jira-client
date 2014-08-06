package eu.plumbr.plugins.jira

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class JiraPluginTest {

  @Test
  public void "without jira username no jiraVersion is set"(){
    def project = ProjectBuilder.builder().build()

    project.apply plugin: 'jira'

    assert project.jira.version == null
  }

  @Test
  public void "jira properties are set from jira*"(){
    def project = ProjectBuilder.builder().build()

    project.extensions.getByName('ext').setProperty('jiraUser', 'us')
    project.extensions.getByName('ext').setProperty('jiraPassword', 'pw')
    project.apply plugin: 'jira'

    assert project.jira.user == 'us'
    assert project.jira.password == 'pw'
  }

}
