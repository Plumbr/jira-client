package eu.plumbr.integration.plugin

import eu.plumbr.integration.artifactory.ArtifactoryClient
import eu.plumbr.integration.jira.JiraClient
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class CloseReleasedIssuesTask extends DefaultTask {
  public ArtifactoryClient artifactoryClient
  public JiraClient jiraClient

  @Input
  String buildNumber
  @Input
  String buildName

  CloseReleasedIssuesTask() {
    this.description = "Closes in JIRA all Resolved tasks that were released in the current build"
  }

  @TaskAction
  def run() {
    def keys = artifactoryClient.releasedIssues(buildName, buildNumber)
    keys.each { jiraClient.closeResolvedIssue(it) }
  }

  static void apply(Project project, JiraClient jiraClient, ArtifactoryClient artifactoryClient) {
    project.tasks.addRule("Pattern: close<BuildName>ReleasedIssues") { String taskName ->
      def matcher = taskName =~ /close(.+)ReleasedIssues/
      if (matcher) {
        CloseReleasedIssuesTask task = project.task(taskName, type: CloseReleasedIssuesTask) as CloseReleasedIssuesTask
        task.buildNumber = project.version
        task.jiraClient = jiraClient
        task.buildName = matcher.group(1)
        if (artifactoryClient) {
          task.artifactoryClient = artifactoryClient
        }
      }
    }
  }

}
