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
    private EntityManager entityManager;

    @Test
    void savesPreparedMembersWithDetails() {
        Member first = fictionalMember("DEV_DETAIL_SAVE001");
        Member second = fictionalMember("DEV_DETAIL_SAVE002");
        second.setOfficialWebsiteUrl(null);
        second.setCurrentMember(null);
        second.setSourceUpdateDate(null);

        List<Member> saved = memberImportService.saveMembers(List.of(first, second));
        assertEquals(2, saved.size());
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
        memberImportService.saveMembers(List.of(refreshed));
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
                    () -> memberImportService.saveMembers(List.of(changed, invalid)));

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
