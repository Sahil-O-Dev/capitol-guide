package dev.capitolguide.member;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "CONGRESS_API_KEY=unused-test-key")
@Transactional
class MemberPartyHistoryTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    void savesMultiplePartyEntriesForOneMember() {
        Member member = new Member();
        member.setBioguideId("DEV_PARTY001");
        member.setName("Fictional Party History Member");
        member.setPartyName("Test Party B");
        member.setState("Virginia");
        member.setChamber("Senate");
        entityManager.persist(member);

        MemberPartyHistory previous = partyEntry(member, "TA", "Test Party A", 2020);
        previous.setEndYear(2024);
        MemberPartyHistory current = partyEntry(member, "TB", "Test Party B", 2024);
        entityManager.persist(previous);
        entityManager.persist(current);
        entityManager.flush();
        Long previousId = previous.getId();
        Long currentId = current.getId();
        entityManager.clear();

        assertNotNull(previousId);
        assertNotNull(currentId);
        assertNotEquals(previousId, currentId);
        MemberPartyHistory savedPrevious = entityManager.find(MemberPartyHistory.class, previousId);
        MemberPartyHistory savedCurrent = entityManager.find(MemberPartyHistory.class, currentId);
        assertEquals("DEV_PARTY001", savedPrevious.getMember().getBioguideId());
        assertEquals("TA", savedPrevious.getPartyAbbreviation());
        assertEquals("Test Party A", savedPrevious.getPartyName());
        assertEquals(2020, savedPrevious.getStartYear());
        assertEquals(2024, savedPrevious.getEndYear());
        assertEquals("DEV_PARTY001", savedCurrent.getMember().getBioguideId());
        assertEquals("TB", savedCurrent.getPartyAbbreviation());
        assertEquals("Test Party B", savedCurrent.getPartyName());
        assertEquals(2024, savedCurrent.getStartYear());
        assertNull(savedCurrent.getEndYear());
    }

    @Test
    void rejectsPartyHistoryForAMissingMember() {
        Member missing = entityManager.getReference(Member.class, "DEV_PARTY_MISSING");
        MemberPartyHistory history = partyEntry(missing, "T", "Test Party", 2025);

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(history);
            entityManager.flush();
        });
    }

    private MemberPartyHistory partyEntry(Member member, String abbreviation, String name, int startYear) {
        MemberPartyHistory history = new MemberPartyHistory();
        history.setMember(member);
        history.setPartyAbbreviation(abbreviation);
        history.setPartyName(name);
        history.setStartYear(startYear);
        return history;
    }
}
