package eu.plumbr.integration.artifactory

import eu.plumbr.integration.PlumbrVersion

interface ArtifactoryClient {

  List<String> getReleasedIssues(String buildName, String buildNumber)

  void deleteOldArtifacts(String buildName)

  void promoteBuild(String buildName, String buildNumber, String targetRepo)

  PlumbrVersion latestVersion(String artifactId, String version)

  PlumbrVersion latestVersion(String artifactId)

  String buildStatus(String buildName, String version)

  void downloadArtifact(File destination, String fullPath)
}