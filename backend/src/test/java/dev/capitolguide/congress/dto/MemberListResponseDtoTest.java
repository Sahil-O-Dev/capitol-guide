package dev.capitolguide.congress.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MemberListResponseDtoTest {

    @Test
    void readsMemberNameAndImageUrl() {
        String json = """
                {
                  "members": [
                    {
                      "name": "Walkinshaw, James R.",
                      "depiction": {
                        "imageUrl": "https://example.com/member.jpg"
                      }
                    }
                  ]
                }
                """;

        JsonMapper mapper = JsonMapper.builder().build();

        MemberListResponseDto response =
                mapper.readValue(json, MemberListResponseDto.class);

        assertEquals("Walkinshaw, James R.",
                response.getMembers().get(0).getName());

        assertEquals("https://example.com/member.jpg",
                response.getMembers().get(0).getDepiction().getImageUrl());
    }

    @Test
    void readsMemberWithoutDepiction()
    {
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

        assertNull(response.getMembers().get(0).getDepiction());
    }
}