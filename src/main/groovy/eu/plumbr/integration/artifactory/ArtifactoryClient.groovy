package eu.plumbr.integration.artifactory

interface ArtifactoryClient {

  List<String> getReleasedIssues(String buildName, String buildNumber)

  void deleteOldArtifacts(String buildName)

  void promoteBuild(String buildName, String buildNumber, String targetRepo)
}