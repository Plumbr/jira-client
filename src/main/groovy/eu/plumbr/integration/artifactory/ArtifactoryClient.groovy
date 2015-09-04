package eu.plumbr.integration.artifactory

import eu.plumbr.integration.PlumbrVersion

interface ArtifactoryClient {

  List<String> getReleasedIssues(String buildName, String buildNumber)

  void deleteOldArtifacts(String buildName)

  void promoteBuild(String buildName, String buildNumber, String targetRepo)

  PlumbrVersion latestBuildVersion(String artifactId, String version)

  PlumbrVersion latestBuildVersion(String artifactId)

  void downloadArtifact(File destination, String fullPath)
}