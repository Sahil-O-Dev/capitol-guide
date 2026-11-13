package dev.capitolguide.member;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberControllerTest {
    private final MemberRepository members = mock(MemberRepository.class);
    private final MemberTermRepository terms = mock(MemberTermRepository.class);
    private final MemberPartyHistoryRepository parties = mock(MemberPartyHistoryRepository.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new MemberController(new MemberResponseMapper(), members, terms, parties)).build();

    @Test
    void returnsTheSavedProfileWithBothHistories() throws Exception {
        Member member = fictionalMember();
        when(members.findById("DEV_PROFILE001")).thenReturn(Optional.of(member));
        MemberTerm term = new MemberTerm();
        term.setChamber("House of Representatives");
        term.setCongress(118);
        term.setDistrict(1);
        term.setMemberType("Representative");
        term.setStartYear(2023);
        term.setEndYear(2025);
        term.setStateCode("VA");
        term.setStateName("Virginia");
        when(terms.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_PROFILE001"))
                .thenReturn(List.of(term));
        MemberPartyHistory party = new MemberPartyHistory();
        party.setPartyAbbreviation("T");
        party.setPartyName("Test Party");
        party.setStartYear(2023);
        when(parties.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_PROFILE001"))
                .thenReturn(List.of(party));

        mvc.perform(get("/api/members/DEV_PROFILE001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bioguideId").value("DEV_PROFILE001"))
                .andExpect(jsonPath("$.name").value("Fictional Profile Member"))
                .andExpect(jsonPath("$.partyName").value("Test Party"))
                .andExpect(jsonPath("$.state").value("Virginia"))
                .andExpect(jsonPath("$.chamber").value("Senate"))
                .andExpect(jsonPath("$.district").isEmpty())
                .andExpect(jsonPath("$.chamberStartYear").value(2025))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/fictional.jpg"))
                .andExpect(jsonPath("$.imageAttribution").value("Fictional photo credit"))
                .andExpect(jsonPath("$.officialWebsiteUrl").value("https://example.com/fictional"))
                .andExpect(jsonPath("$.currentMember").value(true))
                .andExpect(jsonPath("$.sourceUpdateDate").value("2026-11-13T12:00:00Z"))
                .andExpect(jsonPath("$.terms.length()").value(1))
                .andExpect(jsonPath("$.terms[0].chamber").value("House of Representatives"))
                .andExpect(jsonPath("$.terms[0].congress").value(118))
                .andExpect(jsonPath("$.terms[0].district").value(1))
                .andExpect(jsonPath("$.terms[0].memberType").value("Representative"))
                .andExpect(jsonPath("$.terms[0].startYear").value(2023))
                .andExpect(jsonPath("$.terms[0].endYear").value(2025))
                .andExpect(jsonPath("$.terms[0].stateCode").value("VA"))
                .andExpect(jsonPath("$.terms[0].stateName").value("Virginia"))
                .andExpect(jsonPath("$.partyHistory.length()").value(1))
                .andExpect(jsonPath("$.partyHistory[0].partyAbbreviation").value("T"))
                .andExpect(jsonPath("$.partyHistory[0].partyName").value("Test Party"))
                .andExpect(jsonPath("$.partyHistory[0].startYear").value(2023))
                .andExpect(jsonPath("$.partyHistory[0].endYear").isEmpty())
                .andExpect(jsonPath("$.terms[0].member").doesNotExist())
                .andExpect(jsonPath("$.partyHistory[0].member").doesNotExist());
    }

    @Test
    void returns404WithoutReadingHistoriesForAnUnknownId() throws Exception {
        when(members.findById("DEV_MISSING")).thenReturn(Optional.empty());

        mvc.perform(get("/api/members/DEV_MISSING")).andExpect(status().isNotFound());

        verifyNoInteractions(terms, parties);
    }

    @Test
    void returnsEmptyArraysWhenNoHistoriesAreStored() throws Exception {
        when(members.findById("DEV_PROFILE001")).thenReturn(Optional.of(fictionalMember()));
        when(terms.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_PROFILE001"))
                .thenReturn(List.of());
        when(parties.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_PROFILE001"))
                .thenReturn(List.of());

        mvc.perform(get("/api/members/DEV_PROFILE001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.terms").isArray())
                .andExpect(jsonPath("$.terms.length()").value(0))
                .andExpect(jsonPath("$.partyHistory").isArray())
                .andExpect(jsonPath("$.partyHistory.length()").value(0));
    }

    @Test
    void preservesTheExistingListResponse() throws Exception {
        when(members.findByStateAndChamber("Virginia", "Senate"))
                .thenReturn(List.of(fictionalMember()));

        mvc.perform(get("/api/members").param("state", "Virginia").param("chamber", "Senate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bioguideId").value("DEV_PROFILE001"))
                .andExpect(jsonPath("$[0].terms").doesNotExist())
                .andExpect(jsonPath("$[0].partyHistory").doesNotExist())
                .andExpect(jsonPath("$[0].officialWebsiteUrl").doesNotExist());

        verifyNoInteractions(terms, parties);
    }

    @Test
    void stillRequiresBothListFilters() throws Exception {
        mvc.perform(get("/api/members").param("state", "Virginia"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/members").param("chamber", "Senate"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(members, terms, parties);
    }

    private Member fictionalMember() {
        Member member = new Member();
        member.setBioguideId("DEV_PROFILE001");
        member.setName("Fictional Profile Member");
        member.setPartyName("Test Party");
        member.setState("Virginia");
        member.setChamber("Senate");
        member.setChamberStartYear(2025);
        member.setImageUrl("https://example.com/fictional.jpg");
        member.setImageAttribution("Fictional photo credit");
        member.setOfficialWebsiteUrl("https://example.com/fictional");
        member.setCurrentMember(true);
        member.setSourceUpdateDate(Instant.parse("2026-11-13T12:00:00Z"));
        return member;
    }
}
