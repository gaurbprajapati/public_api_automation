package io.rcrm.api.pojo.candidateService;

import java.util.List;

public class PitchCandidateFields {
    private List<SavedPitchedField> savedPitchedFields;

    // Constructors
    public PitchCandidateFields() {
    }

    public PitchCandidateFields(List<SavedPitchedField> savedPitchedFields) {
        this.savedPitchedFields = savedPitchedFields;
    }

    // Getters and Setters
    public List<SavedPitchedField> getSavedPitchedFields() {
        return savedPitchedFields;
    }

    public void setSavedPitchedFields(List<SavedPitchedField> savedPitchedFields) {
        this.savedPitchedFields = savedPitchedFields;
    }
}
