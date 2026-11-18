package dev.capitolguide.member;

import dev.capitolguide.congress.CongressApiClient;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailResponseDto;
import dev.capitolguide.congress.dto.MemberDetailTermDto;
import dev.capitolguide.congress.dto.PartyHistoryDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;
import dev.capitolguide.congress.dto.TermListDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberPreparationTest {
    private final CongressApiClient client = mock(CongressApiClient.class);
    private final MemberRepository repository = mock(MemberRepository.class);
    private final MemberTermRepository termRepository = mock(MemberTermRepository.class);
    private final MemberPartyHistoryRepository partyRepository = mock(MemberPartyHistoryRepository.class);
    private final MemberImportService service = new MemberImportService(
            client, new MemberMapper(), repository, termRepository, partyRepository);

    @Test
    void combinesEachSummaryWithItsOwnDetailsWithoutSaving() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
        when(client.fetchDetailMember("DEV002")).thenReturn(detail("DEV002"));

        PreparedMembers prepared = service.prepareMembers();
        List<Member> members = prepared.members();

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
        assertTrue(prepared.terms().isEmpty());
        assertTrue(prepared.partyHistory().isEmpty());
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void collectsAllHistoriesLinkedToTheirOwnPreparedMember() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        for (String id : List.of("DEV001", "DEV002")) {
            MemberDetailResponseDto response = detail(id);
            MemberDetailTermDto earlier = new MemberDetailTermDto();
            earlier.setStartYear(2021);
            MemberDetailTermDto later = new MemberDetailTermDto();
            later.setStartYear(2025);
            PartyHistoryDto party = new PartyHistoryDto();
            party.setPartyName("Fictional Party " + id);
            response.getMember().setTerms(List.of(earlier, later));
            response.getMember().setPartyHistory(List.of(party));
            when(client.fetchDetailMember(id)).thenReturn(response);
        }

        PreparedMembers prepared = service.prepareMembers();

        assertEquals(2, prepared.members().size());
        assertEquals(4, prepared.terms().size());
        assertEquals(2, prepared.partyHistory().size());
        for (int i = 0; i < 2; i++) {
            Member member = prepared.members().get(i);
            assertSame(member, prepared.terms().get(i * 2).getMember());
            assertSame(member, prepared.terms().get(i * 2 + 1).getMember());
            assertEquals(2021, prepared.terms().get(i * 2).getStartYear());
            assertEquals(2025, prepared.terms().get(i * 2 + 1).getStartYear());
            assertSame(member, prepared.partyHistory().get(i).getMember());
            assertEquals("Fictional Party " + member.getBioguideId(),
                    prepared.partyHistory().get(i).getPartyName());
        }
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void rejectsMissingSummaryIdBeforeRequestingDetails() {
        for (String id : new String[] {null, "", "   "}) {
            when(client.fetchAllMembers()).thenReturn(List.of(summary(id)));
            assertThrows(IllegalStateException.class, service::prepareMembers);
        }

        verify(client, never()).fetchDetailMember(any());
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void rejectsMissingOrMismatchedDetailId() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001")));
        for (String id : new String[] {null, "", "DEV002"}) {
            when(client.fetchDetailMember("DEV001")).thenReturn(detail(id));

            IllegalStateException error = assertThrows(IllegalStateException.class, service::prepareMembers);

            assertEquals("Congress.gov returned mismatched member details for ID DEV001", error.getMessage());
        }
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void stopsWhenALaterDetailRequestFailsWithoutSavingEarlierMembers() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
        IllegalStateException failure = new IllegalStateException("Fictional API failure");
        when(client.fetchDetailMember("DEV002")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, service::prepareMembers));
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void stopsWhenMappingFailsWithoutSavingEarlierMembers() {
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
        when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
        MemberDetailResponseDto malformed = detail("DEV002");
        malformed.getMember().setUpdateDate("not-a-date");
        when(client.fetchDetailMember("DEV002")).thenReturn(malformed);

        assertThrows(IllegalArgumentException.class, service::prepareMembers);
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void returnsEmptyListsForAnEmptySource() {
        when(client.fetchAllMembers()).thenReturn(List.of());

        PreparedMembers prepared = service.prepareMembers();
        assertTrue(prepared.members().isEmpty());
        assertTrue(prepared.terms().isEmpty());
        assertTrue(prepared.partyHistory().isEmpty());
        verify(client, never()).fetchDetailMember(any());
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void rejectsMissingRequiredTextBeforeRequestingDetails() {
        List<Consumer<MemberSummaryDto>> fields = List.of(
                member -> member.setName(null), member -> member.setName("  "),
                member -> member.setPartyName(null), member -> member.setPartyName(""),
                member -> member.setState(null), member -> member.setState("  "));
        for (Consumer<MemberSummaryDto> removeField : fields) {
            MemberSummaryDto member = summary("DEV001");
            removeField.accept(member);
            when(client.fetchAllMembers()).thenReturn(List.of(member));

            IllegalStateException error = assertThrows(IllegalStateException.class, service::prepareMembers);
            assertTrue(error.getMessage().contains("DEV001"));
        }
        verify(client, never()).fetchDetailMember(any());
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void rejectsMissingServiceTermsOrUnusableChamber() {
        List<Consumer<MemberSummaryDto>> changes = List.of(
                member -> member.setTerms(null),
                member -> member.getTerms().setItem(null),
                member -> member.getTerms().setItem(List.of()),
                member -> member.getTerms().setItem(Arrays.asList((TermDto) null)),
                member -> member.getTerms().getItem().getLast().setChamber(null),
                member -> member.getTerms().getItem().getLast().setChamber("  "),
                member -> member.getTerms().getItem().getLast().setChamber("Unknown"));
        for (Consumer<MemberSummaryDto> change : changes) {
            MemberSummaryDto member = summary("DEV001");
            change.accept(member);
            when(client.fetchAllMembers()).thenReturn(List.of(member));

            assertThrows(IllegalStateException.class, service::prepareMembers);
        }
        verify(client, never()).fetchDetailMember(any());
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void rejectsMissingHistoriesOrNullEntriesWithoutSaving() {
        List<Consumer<MemberDetailDto>> changes = List.of(
                member -> member.setTerms(null),
                member -> member.setPartyHistory(null),
                member -> member.setTerms(Arrays.asList((MemberDetailTermDto) null)),
                member -> member.setPartyHistory(Arrays.asList((PartyHistoryDto) null)));
        for (Consumer<MemberDetailDto> change : changes) {
            when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001"), summary("DEV002")));
            when(client.fetchDetailMember("DEV001")).thenReturn(detail("DEV001"));
            MemberDetailResponseDto malformed = detail("DEV002");
            change.accept(malformed.getMember());
            when(client.fetchDetailMember("DEV002")).thenReturn(malformed);

            IllegalStateException error = assertThrows(IllegalStateException.class, service::prepareMembers);
            assertTrue(error.getMessage().contains("DEV002"));
        }
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void acceptsMissingOptionalValuesAndExplicitlyEmptyHistories() {
        MemberSummaryDto member = summary("DEV001");
        member.getTerms().getItem().getLast().setStartYear(null);
        when(client.fetchAllMembers()).thenReturn(List.of(member));
        MemberDetailResponseDto response = detail("DEV001");
        response.getMember().setOfficialWebsiteUrl(null);
        response.getMember().setCurrentMember(null);
        response.getMember().setUpdateDate(null);
        when(client.fetchDetailMember("DEV001")).thenReturn(response);

        PreparedMembers prepared = service.prepareMembers();

        Member saved = prepared.members().getFirst();
        assertNull(saved.getImageUrl());
        assertNull(saved.getDistrict());
        assertNull(saved.getChamberStartYear());
        assertNull(saved.getOfficialWebsiteUrl());
        assertNull(saved.getCurrentMember());
        assertNull(saved.getSourceUpdateDate());
        assertTrue(prepared.terms().isEmpty());
        assertTrue(prepared.partyHistory().isEmpty());
        verifyNoInteractions(repository, termRepository, partyRepository);
    }

    @Test
    void rejectsMissingResponseContainers() {
        when(client.fetchAllMembers()).thenReturn(null);
        assertThrows(IllegalStateException.class, service::prepareMembers);
        when(client.fetchAllMembers()).thenReturn(Arrays.asList((MemberSummaryDto) null));
        assertThrows(IllegalStateException.class, service::prepareMembers);
        when(client.fetchAllMembers()).thenReturn(List.of(summary("DEV001")));
        when(client.fetchDetailMember("DEV001")).thenReturn(null);
        assertThrows(IllegalStateException.class, service::prepareMembers);
        when(client.fetchDetailMember("DEV001")).thenReturn(new MemberDetailResponseDto());
        assertThrows(IllegalStateException.class, service::prepareMembers);
        verifyNoInteractions(repository, termRepository, partyRepository);
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
        detail.setTerms(List.of());
        detail.setPartyHistory(List.of());
        MemberDetailResponseDto response = new MemberDetailResponseDto();
        response.setMember(detail);
        return response;
    }
}
