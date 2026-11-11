package dev.capitolguide.congress;

import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailResponseDto;
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
    private static final int PAGE_SIZE = 250;

    private final String apiKey;
    private final RestClient restClient;

    public CongressApiClient(@Value("${CONGRESS_API_KEY}") String apiKey, RestClient.Builder builder)
    {
        this.apiKey = apiKey;
        this.restClient = builder
                .baseUrl("https://api.congress.gov/v3")
                .build();
    }

    public MemberListResponseDto fetchMembers(int offset)
    {
        return restClient.get()
                .uri("/member?currentMember=true&format=json&limit={limit}&offset={offset}", PAGE_SIZE, offset)
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
            offset += PAGE_SIZE;
            response = fetchMembers(offset);
            allMembers.addAll(response.getMembers());
        }

        return allMembers;
    }

    public MemberDetailResponseDto fetchDetailMember(String bioguideId)
    {

        MemberDetailResponseDto response = restClient.get()
                .uri("https://api.congress.gov/v3/member/{bioguideId}?format=json", bioguideId)
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(MemberDetailResponseDto.class);

        if (response == null || response.getMember() == null)
        {
            throw new IllegalStateException("Congress.gov returned no member details for ID " + bioguideId);
        }

        return response;
    }

    public List<MemberDetailResponseDto> fetchAllDetailMembers()
    {
        List<MemberSummaryDto> allMembers = fetchAllMembers();
        List<MemberDetailResponseDto> allDetailMembers = new ArrayList<>();

        for(MemberSummaryDto memberSummaryDto: allMembers)
        {
            String bioguideId = memberSummaryDto.getBioguideId();
            allDetailMembers.add(fetchDetailMember(bioguideId));
        }
        return allDetailMembers;
    }
}
