package io.recruitcrm.albatross.Appointment;

import com.qa.api.util.reaper.ThreadManager;
import io.rcrm.api.commanfunctions.commanFunction;
import io.rcrm.api.pojo.albatross.Activites.*;
import io.rcrm.api.testbase.TestBase;
import io.rcrm.api.testbase.TestBase.AccountType;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import io.rcrm.api.restclient.RestClient;
import io.restassured.response.Response;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import com.qa.api.util.Owner;


@AccountType("Business|AlbatrossTkn")
public class AddEditAppointmentTest extends TestBase {

    commanFunction function = new commanFunction();
    String entitySlug,candidateName,email= null;
    int appointmentId,ownerId;
    String associatedCandidatesSlugs, associatedCompaniesSlugs, associatedContactsSlugs, associatedJobsSlugs, associatedDealsSlugs = "";


    @Owner("Harika")
    @Test(groups = "nightly-build")
    public void addAppointment() {
        JsonPath jsonCandidate = function.createNewCandidateWithMandatoryFields(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        entitySlug = jsonCandidate.get("slug");

        JsonPath userId = function.getUsers(baseURL, ThreadManager.getAccountApiKey()).jsonPath();
        ownerId = userId.get("id[0]");
        candidateName = jsonCandidate.get("first_name")+" "+jsonCandidate.get("last_name");
        email = jsonCandidate.get("email");

        Appointment appointment = new Appointment();
        appointment.setTitle("Appointment Title");
        appointment.setDescription("Appointment Description");
        appointment.setStatus(0);
        appointment.setType(2);
        appointment.setStartdate(System.currentTimeMillis());
        appointment.setEnddate(System.currentTimeMillis() + 3600000);
        appointment.setReminder(String.valueOf(30));
        appointment.setAddress("");
        appointment.setAllday(1);
        appointment.setOwnerid(ownerId);
        appointment.setAccountid(ThreadManager.getAccount().getAccountId());
        appointment.setEventid("");
        appointment.setCalendarid(null);
        appointment.setRelatedto(entitySlug);
        appointment.setRelatedtotypeid("5");
        appointment.setRelatedtoname(candidateName);
        appointment.setEmailbatchid("");
        appointment.setHtmllink("");
        appointment.setNoCalInvites(0);

        AddUpdateAppointment addAppointment = new AddUpdateAppointment();
        addAppointment.setTask(false);
        addAppointment.setAppointment(appointment);
        addAppointment.setCollaborator(new ArrayList<>());
        addAppointment.setCollaborator_team_ids(new ArrayList<>());
        addAppointment.setCollaborator_user_ids(new ArrayList<>());

        Response response = RestClient.doPost("JSON", albatrossURL, "meetings", ThreadManager.getOwnerAlbatrossToken(), null, true, addAppointment);

        assertThat(response.getStatusCode(), Matchers.equalTo(200));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Meeting Added"));
        response.then().body("data.appointment.id", Matchers.notNullValue());

        appointmentId = response.jsonPath().get("data.appointment.id");
    }

    @Owner("Harika")
    @Test(dependsOnMethods = {"addAppointment"}, groups = "nightly-build")
    public void editAppointmentWithAssociations(){
        getDataForAssociates();
        Appointment appointment = new Appointment();
        appointment.setTitle("Appointment Title Updated");
        appointment.setDescription("Appointment Description Updated");
        appointment.setStatus(0);
        appointment.setType(2);
        appointment.setStartdate(System.currentTimeMillis());
        appointment.setEnddate(System.currentTimeMillis() + 3600000);
        appointment.setReminder(String.valueOf(30));
        appointment.setAddress("");
        appointment.setAllday(1);
        appointment.setOwnerid(ownerId);
        appointment.setAccountid(ThreadManager.getAccount().getAccountId());
        appointment.setEventid("");
        appointment.setCalendarid(null);
        appointment.setRelatedto(entitySlug);
        appointment.setRelatedtotypeid("5");
        appointment.setRelatedtoname(candidateName);
        appointment.setEmailbatchid("");
        appointment.setHtmllink("");
        appointment.setNoCalInvites(0);
        appointment.setId(appointmentId);

        AppointmentAttendee attendee = new AppointmentAttendee();
        attendee.setAttendeeid(entitySlug);
        attendee.setAttendeetype("5");
        attendee.setEmail(email);
        attendee.setIcon("person");
        attendee.setName(candidateName);

        ArrayList<AppointmentAttendee> attendees = new ArrayList<>();
        attendees.add(attendee);

        AddUpdateAppointment addAppointment = getAddUpdateAppointment(appointment, attendees);

        String basePath = "meetings/{id}";
        HashMap<String, String> pathParameters = new HashMap<String, String>();
        pathParameters.put("id", String.valueOf(appointmentId));

        Response response = RestClient.doPost1("JSON", albatrossURL, basePath, ThreadManager.getOwnerAlbatrossToken(), null,pathParameters, true, addAppointment);

        assertThat(response.getStatusCode(), Matchers.equalTo(200));
        response.then().body("status", Matchers.containsString("success"));
        response.then().body("message", Matchers.containsString("Meeting Updated"));
    }

    private AddUpdateAppointment getAddUpdateAppointment(Appointment appointment, ArrayList<AppointmentAttendee> attendees) {
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
            associationData.setActivity_id(appointmentId);
            associationData.setEvent_type("meeting");
            associationDataList.add(associationData);
        }

        AddUpdateAppointment addAppointment = new AddUpdateAppointment();
        addAppointment.setTask(false);
        addAppointment.setAppointment(appointment);
        addAppointment.setCollaborator(attendees);
        addAppointment.setAssociation_data(associationDataList);
        addAppointment.setCollaborator_team_ids(new ArrayList<>());
        addAppointment.setCollaborator_user_ids(new ArrayList<>());

        return addAppointment;
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
