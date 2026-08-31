package com.qa.api.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtil {

	/**
	 * This method is used to convert POJO Object to a JSON String...
	 * 
	 * @param obj
	 * @return jsonString
	 */

	public static String getSerializedJSON(Object obj) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonString = null;
		try {
			jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		return jsonString;
	}

}
