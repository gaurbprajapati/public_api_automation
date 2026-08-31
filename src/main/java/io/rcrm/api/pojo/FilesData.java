package io.rcrm.api.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;

public class FilesData {
	private String objectKey;
	private String fileName;
	private String size;
	private String contentType;

	public FilesData(String objectKey, String fileName, String size, String contentType) {
		this.objectKey = objectKey;
		this.fileName = fileName;
		this.size = size;
		this.contentType = contentType;
	}

	public String getObjectKey() {
		return objectKey;
	}

	public String getFileName() {
		return fileName;
	}

	public String getSize() {
		return size;
	}

	public String getContentType() {
		return contentType;
	}
}
