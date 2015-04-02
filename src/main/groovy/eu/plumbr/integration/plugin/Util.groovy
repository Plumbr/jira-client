package eu.plumbr.integration.plugin

import org.gradle.api.Project

class Util {
  public static String propertySafe(Project project, String name) {
    project.hasProperty(name) ? project.property(name) : null
  }
}