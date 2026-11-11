package dev.capitolguide.member;

import dev.capitolguide.congress.CongressApiClient;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailResponseDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;
import dev.capitolguide.congress.dto.TermListDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberPreparationTest {
    private final CongressApiClient client = mock(CongressApiClient.class);
    private final MemberRepository repository = mock(MemberRepository.class);
    private final MemberImportService service = new MemberImportService(client, new MemberMapper(), repository);

    @Test
    void combinesEachSummaryWithItsOwnDetailsWithoutSaving() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
        when(client.fetchDetailMember("DEV002")).thenReturn(detail("DEV002"));

        List<Member> members = service.prepareMembers();

        assertEquals(2, members.size());
        for (int i = 0; i < members.size(); i++) {
            String id = i == 0 ? "DEV001" : "DEV002";
            Member member = members.get(i);
            assertEquals(id, member.getBioguideId());
            assertEquals("Fictional Member " + id, member.getName());
            assertEquals("Senate", member.getChamber());
            assertEquals("https://example.com/" + id, member.getOfficialWebsiteUrl());
            assertEquals(Boolean.TRUE, member.getCurrentMember());
            assertEquals(Instant.parse("2026-11-05T08:36:10Z"), member.getSourceUpdateDate());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMissingSummaryIdBeforeRequestingDetails() {
        for (String id : new String[] {null, "", "   "}) {
            when(client.fetchAllMembers()).thenReturn(List.of(summary(id)));
            assertThrows(IllegalStateException.class, service::prepareMembers);
        }

        verify(client, never()).fetchDetailMember(any());
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMissingOrMismatchedDetailId() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001")));
        for (String id : new String[] {null, "", "DEV002"}) {
            when(client.fetchDetailMember("DEV001")).thenReturn(detail(id));

            IllegalStateException error = assertThrows(IllegalStateException.class, service::prepareMembers);

            assertEquals("Congress.gov returned mismatched member details for ID DEV001", error.getMessage());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void stopsWhenALaterDetailRequestFailsWithoutSavingEarlierMembers() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
        IllegalStateException failure = new IllegalStateException("Fictional API failure");
        when(client.fetchDetailMember("DEV002")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, service::prepareMembers));
        verifyNoInteractions(repository);
    }

    @Test
    void stopsWhenMappingFailsWithoutSavingEarlierMembers() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
        MemberDetailResponseDto malformed = detail("DEV002");
        malformed.getMember().setUpdateDate("not-a-date");
        when(client.fetchDetailMember("DEV002")).thenReturn(malformed);

        assertThrows(IllegalArgumentException.class, service::prepareMembers);
        verifyNoInteractions(repository);
    }

    @Test
    void returnsAnEmptyListForAnEmptySource() {
        when(client.fetchAllMembers()).thenReturn(List.of());

        assertTrue(service.prepareMembers().isEmpty());
        verify(client, never()).fetchDetailMember(any());
        verifyNoInteractions(repository);
    }

    private MemberSummaryDto summary(String id) {
        MemberSummaryDto summary = new MemberSummaryDto();
        summary.setBioguideId(id);
        summary.setName("Fictional Member " + id);
        summary.setPartyName("Test Party");
        summary.setState("Virginia");
        TermDto term = new TermDto();
        term.setChamber("Senate");
        term.setStartYear(2025);
        TermListDto terms = new TermListDto();
        terms.setItem(List.of(term));
        summary.setTerms(terms);
        return summary;
    }

    private MemberDetailResponseDto detail(String id) {
        MemberDetailDto detail = new MemberDetailDto();
        detail.setBioguideId(id);
        detail.setOfficialWebsiteUrl("https://example.com/" + id);
        detail.setCurrentMember(true);
        detail.setUpdateDate("2026-11-05T08:36:10Z");
        MemberDetailResponseDto response = new MemberDetailResponseDto();
        response.setMember(detail);
        return response;
    }
}
