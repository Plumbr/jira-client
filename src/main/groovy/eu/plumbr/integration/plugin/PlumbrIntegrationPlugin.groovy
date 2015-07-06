package eu.plumbr.integration.plugin

import eu.plumbr.integration.artifactory.ArtifactoryIntegration
import eu.plumbr.integration.jira.JiraIntegration
import org.gradle.api.Plugin
import org.gradle.api.Project

class PlumbrIntegrationPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def jiraClient = JiraIntegration.apply(project)
    def artifactoryClient = ArtifactoryIntegration.apply(project)
    CloseReleasedIssuesTask.apply(project, jiraClient, artifactoryClient)
    DeleteOldArtifactsTask.apply(project, artifactoryClient)
  }
}


