package eu.plumbr.integration.plugin

import eu.plumbr.integration.artifactory.ArtifactoryClient
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class DeleteOldArtifactsTask extends DefaultTask {

  public ArtifactoryClient artifactoryClient
  String buildName

  DeleteOldArtifactsTask() {
    this.description = "Deletes old builds in staging and their artifacts. Default age is 30 days."
  }

  @TaskAction
  def run() {
    logger.quiet("Deleting old $buildName builds")
    artifactoryClient.deleteOldArtifacts(buildName)
  }

  static void apply(Project project, ArtifactoryClient artifactoryClient) {
    project.tasks.addRule("Pattern: delete<BuildName>StagedBuilds") { String taskName ->
      def matcher = taskName =~ /delete(.+)StagedBuilds/
      if (matcher) {
        DeleteOldArtifactsTask task = project.task(taskName, type: DeleteOldArtifactsTask) as DeleteOldArtifactsTask
        task.buildName = matcher.group(1)
        if (artifactoryClient) {
          task.artifactoryClient = artifactoryClient
        }
      }
    }
  }

}
