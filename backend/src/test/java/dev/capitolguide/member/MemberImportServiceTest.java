package dev.capitolguide.member;


import dev.capitolguide.congress.dto.DepictionDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;
import dev.capitolguide.congress.dto.TermListDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @Test
    void savesMultipleMembers()
    {
        MemberSummaryDto fictionalMember1 = new MemberSummaryDto();
        MemberSummaryDto fictionalMember2 = new MemberSummaryDto();

        fictionalMember1.setBioguideId("DEV_BATCH001");
        fictionalMember1.setName("Fictional House Member");
        fictionalMember1.setPartyName("Test Party");
        fictionalMember1.setState("Virginia");
        fictionalMember1.setDistrict(1);

        DepictionDto photo1 = new DepictionDto();
        photo1.setImageUrl("https://example.com/fictional-house.jpg");
        photo1.setAttribution("Fictional test image");
        fictionalMember1.setDepiction(photo1);

        TermDto term1 = new TermDto();
        term1.setChamber("House of Representatives");
        term1.setStartYear(2025);

        TermListDto terms1 = new TermListDto();
        terms1.setItem(List.of(term1));
        fictionalMember1.setTerms(terms1);


        fictionalMember2.setBioguideId("DEV_BATCH002");
        fictionalMember2.setName("Fictional Senate Member");
        fictionalMember2.setPartyName("Test Party");
        fictionalMember2.setState("Virginia");
        fictionalMember2.setDistrict(null);

        DepictionDto photo2 = new DepictionDto();
        photo2.setImageUrl("https://example.com/fictional-senate.jpg");
        photo2.setAttribution("Fictional test image");
        fictionalMember2.setDepiction(photo2);

        TermDto term2 = new TermDto();
        term2.setChamber("Senate");
        term2.setStartYear(2023);

        TermListDto terms2 = new TermListDto();
        terms2.setItem(List.of(term2));
        fictionalMember2.setTerms(terms2);

        List<MemberSummaryDto> source = List.of(fictionalMember1, fictionalMember2);

        List<Member> members = memberImportService.saveMembers(source);

        assertEquals(2, members.size());

        entityManager.flush();
        entityManager.clear();

        Member savedMember1 = memberRepository.findById(fictionalMember1.getBioguideId()).orElseThrow();
        Member savedMember2 = memberRepository.findById(fictionalMember2.getBioguideId()).orElseThrow();

        assertEquals(fictionalMember1.getName(), savedMember1.getName());
        assertEquals(fictionalMember2.getName(), savedMember2.getName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollsBackBatchWhenOneMemberFails()
    {
        MemberSummaryDto fictionalMember1 = new MemberSummaryDto();
        MemberSummaryDto fictionalMember2 = new MemberSummaryDto();

        fictionalMember1.setBioguideId("DEV_BATCH001");
        fictionalMember1.setName("Fictional House Member");
        fictionalMember1.setPartyName("Test Party");
        fictionalMember1.setState("Virginia");
        fictionalMember1.setDistrict(1);

        DepictionDto photo1 = new DepictionDto();
        photo1.setImageUrl("https://example.com/fictional-house.jpg");
        photo1.setAttribution("Fictional test image");
        fictionalMember1.setDepiction(photo1);

        TermDto term1 = new TermDto();
        term1.setChamber("House of Representatives");
        term1.setStartYear(2025);

        TermListDto terms1 = new TermListDto();
        terms1.setItem(List.of(term1));
        fictionalMember1.setTerms(terms1);


        fictionalMember2.setBioguideId("DEV_BATCH002");
        fictionalMember2.setName("Fictional Senate Member");
        fictionalMember2.setPartyName("Test Party");
        fictionalMember2.setState("Virginia");
        fictionalMember2.setDistrict(null);

        DepictionDto photo2 = new DepictionDto();
        photo2.setImageUrl("https://example.com/fictional-senate.jpg");
        photo2.setAttribution("Fictional test image");
        fictionalMember2.setDepiction(photo2);

        TermDto term2 = new TermDto();
        term2.setChamber("Senate");
        term2.setStartYear(2023);

        TermListDto terms2 = new TermListDto();
        terms2.setItem(List.of());
        fictionalMember2.setTerms(terms2);

        List<MemberSummaryDto> source = List.of(fictionalMember1, fictionalMember2);




        assertThrows(NoSuchElementException.class, ()-> memberImportService.saveMembers((source)));

        assertFalse(memberRepository.existsById(fictionalMember1.getBioguideId()));
        assertFalse(memberRepository.existsById(fictionalMember2.getBioguideId()));
    }
}
