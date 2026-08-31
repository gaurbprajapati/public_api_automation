package io.rcrm.api.commanfunctions.externalJobBoards;

import java.util.Map;

import io.rcrm.api.javafaker.JavaFakerExternalJobBoards;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonExternalJobBoard;
import io.rcrm.api.pojo.externalJobBoards.logicmelon.LogicmelonJobBoardSetting;
import io.rcrm.api.restclient.RestClient;
import io.rcrm.api.testbase.TestBase;
import io.restassured.response.Response;

public class LogicmelonJobBoardFunctions extends TestBase {
	LogicmelonExternalJobBoard jobBoard = new LogicmelonExternalJobBoard();
	LogicmelonJobBoardSetting jobBoardSetting = new LogicmelonJobBoardSetting();

	JavaFakerExternalJobBoards javaFakerJobBoards = new JavaFakerExternalJobBoards();

	String userName = javaFakerJobBoards.getEmailAddress();
	String password = javaFakerJobBoards.getPassword();

	public LogicmelonJobBoardFunctions() {
		super();
	}

	public Response createLogicmelonJobBoardFunctions(String basePath, String logicmelonJobBoardsServiceURL,
			Object authTokenMap, String apikey) {
		jobBoardSetting.setUsername(userName);
		jobBoardSetting.setPassword(password);
		jobBoardSetting.setApikey(apikey);

		jobBoard.setJob_board_id(2);
		jobBoard.setSettings(jobBoardSetting);
		jobBoard.setEnable_logicmelon_to_accounts_user(javaFakerJobBoards.getEnable_logicmelon());

		Response response = RestClient.doPost("JSON", logicmelonJobBoardsServiceURL, basePath, authTokenMap, null, true,
				jobBoard);

		return response;
	}
}
