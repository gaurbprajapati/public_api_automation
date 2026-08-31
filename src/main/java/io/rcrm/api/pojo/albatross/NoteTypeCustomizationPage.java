package io.rcrm.api.pojo.albatross;

import java.util.ArrayList;

public class NoteTypeCustomizationPage {
	
	ArrayList<Object> customizedNoteTypes = new ArrayList<>();

	public NoteTypeCustomizationPage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NoteTypeCustomizationPage(ArrayList<Object> customizedNoteTypes) {
		super();
		this.customizedNoteTypes = customizedNoteTypes;
	}

	public ArrayList<Object> getCustomizedNoteTypes() {
		return customizedNoteTypes;
	}

	public void setCustomizedNoteTypes(ArrayList<Object> customizedNoteTypes) {
		this.customizedNoteTypes = customizedNoteTypes;
	}
	
	

}
