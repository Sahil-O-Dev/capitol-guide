package dev.capitolguide.member;


import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;
import dev.capitolguide.congress.dto.TermListDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class MemberImportServiceTest
{
    @Autowired
    private MemberImportService memberImportService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;


    @Test
    void savesMappedMember()
    {
        MemberSummaryDto source = new MemberSummaryDto();
        source.setBioguideId("DEV001");
        source.setName("Development Member");
        source.setPartyName("Test Party");
        source.setState("Virginia");


        TermDto house = new TermDto();
        house.setChamber("House of Representatives");
        house.setStartYear(2009);

        TermDto senate = new TermDto();
        senate.setChamber("Senate");
        senate.setStartYear(2013);

        TermListDto terms = new TermListDto();
        terms.setItem(List.of(house, senate));
        source.setTerms(terms);

        memberImportService.saveMember(source);

        entityManager.flush();
        entityManager.clear();

        Member result = memberRepository.findById("DEV001").orElseThrow();

        assertEquals("Development Member", result.getName());
        assertEquals("Senate", result.getChamber());
        assertEquals(2013, result.getChamberStartYear());

    }
}
