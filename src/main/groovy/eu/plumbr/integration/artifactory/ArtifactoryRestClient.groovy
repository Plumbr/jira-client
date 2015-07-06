package eu.plumbr.integration.artifactory

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

class ArtifactoryRestClient implements ArtifactoryClient {
  private static Logger logger = Logging.getLogger(ArtifactoryRestClient)

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

  void deleteOldArtifacts(String buildName) {
    def date = new DateTime().minusDays(30).toString(ISODateTimeFormat.date())
    def query = """
items.find({
	"\$and":
		[
			{"@build.name":"$buildName"},
			{"repo":"staging"},
      {"created":{"\$lt":"$date"}}
		]}).include("property.*")
"""
    def answer = restClient.post(path: '/plumbr/api/search/aql', body: query, contentType: ContentType.JSON).data
    def oldBuilds = (answer.results['properties']*.find { it.key == 'build.number' }.value.sort() as Set).join(',')
    logger.quiet("Deleting old staged build $oldBuilds")
    restClient.delete(path: "/plumbr/api/build/$buildName", query: ['buildNumbers': oldBuilds, 'artifacts': 1])
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
