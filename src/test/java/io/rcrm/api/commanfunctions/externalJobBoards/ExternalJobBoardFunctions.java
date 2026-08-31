package io.rcrm.api.commanfunctions.externalJobBoards;

import java.util.Map;

import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.JobBoard;
import io.rcrm.api.pojo.externalJobBoards.JobBoardSettings;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

public class ExternalJobBoardFunctions extends TestBase {

	JobBoard jobBoard = new JobBoard();
	JobBoardSettings jobBoardSettings = new JobBoardSettings();
	
	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();

	String userName = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();

	public ExternalJobBoardFunctions() {
		super();
	}

	public Response createExternalJobBoardResponse(String basePath, String externalJobBoardsServiceURL,
			Object authTokenMap) {

		jobBoardSettings.setUserEmail(userName);
		jobBoardSettings.setPassword(password);

		jobBoard.setJob_board_id(javaFakerJobBoards.getJobBoardId());
		jobBoard.setSettings(jobBoardSettings);

		Response response = RestClient.doPost("JSON", externalJobBoardsServiceURL, basePath, authTokenMap, null, true, jobBoard);

		return response;
	}

}
