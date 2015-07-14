package eu.plumbr.integration.plugin

import eu.plumbr.integration.artifactory.ArtifactoryClient
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class PromoteBuildTask extends DefaultTask {

  public ArtifactoryClient artifactoryClient
  String buildName
  String buildNumber
  String targetRepo

  PromoteBuildTask() {
    this.description = 'Promotes given version of the build to the requested repository'
  }

  @TaskAction
  def run() {
    artifactoryClient.promoteBuild(buildName, buildNumber, targetRepo)
  }

  static void apply(Project project, ArtifactoryClient artifactoryClient) {
    project.tasks.addRule("Pattern: promote<BuildName>To<RepoName>") { String taskName ->
      def matcher = taskName =~ /promote(.+)To(.+)/
      if (matcher) {
        PromoteBuildTask task = project.task(taskName, type: PromoteBuildTask) as PromoteBuildTask
        task.buildName = matcher.group(1)
        task.targetRepo = matcher.group(2).toLowerCase()
        task.buildNumber = project.version
        if (artifactoryClient) {
          task.artifactoryClient = artifactoryClient
        }
      }
    }
  }

}
