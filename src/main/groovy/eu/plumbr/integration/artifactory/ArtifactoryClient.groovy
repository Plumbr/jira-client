package eu.plumbr.integration.artifactory

interface ArtifactoryClient {

  List<String> releasedIssues(String buildName, String buildNumber)
}