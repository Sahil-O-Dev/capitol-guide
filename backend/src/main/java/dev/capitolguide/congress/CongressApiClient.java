package dev.capitolguide.congress;

import dev.capitolguide.congress.dto.MemberListResponseDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class CongressApiClient
{

    private final String apiKey;
    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.congress.gov/v3")
            .build();

    public CongressApiClient(@Value("${CONGRESS_API_KEY}") String apiKey)
    {
        this.apiKey = apiKey;
    }

    public MemberListResponseDto fetchMembers(int offset)
    {
        return restClient.get()
                .uri("/member?currentMember=true&format=json&limit=20&offset={offset}", offset)
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(MemberListResponseDto.class);
    }

    public List<MemberSummaryDto> fetchAllMembers()
    {
        int offset = 0;
        MemberListResponseDto response = fetchMembers(offset);
        List<MemberSummaryDto> allMembers = new ArrayList<>(response.getMembers());

        while(response.getPagination().getNext() != null)
        {
            offset += 20;
            response = fetchMembers(offset);
            allMembers.addAll(response.getMembers());
        }

        return allMembers;
    }
}