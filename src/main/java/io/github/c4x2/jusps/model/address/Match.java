package io.github.c4x2.jusps.model.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {

    @JsonProperty("code")
    private String code;

    @JsonProperty("text")
    private String text;

    @Override
    public String toString() {
        return "[" + code + "] " + text;
    }
}
