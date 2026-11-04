package dev.capitolguide.congress;

import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class CongressApiClientTest
{
    @Test
    void fetchesSinglePage()
    {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        CongressApiClient client = new CongressApiClient("test-key", builder);

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=0"))
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

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=0"))
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
                             "next": "https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=20"
                           }
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=20"))
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

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=0"))
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
                             "next": "https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=20"
                           }
                        }
                        """, MediaType.APPLICATION_JSON));

        server.expect(requestTo("https://api.congress.gov/v3/member?currentMember=true&format=json&limit=20&offset=20"))
                .andRespond(withServerError());

        assertThrows(HttpServerErrorException.class,
                () -> client.fetchAllMembers());

        server.verify();
    }
}
