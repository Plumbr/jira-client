package eu.plumbr.plugins.jira

import com.atlassian.jira.rest.client.JiraRestClient
import com.atlassian.jira.rest.client.NullProgressMonitor
import com.atlassian.jira.rest.client.domain.Comment
import com.atlassian.jira.rest.client.domain.Issue
import com.atlassian.jira.rest.client.domain.Project as JiraProject
import com.atlassian.jira.rest.client.domain.Transition
import com.atlassian.jira.rest.client.domain.input.TransitionInput
import com.atlassian.jira.rest.client.internal.jersey.JerseyJiraRestClientFactory
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import groovyx.net.http.RESTClient
import groovyx.net.http.ContentType


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
        project.task('changeJiraStatusTask', type: JiraStatusTask)
        project.extensions.create('jira', JiraPluginExtension)
        if (project.hasProperty('jiraUser')) {
            project.jira.user = project.property('jiraUser')
        }
        if (project.hasProperty('jiraPassword')) {
            project.jira.password = project.property('jiraPassword')
        }
        if (project.hasProperty('artifactoryUser')) {
            project.jira.artifactoryUser = project.property('artifactoryUser')
        }
        if (project.hasProperty('jiraPassword')) {
            project.jira.artifactoryPassword = project.property('artifactoryPassword')
        }
    }
}

class JiraPluginExtension {
    String user
    String password
    String version
    String artifactoryUser
    String artifactoryPassword

    public String getVersion() {
        if (version == null && user != null) {
            version = JiraPlugin.earliestUnreleasedVersion(user, password)
        }
        version
    }

}

class JiraStatusTask extends DefaultTask {

    @Input
    String buildNum

    @TaskAction
    def executeStatusChange() {
        changeJiraStatus(buildNum)
    }

    public void changeJiraStatus(def buildNum) {
        def restClient = new RESTClient("https://plumbr.artifactoryonline.com/plumbr/api/build/dashboard/${buildNum}")
        restClient.auth.basic project.jira.artifactoryUser, project.jira.artifactoryPassword
        def jsonResponse = restClient.get(contentType: ContentType.JSON).data
        if (_isRelease(jsonResponse))
            if (_getKeys(jsonResponse) != null)
                _getKeys(jsonResponse).each { v -> _toClosedStatus(v) }
            else
                println("No issues for build #${buildNum}")
        else
            println("Build #${buildNum} isn't in RELEASE status.")
    }

    private void _toClosedStatus(def issueKey) {
        final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
        final URI jiraServerUri = new URI("https://plumbr.atlassian.net");
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, project.jira.user, project.jira.password);
        final NullProgressMonitor pm = new NullProgressMonitor();
        final Issue issue = restClient.getIssueClient().getIssue(issueKey, pm)
        final Iterable<Transition> availableTransitions = restClient.issueClient.getTransitions(issue.getTransitionsUri(), pm);
        final Transition closeIssueTransition = _getTransitionById(availableTransitions, 701);
        if(closeIssueTransition != null) {
            final TransitionInput transitionInput = new TransitionInput(closeIssueTransition.getId(),
                    Comment.valueOf("Issue was closed automatically from JiraPlugin."));
            restClient.getIssueClient().transition(issue.getTransitionsUri(), transitionInput, pm);
            println("SUCCESS: Status for task #${issueKey} was changed to CLOSED");
        }
        else
            println("Problem during status change for task: #${issueKey}. NOTE: Current task status should be RESOLVED");
    }

    private static boolean _isRelease(def response) {
        if (response["buildInfo"]["statuses"] == null)
            return false
        else
            return response["buildInfo"]["statuses"].last()["status"] == "release"
    }

    private static List<String> _getKeys(def response) {
        if (response["buildInfo"]["issues"]["affectedIssues"] == null)
            return null
        else {
            def keys = new ArrayList<String>()
            response["buildInfo"]["issues"]["affectedIssues"].each { v -> keys.add(v["key"]) }
            return keys
        }
    }

    private static Transition _getTransitionById(Iterable<Transition> transitions, int transitionId) {
        for (Transition transition : transitions) {
            if (transition.getId().equals(transitionId)) {
                return transition
            }
        }
        return null
    }
}