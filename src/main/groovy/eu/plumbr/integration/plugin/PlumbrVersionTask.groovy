package eu.plumbr.integration.plugin

import eu.plumbr.integration.PlumbrVersion
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.joda.time.DateTime

class PlumbrVersionTask extends DefaultTask {

  public static final String SOURCE_VERSION = 'SOURCE_VERSION'
  public static final String BUILD_NUMBER = 'BUILD_NUMBER'
  public static final String SNAPSHOT = 'SNAPSHOT'

  private static String defaultVersion() {
    return new DateTime().toString('YY.MM.dd')
  }

  static void apply(Project project) {
    String sourceVersion = System.getProperty(SOURCE_VERSION)
    String buildNumber = System.getenv(BUILD_NUMBER)
    if (sourceVersion) {
      project.version = new PlumbrVersion(sourceVersion)
    } else if (buildNumber) {
      project.version = new PlumbrVersion(defaultVersion(), buildNumber)
    } else {
      project.version = new PlumbrVersion(defaultVersion(), SNAPSHOT)
    }
  }
}