package eu.plumbr.integration.plugin

import org.gradle.api.DefaultTask
import org.gradle.api.Project

class BuildNumberTask extends DefaultTask {

  static void apply(Project project) {
    def buildNumber
    if (System.properties['SOURCE_BUILD_NUMBER'] != null) {
      buildNumber = System.properties['SOURCE_BUILD_NUMBER']
    } else if (System.getenv().BUILD_NUMBER != null) {
      buildNumber = System.getenv().BUILD_NUMBER
    } else {
      buildNumber = 'SNAPSHOT'
    }

    project.ext.buildNumber = buildNumber
  }
}