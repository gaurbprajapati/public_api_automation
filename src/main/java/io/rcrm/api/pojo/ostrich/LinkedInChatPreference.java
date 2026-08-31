package io.rcrm.api.pojo.ostrich;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkedInChatPreference {

    @JsonProperty("hide_chat")
    private int hideChat;
}