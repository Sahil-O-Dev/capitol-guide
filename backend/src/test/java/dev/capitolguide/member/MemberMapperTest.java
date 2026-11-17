package dev.capitolguide.member;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailTermDto;
import dev.capitolguide.congress.dto.TermDto;
import dev.capitolguide.congress.dto.TermListDto;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MemberMapperTest
{
    @Test
    void mapsTermFieldsAndLinksTheSameMember() {
        Member member = new Member();
        member.setBioguideId("DEV_TERM_MAP001");
        MemberDetailTermDto source = new MemberDetailTermDto();
        source.setChamber("House of Representatives");
        source.setCongress(118);
        source.setDistrict(1);
        source.setMemberType("Representative");
        source.setStartYear(2023);
        source.setEndYear(2025);
        source.setStateCode("VA");
        source.setStateName("Virginia");

        MemberTerm result = new MemberMapper().toMemberTerm(source, member);

        assertSame(member, result.getMember());
        assertNull(result.getId());
        assertEquals("House of Representatives", result.getChamber());
        assertEquals(118, result.getCongress());
        assertEquals(1, result.getDistrict());
        assertEquals("Representative", result.getMemberType());
        assertEquals(2023, result.getStartYear());
        assertEquals(2025, result.getEndYear());
        assertEquals("VA", result.getStateCode());
        assertEquals("Virginia", result.getStateName());
    }

    @Test
    void keepsMissingTermFieldsNull() {
        Member member = new Member();
        MemberDetailTermDto source = new MemberDetailTermDto();
        source.setChamber("Senate");
        source.setStartYear(2025);

        MemberTerm result = new MemberMapper().toMemberTerm(source, member);

        assertSame(member, result.getMember());
        assertEquals("Senate", result.getChamber());
        assertEquals(2025, result.getStartYear());
        assertNull(result.getEndYear());
        assertNull(result.getDistrict());
        assertNull(result.getCongress());
        assertNull(result.getMemberType());
        assertNull(result.getStateCode());
        assertNull(result.getStateName());
    }

    @Test
    void rejectsTermMappingWithoutAMember() {
        assertThrows(NullPointerException.class,
                () -> new MemberMapper().toMemberTerm(new MemberDetailTermDto(), null));
    }

    @Test
    void rejectsTermMappingWithoutASource() {
        assertThrows(NullPointerException.class,
                () -> new MemberMapper().toMemberTerm(null, new Member()));
    }

    @Test
    void addsDetailsToTheSameMember() {
        Member member = new Member();
        member.setBioguideId("DEV001");
        member.setName("Fictional Member");
        MemberDetailDto detail = new MemberDetailDto();
        detail.setOfficialWebsiteUrl("https://example.com/fictional-member");
        detail.setCurrentMember(true);
        detail.setUpdateDate("2026-11-05T08:36:10Z");

        Member result = new MemberMapper().updateDetails(member, detail);

        assertSame(member, result);
        assertEquals("Fictional Member", result.getName());
        assertEquals("https://example.com/fictional-member", result.getOfficialWebsiteUrl());
        assertEquals(Boolean.TRUE, result.getCurrentMember());
        assertEquals(Instant.parse("2026-11-05T08:36:10Z"), result.getSourceUpdateDate());
    }

    @Test
    void allowsMissingOptionalDetails() {
        for (String date : new String[] {null, "", "   "}) {
            Member member = new Member();
            member.setSourceUpdateDate(Instant.parse("2026-11-05T08:36:10Z"));
            MemberDetailDto detail = new MemberDetailDto();
            detail.setUpdateDate(date);

            new MemberMapper().updateDetails(member, detail);

            assertNull(member.getSourceUpdateDate());
            assertNull(member.getOfficialWebsiteUrl());
            assertNull(member.getCurrentMember());
        }
    }

    @Test
    void rejectsMalformedSourceDateWithMemberId() {
        Member member = new Member();
        member.setBioguideId("DEV001");
        MemberDetailDto detail = new MemberDetailDto();
        detail.setUpdateDate("not-a-date");

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> new MemberMapper().updateDetails(member, detail));

        assertEquals("Congress.gov returned an invalid update date for ID DEV001", error.getMessage());
    }

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
