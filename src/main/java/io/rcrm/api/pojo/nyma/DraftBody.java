package io.rcrm.api.pojo.nyma;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
public class DraftBody {
    private int from;
    private Recipients to;
	private String body;
	private String subject;

	@JsonProperty("include_signature")
	private boolean includeSignature;

    @JsonProperty("include_opt_out_link")
	private boolean includeOptOutLink;
}