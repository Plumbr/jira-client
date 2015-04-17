package eu.plumbr.integration.jira

import com.atlassian.jira.rest.client.IssueRestClient
import com.atlassian.jira.rest.client.NullProgressMonitor
import com.atlassian.jira.rest.client.ProgressMonitor
import com.atlassian.jira.rest.client.RestClientException
import com.atlassian.jira.rest.client.domain.Comment
import com.atlassian.jira.rest.client.domain.Issue
import com.atlassian.jira.rest.client.domain.Project
import com.atlassian.jira.rest.client.domain.input.TransitionInput
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory
import org.slf4j.Logger

class JiraRestClient implements JiraClient {
  public static final int MAGIC_ID_OF_OUR_TRANSITION = 701
  private final ProgressMonitor progressMonitor = new NullProgressMonitor()
  private final URI jiraServerUri = new URI("https://plumbr.atlassian.net");
  private final String jiraUser
  private final String jiraPassword
  private final com.atlassian.jira.rest.client.JiraRestClient jiraClient
  private final Logger log

  JiraRestClient(String jiraUser, String jiraPassword, Logger logger) {
    this.log = logger
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
    final TransitionInput transitionInput = new TransitionInput(MAGIC_ID_OF_OUR_TRANSITION, Comment.valueOf("Issue was closed automatically from JiraPlugin."));
    try {
      issueClient.transition(issue.getTransitionsUri(), transitionInput, progressMonitor);
      log.info("SUCCESS: Status for task {} was changed to CLOSED", issueKey);
    } catch (RestClientException ex) {
      log.warn("Failed to close task {}. May be it is already closed?", issueKey)
      log.debug("REST failed", ex);
    }
  }

}
