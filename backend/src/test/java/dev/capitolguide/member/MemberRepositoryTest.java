package dev.capitolguide.member;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReadsMember() {
        Member memberOne = new Member();
        memberOne.setBioguideId("DEV001");
        memberOne.setName("Development Member");
        memberOne.setPartyName("Test Party");
        memberOne.setState("Virginia");
        memberOne.setChamber("House of Representatives");

        memberRepository.saveAndFlush(memberOne);

        entityManager.clear();

        Member storedMember = memberRepository.findById("DEV001").orElseThrow();

        assertEquals("Development Member", storedMember.getName());
    }

    @Test
    void findsMembersMatchingBothStateAndChamber() {
        memberRepository.saveAll(List.of(
                developmentMember("DEV_FILTER001", "Development State A", "Senate"),
                developmentMember("DEV_FILTER002", "Development State A", "Senate"),
                developmentMember("DEV_FILTER003", "Development State A", "House of Representatives"),
                developmentMember("DEV_FILTER004", "Development State B", "Senate")
        ));
        memberRepository.flush();
        entityManager.clear();

        List<Member> members = memberRepository.findByStateAndChamber("Development State A", "Senate");

        List<String> memberIds = new ArrayList<>();
        for (Member member : members) {
            memberIds.add(member.getBioguideId());
        }

        assertEquals(2, members.size());
        assertTrue(memberIds.contains("DEV_FILTER001"));
        assertTrue(memberIds.contains("DEV_FILTER002"));
    }

    @Test
    void returnsEmptyListWhenNoMembersMatch() {
        memberRepository.saveAll(List.of(
                developmentMember("DEV_FILTER005", "Development State C", "House of Representatives"),
                developmentMember("DEV_FILTER006", "Development State D", "Senate")
        ));
        memberRepository.flush();
        entityManager.clear();

        List<Member> members = memberRepository.findByStateAndChamber("Development State C", "Senate");

        assertTrue(members.isEmpty());
    }

    private Member developmentMember(String id, String state, String chamber) {
        Member member = new Member();
        member.setBioguideId(id);
        member.setName("Fictional Development Member " + id);
        member.setPartyName("Test Party");
        member.setState(state);
        member.setChamber(chamber);
        return member;
    }
}
