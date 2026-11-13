package dev.capitolguide.member;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "CONGRESS_API_KEY=unused-test-key")
@Transactional
class MemberHistoryRepositoryTest {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private MemberTermRepository terms;
    @Autowired
    private MemberPartyHistoryRepository parties;

    @Test
    void filtersBothHistoriesByMemberAndOrdersByYearThenId() {
        Member selected = member("DEV_HISTORY001");
        Member other = member("DEV_HISTORY002");
        MemberTerm newerTerm = term(selected, 2025);
        MemberTerm olderTerm = term(selected, 2023);
        MemberTerm sameYearTerm = term(selected, 2023);
        term(other, 2020);
        MemberPartyHistory newerParty = party(selected, 2025);
        MemberPartyHistory olderParty = party(selected, 2023);
        MemberPartyHistory sameYearParty = party(selected, 2023);
        party(other, 2020);
        entityManager.flush();
        entityManager.clear();

        List<MemberTerm> savedTerms = terms.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_HISTORY001");
        List<MemberPartyHistory> savedParties =
                parties.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_HISTORY001");

        assertEquals(List.of(olderTerm.getId(), sameYearTerm.getId(), newerTerm.getId()),
                savedTerms.stream().map(MemberTerm::getId).toList());
        assertEquals(List.of(olderParty.getId(), sameYearParty.getId(), newerParty.getId()),
                savedParties.stream().map(MemberPartyHistory::getId).toList());
    }

    @Test
    void returnsEmptyListsWhenNoHistoriesMatch() {
        Member other = member("DEV_HISTORY_OTHER");
        term(other, 2025);
        party(other, 2025);
        entityManager.flush();
        entityManager.clear();

        assertTrue(terms.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_HISTORY_MISSING").isEmpty());
        assertTrue(parties.findByMember_BioguideIdOrderByStartYearAscIdAsc("DEV_HISTORY_MISSING").isEmpty());
    }

    private Member member(String id) {
        Member member = new Member();
        member.setBioguideId(id);
        member.setName("Fictional History Member " + id);
        member.setPartyName("Test Party");
        member.setState("Virginia");
        member.setChamber("Senate");
        entityManager.persist(member);
        return member;
    }

    private MemberTerm term(Member member, int year) {
        MemberTerm term = new MemberTerm();
        term.setMember(member);
        term.setStartYear(year);
        entityManager.persist(term);
        return term;
    }

    private MemberPartyHistory party(Member member, int year) {
        MemberPartyHistory history = new MemberPartyHistory();
        history.setMember(member);
        history.setStartYear(year);
        entityManager.persist(history);
        return history;
    }
}
