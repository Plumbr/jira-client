package eu.plumbr.integration.jira

import com.atlassian.jira.rest.client.IssueRestClient
import com.atlassian.jira.rest.client.NullProgressMonitor
import com.atlassian.jira.rest.client.ProgressMonitor
import com.atlassian.jira.rest.client.domain.Comment
import com.atlassian.jira.rest.client.domain.Issue
import com.atlassian.jira.rest.client.domain.Project
import com.atlassian.jira.rest.client.domain.input.TransitionInput
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory

class JiraRestClient implements JiraClient {
  private final ProgressMonitor progressMonitor = new NullProgressMonitor()
  private final URI jiraServerUri = new URI("https://plumbr.atlassian.net");
  private final String jiraUser
  private final String jiraPassword
  private final com.atlassian.jira.rest.client.JiraRestClient jiraClient

  JiraRestClient(String jiraUser, String jiraPassword) {
    this.jiraPassword = jiraPassword
    this.jiraUser = jiraUser
    this.jiraClient = new JerseyJiraRestClientFactory().createWithBasicHttpAuthentication(jiraServerUri, jiraUser, jiraPassword)
  }

  @Override
  String currentVersion() {
    final Project p = jiraClient.getProjectClient().getProject("DTS", progressMonitor);
    p.getVersions().findAll { !it.isReleased() }.sort { it.releaseDate }.first().name
  }

  @Override
  void closeResolvedIssue(String issueKey) {
    IssueRestClient issueClient = jiraClient.issueClient
    final Issue issue = issueClient.getIssue(issueKey, progressMonitor)
    final TransitionInput transitionInput = new TransitionInput(701, Comment.valueOf("Issue was closed automatically from JiraPlugin."));
    issueClient.transition(issue.getTransitionsUri(), transitionInput, progressMonitor);
    println("SUCCESS: Status for task #${issueKey} was changed to CLOSED");
  }

}
