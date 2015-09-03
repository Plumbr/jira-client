package eu.plumbr.integration

import spock.lang.Specification
import spock.lang.Unroll

class PlumbrVersionTest extends Specification {

  @Unroll
  def "correctly parses #fullVersion to #version and #buildNumber"() {
    when:
    def pv = new PlumbrVersion(fullVersion)

    then:
    pv.version == version
    pv.buildNumber == buildNumber

    where:
    fullVersion     | version    | buildNumber
    '15.09.03.4492' | '15.09.03' | '4492'
    '1.2.3'         | '1.2.3'    | null
    '4269'          | null       | '4269'
  }

  def "returns full version as toString"() {
    expect:
    new PlumbrVersion(fullVersion).toString() == fullVersion

    where:
    fullVersion << ['15.09.03.4492', '1.2.3', '4269']
  }
}
