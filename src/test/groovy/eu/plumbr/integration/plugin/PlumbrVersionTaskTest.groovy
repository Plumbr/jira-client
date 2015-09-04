package eu.plumbr.integration.plugin

import eu.plumbr.integration.PlumbrVersion
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class PlumbrVersionTaskTest extends Specification {

  Project project

  def setup() {
    project = ProjectBuilder.builder().build()
  }

  def "developer will have new version with SNAPSHOT buildNumber"() {
    when:
    PlumbrVersionTask.apply(project)
    PlumbrVersion version = project.version as PlumbrVersion

    then:
    version.buildNumber == 'SNAPSHOT'
  }

  def "buildNumber can be overridden via environment"() {
    setup:
    GroovyMock(System, global: true)
    System.getenv(PlumbrVersionTask.BUILD_NUMBER) >> '4269'

    when:
    PlumbrVersionTask.apply(project)
    PlumbrVersion version = project.version as PlumbrVersion

    then:
    version.buildNumber == '4269'
  }

  def "version can be overridden via system properties"() {
    setup:
    GroovyMock(System, global: true)
    System.getProperty(PlumbrVersionTask.SOURCE_VERSION) >> '15.09.04.1234'

    when:
    PlumbrVersionTask.apply(project)
    PlumbrVersion version = project.version as PlumbrVersion

    then:
    version.buildNumber == '1234'
    version.toString() == '15.09.04.1234'
  }

  def "version override wins over buildNumber override"() {
    setup:
    GroovyMock(System, global: true)
    System.getenv(PlumbrVersionTask.BUILD_NUMBER) >> '4269'
    System.getProperty(PlumbrVersionTask.SOURCE_VERSION) >> '15.09.04.9876'

    when:
    PlumbrVersionTask.apply(project)
    PlumbrVersion version = project.version as PlumbrVersion

    then:
    version.buildNumber == '9876'
    version.toString() == '15.09.04.9876'
  }

}
