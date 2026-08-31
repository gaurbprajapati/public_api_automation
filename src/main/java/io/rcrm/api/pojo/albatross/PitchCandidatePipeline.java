package io.rcrm.api.pojo.albatross;

import java.util.ArrayList;

public class PitchCandidatePipeline {

    ArrayList<PitchPipelineStages> pitchPipelineStages=new ArrayList<>();

    public ArrayList<PitchPipelineStages> getPitchPipelineStages() {
        return pitchPipelineStages;
    }

    public void setPitchPipelineStages(ArrayList<PitchPipelineStages> pitchPipelineStages) {
        this.pitchPipelineStages = pitchPipelineStages;
    }
}
