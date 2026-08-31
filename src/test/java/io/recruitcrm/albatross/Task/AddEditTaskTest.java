package io.recruitcrm.albatross.Task;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.Activites.AddUpdateTask;
import io.rcrm.api.pojo.albatross.Activites.AssociationData;
import io.rcrm.api.pojo.albatross.Activites.Task;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import com.qa.api.util.Owner;

@AccountType("Business|AlbatrossTkn")
public class AddEditTaskTest extends TestBase {

    commanFunction function = new commanFunction();
    String entitySlug,candidateName= null;
    int taskId,ownerId;
    String associatedCandidatesSlugs, associatedCompaniesSlugs, associatedContactsSlugs, associatedJobsSlugs, associatedDealsSlugs = "";

    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void addTask() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        entitySlug = jsonCandidate.get("slug");

        JsonPath userId = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        ownerId = userId.get("id[0]");
        candidateName = jsonCandidate.get("first_name")+" "+jsonCandidate.get("last_name");

        Task task = new Task();
        task.setTitle("Task Title");
        task.setDescription("Task Description");
        task.setStatus(0);
        task.setType(1);
        task.setStartdate(System.currentTimeMillis());
        task.setReminder(30);
        task.setAddress("");
        task.setAllday(0);
        task.setOwnerid(ownerId);
        task.setAccountid(ThreadManager.getAccount().getAccountId());
        task.setEventid("");
        task.setRelatedto(entitySlug);
        task.setRelatedtotypeid("5");
        task.setRelatedtoname(candidateName);
        task.setEmailbatchid("");

        AddUpdateTask addTask = new AddUpdateTask();
        addTask.setTask(task);
        addTask.setCollaborator_team_ids(new ArrayList<>());
        addTask.setCollaborator_user_ids(new ArrayList<>());

        Response response = RestClient.doPost("JSON", albatrossURL, "tasks", ThreadManager.getOwnerAlbatrossToken(), null, true, addTask);

        assertThat(response.getStatusCode(), Matchers.equalTo(200));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Task Added"));
        response.then().body("data.task.id", Matchers.notNullValue());

        taskId = response.jsonPath().get("data.task.id");
    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"addTask"}, groups = "nightly-build")
    public void EditTaskWithAssociations() {
        getDataForAssociates();

        Task task = new Task();
        task.setTitle("Task Title Updated");
        task.setDescription("Task Description Updated");
        task.setStatus(0);
        task.setType(1);
        task.setStartdate(System.currentTimeMillis());
        task.setReminder(30);
        task.setAddress("");
        task.setAllday(0);
        task.setOwnerid(ownerId);
        task.setAccountid(ThreadManager.getAccount().getAccountId());
        task.setEventid("");
        task.setRelatedto(entitySlug);
        task.setRelatedtotypeid("5");
        task.setRelatedtoname(candidateName);
        task.setEmailbatchid("");
        task.setId(taskId);

        AddUpdateTask addTask = getAddUpdateTask(task);

        String basePath = "tasks/{id}";
        HashMap<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(taskId));

        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,pathParameters, true, addTask);

        assertThat(response.getStatusCode(), Matchers.equalTo(200));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Task Updated"));
        response.then().body("data.task.id", Matchers.notNullValue());

        taskId = response.jsonPath().get("data.task.id");
    }

    private AddUpdateTask getAddUpdateTask(Task task) {
        Map<String, String> entityDataMap = new LinkedHashMap<>();
        entityDataMap.put("5", associatedCandidatesSlugs);
        entityDataMap.put("3", associatedCompaniesSlugs);
        entityDataMap.put("2", associatedContactsSlugs);
        entityDataMap.put("4", associatedJobsSlugs);
        entityDataMap.put("11", associatedDealsSlugs);

        List<AssociationData> associationDataList = new ArrayList<>();

        for (Map.Entry<String, String> entry : entityDataMap.entrySet()) {
            AssociationData associationData = new AssociationData();
            associationData.setAssociated_entity(entry.getValue());
            associationData.setAssociated_entity_type_id(entry.getKey());
            associationData.setActivity_id(taskId);
            associationData.setEvent_type("Task");
            associationDataList.add(associationData);
        }


        AddUpdateTask addTask = new AddUpdateTask();
        addTask.setTask(task);
        addTask.setAssociation_data(associationDataList);
        addTask.setCollaborator_team_ids(new ArrayList<>());
        addTask.setCollaborator_user_ids(new ArrayList<>());
        return addTask;
    }


    public void getDataForAssociates() {

        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        associatedCandidatesSlugs = jsonCandidate.get("slug");

        JsonPath jsonCompany = function.createNewCompanyWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        associatedCompaniesSlugs = jsonCompany.get("slug");

        JsonPath jsonContact = function.createNewContact_POST(baseURL, ThreadManager.getAccountApiKey(), associatedCompaniesSlugs).jsonPath();
        associatedContactsSlugs = jsonContact.get("slug");

        JsonPath jsonJob = function.createNewJob(baseURL, ThreadManager.getAccountApiKey(), associatedCompaniesSlugs, associatedContactsSlugs).jsonPath();
        associatedJobsSlugs = jsonJob.get("slug");

        JsonPath jsonDeal = function.createNewDealWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey(), associatedCompaniesSlugs, associatedContactsSlugs, associatedJobsSlugs).jsonPath();
        associatedDealsSlugs = jsonDeal.get("slug");

    }
}
