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

    @Test
    void readsMemberWithoutDistrict()
    {
        String json = """
        {
          "members": [
            {
              "bioguideId": "B001323",
              "depiction": {
                "attribution": "Image courtesy of the Member",
                "imageUrl": "https://www.congress.gov/img/member/6774217e0b34857ecc909040_200.jpg"
              },
              "name": "Begich, Nicholas J.",
              "partyName": "Republican",
              "state": "Alaska",
              "terms": {
                "item": [
                  {
                    "chamber": "House of Representatives",
                    "startYear": 2025
                  }
                ]
              },
              "updateDate": "2026-10-30T07:41:07Z",
              "url": "https://api.congress.gov/v3/member/B001323?format=json"
            },
            {
              "bioguideId": "S001198",
              "depiction": {
                "attribution": "<a href=\\"http://www.senate.gov/artandhistory/history/common/generic/Photo_Collection_of_the_Senate_Historical_Office.htm\\">Courtesy U.S. Senate Historical Office</a>",
                "imageUrl": "https://www.congress.gov/img/member/s001198_200.jpg"
              },
              "name": "Sullivan, Dan",
              "partyName": "Republican",
              "state": "Alaska",
              "terms": {
                "item": [
                  {
                    "chamber": "Senate",
                    "startYear": 2015
                  }
                ]
              },
              "updateDate": "2026-03-08T10:32:48Z",
              "url": "https://api.congress.gov/v3/member/S001198?format=json"
            },
            {
              "bioguideId": "M001153",
              "depiction": {
                "attribution": "<a href=\\"http://www.senate.gov/artandhistory/history/common/generic/Photo_Collection_of_the_Senate_Historical_Office.htm\\">Courtesy U.S. Senate Historical Office</a>",
                "imageUrl": "https://www.congress.gov/img/member/m001153_200.jpg"
              },
              "name": "Murkowski, Lisa",
              "partyName": "Republican",
              "state": "Alaska",
              "terms": {
                "item": [
                  {
                    "chamber": "Senate",
                    "startYear": 2002
                  }
                ]
              },
              "updateDate": "2026-03-08T10:32:09Z",
              "url": "https://api.congress.gov/v3/member/M001153?format=json"
            }
          ],
          "pagination": {
            "count": 3
          },
          "request": {
            "contentType": "application/json",
            "format": "json"
          }
        }
        """;

        JsonMapper mapper = JsonMapper.builder().build();

        MemberListResponseDto response = mapper.readValue(json, MemberListResponseDto.class);

        assertNull(response.getMembers().get(0).getDistrict());
    }
}