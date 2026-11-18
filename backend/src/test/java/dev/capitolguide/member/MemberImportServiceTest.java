package dev.capitolguide.member;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "CONGRESS_API_KEY=unused-test-key")
@Transactional
class MemberImportServiceTest {
    @Autowired
    private MemberImportService memberImportService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberTermRepository memberTermRepository;

    @Autowired
    private MemberPartyHistoryRepository memberPartyHistoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void repeatedRefreshReplacesHistoriesAndLeavesOtherMembersUntouched() {
        Member original = fictionalMember("DEV_REFRESH_TARGET");
        Member other = fictionalMember("DEV_REFRESH_OTHER");
        memberImportService.saveMembers(withHistory(original, 2021, "Fictional Old Party"));
        memberImportService.saveMembers(withHistory(other, 2023, "Fictional Other Party"));
        entityManager.flush();
        entityManager.clear();

        for (int attempt = 0; attempt < 2; attempt++) {
            Member refreshed = fictionalMember(original.getBioguideId());
            memberImportService.saveMembers(withHistory(refreshed, 2025, "Fictional Updated Party"));
            entityManager.flush();
            entityManager.clear();

            assertHistory(original.getBioguideId(), 2025, "Fictional Updated Party");
            assertHistory(other.getBioguideId(), 2023, "Fictional Other Party");
        }
    }

