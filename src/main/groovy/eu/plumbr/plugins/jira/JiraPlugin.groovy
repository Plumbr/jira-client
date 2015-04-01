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
        final JiraRestClient restClient = JiraPluginBase.factory.createWithBasicHttpAuthentication(JiraPluginBase.jiraServerUri, jiraUser, jiraPassword);
        final JiraProject p = restClient.getProjectClient().getProject("DTS", JiraPluginBase.pm);
        p.getVersions().findAll { !it.isReleased() }.sort { it.releaseDate }.first().name
    }

    @Override
    void apply(Project project) {
        project.task('closeReleasedIssues', type: JiraStatusTask)
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
        changeJiraStatusit s(buildNum)
    }

    public void changeJiraStatus(def buildNum) {
        def restClient = new RESTClient("https://plumbr.artifactoryonline.com/plumbr/api/build/dashboard/${buildNum}")
        restClient.auth.basic project.jira.artifactoryUser, project.jira.artifactoryPassword
        def jsonResponse = restClient.get(contentType: ContentType.JSON).data
        if (isRelease(jsonResponse)) {
            def keys = getKeys(jsonResponse)
            if (keys != null)
                keys.each { v -> toClosedStatus(v) }
            else
                println("No issues for build #${buildNum}")
        }
        else
            println("Build #${buildNum} isn't in RELEASE status.")
    }

    private void toClosedStatus(def issueKey) {
        final JiraRestClient restClient = JiraPluginBase.factory.createWithBasicHttpAuthentication(JiraPluginBase.jiraServerUri, project.jira.user, project.jira.password);
        final Issue issue = restClient.getIssueClient().getIssue(issueKey, JiraPluginBase.pm)
        final Iterable<Transition> availableTransitions = restClient.issueClient.getTransitions(issue.getTransitionsUri(), JiraPluginBase.pm);
        final Transition closeIssueTransition = getTransitionById(availableTransitions, 701);
        if(closeIssueTransition != null) {
            final TransitionInput transitionInput = new TransitionInput(closeIssueTransition.getId(),
                    Comment.valueOf("Issue was closed automatically from JiraPlugin."));
            restClient.getIssueClient().transition(issue.getTransitionsUri(), transitionInput, JiraPluginBase.pm);
            println("SUCCESS: Status for task #${issueKey} was changed to CLOSED");
        }
        else
            println("Problem during status change for task: #${issueKey}. NOTE: Current task status should be RESOLVED");
    }

    private static boolean isRelease(def response) {
        if (response["buildInfo"]["statuses"] == null)
            return false
        else
            return response["buildInfo"]["statuses"].last()["status"] == "release"
    }

    private static List<Object> getKeys(def response) {
        if (response["buildInfo"]["issues"]["affectedIssues"] == null)
            return null
        else
            return response["buildInfo"]["issues"]["affectedIssues"].collect { it['key'] }
    }

    private static Transition getTransitionById(Iterable<Transition> transitions, int transitionId) {
        for (Transition transition : transitions) {
            if (transition.getId().equals(transitionId)) {
                return transition
            }
        }
        return null
    }
}

class JiraPluginBase {
    public static final JerseyJiraRestClientFactory factory = new JerseyJiraRestClientFactory();
    public static final URI jiraServerUri = new URI("https://plumbr.atlassian.net");
    public static final NullProgressMonitor pm = new NullProgressMonitor();
}