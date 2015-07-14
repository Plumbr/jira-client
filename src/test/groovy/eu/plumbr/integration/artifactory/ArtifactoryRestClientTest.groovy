package eu.plumbr.integration.artifactory

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import spock.lang.Specification

class ArtifactoryRestClientTest extends Specification {
  private ArtifactoryRestClient artifactoryRestClient

  def setup() {
    artifactoryRestClient = new ArtifactoryRestClient('a', 'b')
    artifactoryRestClient.restClient = Mock(RESTClient)
  }

  def "for non 'release' build empty list of issues is returned"() {
    setup:
    def result = [data:
                      [buildInfo:
                           [statuses: [
                               [status: 'staging', timestamp: '1'],
                               [status: 'staging', timestamp: '2']
                           ],
                            issues  : [
                                affectedIssues: [[key: 'K-1'], [key: 'T-2']]
                            ]]]]
    artifactoryRestClient.restClient.get(_) >> result

    expect:
    artifactoryRestClient.getReleasedIssues('Agent', '42') == []
  }

  def "for 'release' build all connected issues are returned"() {
    setup:
    def result = [data:
                      [buildInfo:
                           [statuses: [
                               [status: 'release', timestamp: '2'],
                               [status: 'staging', timestamp: '1']
                           ],
                            issues  : [
                                affectedIssues: [[key: 'K-1'], [key: 'T-2']]
                            ]]]]
    artifactoryRestClient.restClient.get(_) >> result

    expect:
    artifactoryRestClient.getReleasedIssues('Agent', '42') == ['K-1', 'T-2']
  }

  def "can handle build info without statuses"() {
    setup:

    def result = [data:
                      [buildInfo:
                           [issues: [
                               affectedIssues: [[key: 'K-1'], [key: 'T-2']]
                           ]]]]
    this.artifactoryRestClient.restClient.get(_) >> result

    expect:
    this.artifactoryRestClient.getReleasedIssues('Agent', '42') == []
  }

  def "can handle non-existing build number"() {
    setup:
    artifactoryRestClient.restClient.get(_) >> { throw Mock(HttpResponseException) }

    expect:
    artifactoryRestClient.getReleasedIssues('Agent', '42') == []
  }
}
