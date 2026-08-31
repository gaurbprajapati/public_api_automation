package io.rcrm.api.pojo;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Files {

	private String related_to;
	private String related_to_type;
	
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private String folder;
	
	private ArrayList<String> files;

	public String getRelated_to() {
		return related_to;
	}

	public void setRelated_to(String related_to) {
		this.related_to = related_to;
	}

	public String getRelated_to_type() {
		return related_to_type;
	}

	public void setRelated_to_type(String related_to_type) {
		this.related_to_type = related_to_type;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public ArrayList<String> getFiles() {
		return files;
	}

	public void setFiles(ArrayList<String> files) {
		this.files = files;
	}

}
