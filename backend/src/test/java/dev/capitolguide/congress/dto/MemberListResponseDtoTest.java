package dev.capitolguide.congress.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberListResponseDtoTest {

    @Test
    void readsMemberName() {
        String json = """
                {
                  "members": [
                    {
                      "name": "Walkinshaw, James R."
                    }
                  ]
                }
                """;

        JsonMapper mapper = JsonMapper.builder().build();

        MemberListResponseDto response = mapper.readValue(json, MemberListResponseDto.class);

        assertEquals("Walkinshaw, James R.", response.getMembers().get(0).getName());


    }
}