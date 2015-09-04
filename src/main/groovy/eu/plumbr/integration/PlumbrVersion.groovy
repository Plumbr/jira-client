package eu.plumbr.integration

import groovy.transform.Canonical

@Canonical
class PlumbrVersion {
  final String version
  final String buildNumber

  PlumbrVersion(String version, String buildNumber) {
    this.version = version
    this.buildNumber = buildNumber
  }

  PlumbrVersion(String fullVersion) {
    if (fullVersion.contains('.')) {
      def matcher = fullVersion =~ /(\d+.\d+.\d+).?(\d*)/
      this.version = matcher[0][1]
      this.buildNumber = matcher[0][2] ?: null //to change '' to null
    } else {
      this.version = null
      this.buildNumber = fullVersion
    }
  }

  String toString() {
    [version, buildNumber].findAll().join('.')
  }
}
