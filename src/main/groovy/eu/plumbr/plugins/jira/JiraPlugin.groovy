package eu.plumbr.plugins.jira

import com.atlassian.jira.rest.client.JiraRestClient
import com.atlassian.jira.rest.client.NullProgressMonitor
import com.atlassian.jira.rest.client.domain.Project as JiraProject
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory
import org.gradle.api.Plugin
import org.gradle.api.Project

class JiraPlugin implements Plugin<Project> {

  public static String earliestUnreleasedVersion(String jiraUser, String jiraPassword) {
    final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
    final URI jiraServerUri = new URI("https://plumbr.atlassian.net");
    final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, jiraUser, jiraPassword);
    final NullProgressMonitor pm = new NullProgressMonitor();
    final JiraProject p = restClient.getProjectClient().getProject("DTS", pm);

    p.getVersions().findAll { !it.isReleased() }.sort { it.releaseDate }.first().name
  }

  @Override
  void apply(Project project) {
    project.extensions.create('jira', JiraPluginExtension)
    if (project.hasProperty('jiraUser')) {
      project.jira.user = project.property('jiraUser')
    }
    if (project.hasProperty('jiraPassword')) {
      project.jira.password = project.property('jiraPassword')
    }
  }
}

class JiraPluginExtension {
  String user
  String password
  String version

  public String getVersion() {
    if (version == null && user != null) {
      version = JiraPlugin.earliestUnreleasedVersion(user, password)
    }
    version
  }

}