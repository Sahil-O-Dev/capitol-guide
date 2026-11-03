package dev.capitolguide.member;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;
import dev.capitolguide.congress.dto.TermListDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MemberMapperTest
{
    @Test
    void mapsLatestChamber()
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

        MemberMapper mapper = new MemberMapper();
        Member result = mapper.toMember(source);

        assertEquals("Development Member", result.getName());
        assertEquals("Senate", result.getChamber());
        assertEquals(2013, result.getChamberStartYear());


    }
}