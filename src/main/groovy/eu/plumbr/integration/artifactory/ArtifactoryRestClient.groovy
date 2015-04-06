package eu.plumbr.integration.artifactory

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient

class ArtifactoryRestClient implements ArtifactoryClient {
  final String username
  final String password
  RESTClient restClient

  ArtifactoryRestClient(String username, String password) {
    this.username = username
    this.password = password
    this.restClient = new RESTClient("https://plumbr.artifactoryonline.com")
    this.restClient.auth.basic username, password
  }

  @Override
  List<String> getReleasedIssues(String buildName, String buildNumber) {
    try {
      def jsonResponse = this.restClient.get(path: "/plumbr/api/build/${buildName}/${buildNumber}", contentType: ContentType.JSON).data
      return isRelease(jsonResponse) ? getKeys(jsonResponse) : []
    } catch (HttpResponseException ex) {
      println "Failed to get released issues: ${ex}"
      return []
    }
  }

  private static boolean isRelease(def response) {
    if (response.buildInfo.statuses != null) {
      return response.buildInfo.statuses.sort { it.timestamp }.last().status == "release"
    } else {
      return false
    }
  }

  private static List<String> getKeys(def response) {
    if (response.buildInfo.issues.affectedIssues != null) {
      return response.buildInfo.issues.affectedIssues.collect { it['key'] as String }
    } else {
      return []
    }
  }
}
