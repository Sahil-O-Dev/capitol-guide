package dev.capitolguide.member;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
}