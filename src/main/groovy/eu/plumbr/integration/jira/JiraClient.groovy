package eu.plumbr.integration.jira

interface JiraClient {

  String currentVersion()

  void closeResolvedIssue(String key)
}