    @Test
    void emptyReplacementRemovesOldHistories() {
        Member member = fictionalMember("DEV_REFRESH_EMPTY");
        memberImportService.saveMembers(withHistory(member, 2021, "Fictional Old Party"));
        entityManager.flush();
        entityManager.clear();

        memberImportService.saveMembers(new PreparedMembers(
                List.of(fictionalMember(member.getBioguideId())), List.of(), List.of()));
        entityManager.flush();
        entityManager.clear();

        assertTrue(memberRepository.existsById(member.getBioguideId()));
        assertTrue(memberTermRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(
                member.getBioguideId()).isEmpty());
        assertTrue(memberPartyHistoryRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(
                member.getBioguideId()).isEmpty());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void failedRefreshRestoresDeletedHistoriesAndOriginalMember() {
        String id = "DEV_REFRESH_ROLLBACK";
        Member original = fictionalMember(id);
        memberImportService.saveMembers(withHistory(original, 2021, "Fictional Old Party"));

        try {
            Member changed = fictionalMember(id);
            changed.setName("Fictional Change That Must Roll Back");
            PreparedMembers replacement = withHistory(changed, 2025, "Fictional New Party");
            MemberPartyHistory invalidParty = new MemberPartyHistory();

            assertThrows(DataIntegrityViolationException.class,
                    () -> memberImportService.saveMembers(new PreparedMembers(
                            replacement.members(), replacement.terms(), List.of(invalidParty))));

            assertEquals(original.getName(), memberRepository.findById(id).orElseThrow().getName());
            assertHistory(id, 2021, "Fictional Old Party");
        } finally {
            memberTermRepository.deleteAll(
                    memberTermRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id));
            memberPartyHistoryRepository.deleteAll(
                    memberPartyHistoryRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id));
            memberRepository.deleteAllById(List.of(id));
        }
    }

    private PreparedMembers withHistory(Member member, int startYear, String partyName) {
        MemberTerm term = new MemberTerm();
        term.setMember(member);
        term.setStartYear(startYear);
        MemberPartyHistory party = new MemberPartyHistory();
        party.setMember(member);
        party.setPartyName(partyName);
        return new PreparedMembers(List.of(member), List.of(term), List.of(party));
    }

    private void assertHistory(String id, int startYear, String partyName) {
        List<MemberTerm> terms = memberTermRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id);
        List<MemberPartyHistory> parties = memberPartyHistoryRepository
                .findByMember_BioguideIdOrderByStartYearAscIdAsc(id);
        assertEquals(1, terms.size());
        assertEquals(startYear, terms.getFirst().getStartYear());
        assertEquals(1, parties.size());
        assertEquals(partyName, parties.getFirst().getPartyName());
    }

    @Test
    void savesHistoriesForANewAndAnExistingMember() {
        Member existing = fictionalMember("DEV_HISTORY_EXISTING");
        memberRepository.saveAndFlush(existing);
        entityManager.clear();

        Member refreshed = fictionalMember(existing.getBioguideId());
        Member added = fictionalMember("DEV_HISTORY_NEW");
        List<Member> members = List.of(refreshed, added);
        List<MemberTerm> terms = members.stream().map(member -> {
            MemberTerm term = new MemberTerm();
            term.setMember(member);
            term.setChamber("Senate");
            term.setStartYear(2025);
            return term;
        }).toList();
        List<MemberPartyHistory> parties = members.stream().map(member -> {
            MemberPartyHistory party = new MemberPartyHistory();
            party.setMember(member);
            party.setPartyName("Fictional Test Party");
            return party;
        }).toList();

        memberImportService.saveMembers(new PreparedMembers(members, terms, parties));
        entityManager.flush();
        entityManager.clear();

        for (Member member : members) {
            String id = member.getBioguideId();
            assertTrue(memberRepository.existsById(id));
            List<MemberTerm> savedTerms = memberTermRepository
                    .findByMember_BioguideIdOrderByStartYearAscIdAsc(id);
            List<MemberPartyHistory> savedParties = memberPartyHistoryRepository
                    .findByMember_BioguideIdOrderByStartYearAscIdAsc(id);
            assertEquals(1, savedTerms.size());
            assertEquals("Senate", savedTerms.getFirst().getChamber());
            assertEquals(2025, savedTerms.getFirst().getStartYear());
            assertEquals(1, savedParties.size());
            assertEquals("Fictional Test Party", savedParties.getFirst().getPartyName());
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollsBackMembersAndTermsWhenPartyHistoryFails() {
        String id = "DEV_HISTORY_ROLLBACK";
        Member member = fictionalMember(id);
        MemberTerm term = new MemberTerm();
        term.setMember(member);
        MemberPartyHistory invalidParty = new MemberPartyHistory();
        // A history row must reference a member; this intentionally omits that reference.

        try {
            PreparedMembers prepared = new PreparedMembers(
                    List.of(member), List.of(term), List.of(invalidParty));
            assertThrows(DataIntegrityViolationException.class,
                    () -> memberImportService.saveMembers(prepared));

            assertFalse(memberRepository.existsById(id));
            assertTrue(memberTermRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id).isEmpty());
            assertTrue(memberPartyHistoryRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id).isEmpty());
        } finally {
            // Also clean up if a regression incorrectly commits an earlier save.
            memberTermRepository.deleteAll(
                    memberTermRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id));
            memberPartyHistoryRepository.deleteAll(
                    memberPartyHistoryRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(id));
            memberRepository.deleteAllById(List.of(id));
        }
    }

    @Test
    void savesPreparedMembersWithDetails() {
        Member first = fictionalMember("DEV_DETAIL_SAVE001");
        Member second = fictionalMember("DEV_DETAIL_SAVE002");
        second.setOfficialWebsiteUrl(null);
        second.setCurrentMember(null);
        second.setSourceUpdateDate(null);

        memberImportService.saveMembers(new PreparedMembers(List.of(first, second), List.of(), List.of()));
        entityManager.flush();
        entityManager.clear();

        Member result = memberRepository.findById(first.getBioguideId()).orElseThrow();
        assertEquals(first.getName(), result.getName());
        assertEquals(first.getChamber(), result.getChamber());
        assertEquals(first.getOfficialWebsiteUrl(), result.getOfficialWebsiteUrl());
        assertEquals(Boolean.TRUE, result.getCurrentMember());
        assertEquals(first.getSourceUpdateDate(), result.getSourceUpdateDate());

        Member optional = memberRepository.findById(second.getBioguideId()).orElseThrow();
        assertNull(optional.getOfficialWebsiteUrl());
        assertNull(optional.getCurrentMember());
        assertNull(optional.getSourceUpdateDate());
    }

    @Test
    void updatesAnExistingMemberUsingItsStableId() {
        Member original = fictionalMember("DEV_DETAIL_UPDATE001");
        memberRepository.saveAndFlush(original);
        entityManager.clear();

        Member refreshed = fictionalMember(original.getBioguideId());
        refreshed.setName("Fictional Updated Member");
        refreshed.setOfficialWebsiteUrl("https://example.com/updated");
        refreshed.setCurrentMember(false);
        refreshed.setSourceUpdateDate(Instant.parse("2026-11-06T12:00:00Z"));
        memberImportService.saveMembers(new PreparedMembers(List.of(refreshed), List.of(), List.of()));
        entityManager.flush();
        entityManager.clear();

        Member result = memberRepository.findById(original.getBioguideId()).orElseThrow();
        assertEquals(refreshed.getName(), result.getName());
        assertEquals(refreshed.getOfficialWebsiteUrl(), result.getOfficialWebsiteUrl());
        assertEquals(Boolean.FALSE, result.getCurrentMember());
        assertEquals(refreshed.getSourceUpdateDate(), result.getSourceUpdateDate());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollsBackDatabaseFailureAndPreservesPreviouslySavedMember() {
        String existingId = "DEV_DETAIL_ROLLBACK001";
        String invalidId = "DEV_DETAIL_ROLLBACK002";
        Member original = fictionalMember(existingId);
        memberRepository.saveAndFlush(original);

        try {
            Member changed = fictionalMember(existingId);
            changed.setName("Fictional Change That Must Roll Back");
            changed.setOfficialWebsiteUrl("https://example.com/must-not-persist");
            Member invalid = fictionalMember(invalidId);
            invalid.setName(null); // The database's NOT NULL constraint must reject this record.

            assertThrows(DataIntegrityViolationException.class,
                    () -> memberImportService.saveMembers(
                            new PreparedMembers(List.of(changed, invalid), List.of(), List.of())));

            Member result = memberRepository.findById(existingId).orElseThrow();
            assertEquals(original.getName(), result.getName());
            assertEquals(original.getOfficialWebsiteUrl(), result.getOfficialWebsiteUrl());
            assertFalse(memberRepository.existsById(invalidId));
        } finally {
            // This test has no outer test transaction, so remove its committed seed record.
            memberRepository.deleteAllById(List.of(existingId, invalidId));
        }
    }

    private Member fictionalMember(String id) {
        Member member = new Member();
        member.setBioguideId(id);
        member.setName("Fictional Development Member " + id);
        member.setPartyName("Test Party");
        member.setState("Virginia");
        member.setChamber("Senate");
        member.setChamberStartYear(2025);
        member.setOfficialWebsiteUrl("https://example.com/" + id);
        member.setCurrentMember(true);
        member.setSourceUpdateDate(Instant.parse("2026-11-05T08:36:10Z"));
        return member;
    }
}
