package dev.capitolguide.congress;

import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

class CongressApiClientTest
{
    @Test
    void rejectsMissingMemberDetails()
    {
        for (String body : List.of("", "null", "{}", "{\"member\":null}"))
        {
            RestClient.Builder builder = RestClient.builder();
            MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
            CongressApiClient client = new CongressApiClient("test-key", builder);

            server.expect(requestTo("https://api.congress.gov/v3/member/DEV_DETAIL001?format=json"))
                    .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

            IllegalStateException error = assertThrows(IllegalStateException.class,
                    () -> client.fetchDetailMember("DEV_DETAIL001"));

            assertEquals("Congress.gov returned no member details for ID DEV_DETAIL001", error.getMessage());
            server.verify();
        }
    }

    @Test
    void fetchesMemberDetails()
    {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CongressApiClient client = new CongressApiClient("test-key", builder);

        server.expect(requestTo("https://api.congress.gov/v3/member/DEV_DETAIL001?format=json"))
                .andRespond(withSuccess("""
                        {
                          "member": {
                            "bioguideId": "DEV_DETAIL001",
                            "directOrderName": "Fictional Detail Member",
                            "currentMember": true,
                            "state": "Virginia",
                            "district": 11,
                            "birthYear": "1982",
                            "depiction": {
                              "imageUrl": "https://example.com/fictional-member.jpg",
                              "attribution": "Fictional test photo credit"
                            },
                            "officialWebsiteUrl": "https://example.com/fictional-member",
                            "terms": [
                              {
                                "chamber": "House of Representatives",
                                "congress": 119,
                                "district": 11,
                                "memberType": "Representative",
                                "startYear": 2025,
                                "stateCode": "VA",
                                "stateName": "Virginia"
                              }
                            ],
                            "partyHistory": [
                              {
                                "partyAbbreviation": "T",
                                "partyName": "Test Party",
                                "startYear": 2025
                              }
                            ],
                            "updateDate": "2026-11-05T08:36:10Z"
                          },
                          "request": {
                            "bioguideId": "dev_detail001",
                            "format": "json"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        MemberDetailResponseDto response = client.fetchDetailMember("DEV_DETAIL001");
        MemberDetailDto member = response.getMember();

        assertEquals("DEV_DETAIL001", member.getBioguideId());
        assertEquals("Fictional Detail Member", member.getDirectOrderName());
        assertEquals(Boolean.TRUE, member.getCurrentMember());
        assertEquals("Virginia", member.getState());
        assertEquals(11, member.getDistrict());
        assertEquals("https://example.com/fictional-member", member.getOfficialWebsiteUrl());
        assertEquals("https://example.com/fictional-member.jpg", member.getDepiction().getImageUrl());
        assertEquals("Fictional test photo credit", member.getDepiction().getAttribution());
        assertEquals(1, member.getTerms().size());
        assertEquals("House of Representatives", member.getTerms().get(0).getChamber());
        assertEquals(119, member.getTerms().get(0).getCongress());
        assertEquals(11, member.getTerms().get(0).getDistrict());
        assertEquals("Representative", member.getTerms().get(0).getMemberType());
        assertEquals(2025, member.getTerms().get(0).getStartYear());
        assertEquals("VA", member.getTerms().get(0).getStateCode());
        assertEquals("Virginia", member.getTerms().get(0).getStateName());
        assertNull(member.getTerms().get(0).getEndYear());
        assertEquals(1, member.getPartyHistory().size());
        assertEquals("T", member.getPartyHistory().get(0).getPartyAbbreviation());
        assertEquals("Test Party", member.getPartyHistory().get(0).getPartyName());
        assertEquals(2025, member.getPartyHistory().get(0).getStartYear());
        assertNull(member.getPartyHistory().get(0).getEndYear());
        assertEquals("2026-11-05T08:36:10Z", member.getUpdateDate());

        server.verify();
    }

    @Test
    void fetchesSinglePage()
    {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        CongressApiClient client = new CongressApiClient("test-key", builder);

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=0"))
                .andRespond(withSuccess("""
                        {
                          "members": [
                            {
                              "bioguideId": "DEV001",
                              "name": "Fictional Test Member"
                            }
                          ],
                          "pagination": {
                            "count": 1
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<MemberSummaryDto> members = client.fetchAllMembers();

        assertEquals(1, members.size());
        assertEquals("Fictional Test Member", members.get(0).getName());

        server.verify();
    }

    @Test
    void fetchesMultiplePages()
    {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        CongressApiClient client = new CongressApiClient("test-key", builder);

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=0"))
                .andRespond(withSuccess("""
                        {
                          "members": [
                            {
                              "bioguideId": "DEV001",
                              "name": "Fictional Test Member"
                            }
                          ],
                          "pagination": {
                             "count": 2,
                             "next": "https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=250"
                           }
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=250"))
                .andRespond(withSuccess("""
                        {
                          "members": [
                            {
                              "bioguideId": "DEV002",
                              "name": "Second Test Member"
                            }
                          ],
                          "pagination": {
                             "count": 2
                           }
                        }
                        """, MediaType.APPLICATION_JSON));

        List<MemberSummaryDto> members = client.fetchAllMembers();

        assertEquals(2, members.size());
        assertEquals("Fictional Test Member", members.get(0).getName());
        assertEquals("Second Test Member", members.get(1).getName());


        server.verify();
    }

    @Test
    void failsWhenSecondPageFails()
    {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        CongressApiClient client = new CongressApiClient("test-key", builder);

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=0"))
                .andRespond(withSuccess("""
                        {
                          "members": [
                            {
                              "bioguideId": "DEV001",
                              "name": "Fictional Test Member"
                            }
                          ],
                          "pagination": {
                             "count": 2,
                             "next": "https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=250"
                           }
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=250"))
                .andRespond(withServerError());

        assertThrows(HttpServerErrorException.class,
                () -> client.fetchAllMembers());

        server.verify();
    }

    @Test
    void failsWhenRateLimited()
    {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        CongressApiClient client = new CongressApiClient("test-key", builder);

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=250&offset=0"))
                .andRespond(withTooManyRequests());

        var error = assertThrows(HttpClientErrorException.class,
                () -> client.fetchAllMembers());

        assertEquals(429, error.getStatusCode().value());

        server.verify();
    }
}
