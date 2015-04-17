package eu.plumbr.integration.jira

import eu.plumbr.integration.plugin.Util
import org.gradle.api.Project
import org.slf4j.Logger

class JiraIntegration {
  public static final String JIRA_VERSION_PROPERTY_NAME = 'currentJiraVersion'

  static JiraClient apply(Project project) {
    def extraProperties = project.getExtensions().getExtraProperties()

    String jiraUser = Util.propertySafe(project, 'jiraUser')
    String jiraPassword = Util.propertySafe(project, 'jiraPassword')

    def jiraClient = null
    if (jiraUser) {
      jiraClient = makeJiraClient(jiraUser, jiraPassword, project.getLogger())
      extraProperties.set(JIRA_VERSION_PROPERTY_NAME, jiraClient.currentVersion())
    } else {
      extraProperties.set(JIRA_VERSION_PROPERTY_NAME, null)
    }
    return jiraClient
  }

  static JiraClient makeJiraClient(String jiraUser, String jiraPassword, Logger logger) {
    return new JiraRestClient(jiraUser, jiraPassword, logger)
  }
}
