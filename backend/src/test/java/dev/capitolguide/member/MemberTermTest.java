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
class MemberTermTest {
    @Autowired
    private EntityManager entityManager;

    @Test
    void savesMultipleTermsForOneMember() {
        Member member = new Member();
        member.setBioguideId("DEV_TERMS001");
        member.setName("Fictional Term Test Member");
        member.setPartyName("Test Party");
        member.setState("Virginia");
        member.setChamber("House of Representatives");
        entityManager.persist(member);

        MemberTerm previous = fictionalTerm(member, 118, 2023);
        previous.setEndYear(2025);
        MemberTerm current = fictionalTerm(member, 119, 2025);
        entityManager.persist(previous);
        entityManager.persist(current);
        entityManager.flush();
        Long previousId = previous.getId();
        Long currentId = current.getId();
        entityManager.clear();

        assertNotNull(previousId);
        assertNotNull(currentId);
        assertNotEquals(previousId, currentId);
        MemberTerm savedPrevious = entityManager.find(MemberTerm.class, previousId);
        MemberTerm savedCurrent = entityManager.find(MemberTerm.class, currentId);
        assertEquals("DEV_TERMS001", savedPrevious.getMember().getBioguideId());
        assertEquals("DEV_TERMS001", savedCurrent.getMember().getBioguideId());
        assertEquals(118, savedPrevious.getCongress());
        assertEquals(2023, savedPrevious.getStartYear());
        assertEquals(2025, savedPrevious.getEndYear());
        assertEquals(119, savedCurrent.getCongress());
        assertEquals(2025, savedCurrent.getStartYear());
        assertNull(savedCurrent.getEndYear());
        assertEquals("House of Representatives", savedCurrent.getChamber());
        assertEquals(1, savedCurrent.getDistrict());
        assertEquals("Representative", savedCurrent.getMemberType());
        assertEquals("VA", savedCurrent.getStateCode());
        assertEquals("Virginia", savedCurrent.getStateName());
    }

    @Test
    void rejectsATermWhoseMemberDoesNotExist() {
        Member missing = entityManager.getReference(Member.class, "DEV_TERMS_MISSING");
        MemberTerm term = fictionalTerm(missing, 119, 2025);

        assertThrows(PersistenceException.class, () -> {
            entityManager.persist(term);
            entityManager.flush();
        });
    }

    private MemberTerm fictionalTerm(Member member, int congress, int startYear) {
        MemberTerm term = new MemberTerm();
        term.setMember(member);
        term.setChamber("House of Representatives");
        term.setCongress(congress);
        term.setDistrict(1);
        term.setMemberType("Representative");
        term.setStartYear(startYear);
        term.setStateCode("VA");
        term.setStateName("Virginia");
        return term;
    }
}
