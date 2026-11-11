package dev.capitolguide.member;

import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@Component
public class MemberMapper {

    public Member toMember(MemberSummaryDto source) {
        Member member = new Member();
        TermDto latestTerm = source.getTerms().getItem().getLast();


        member.setBioguideId(source.getBioguideId());
        member.setName(source.getName());
        member.setPartyName(source.getPartyName());
        member.setState(source.getState());
        member.setDistrict(source.getDistrict());
        member.setChamber(latestTerm.getChamber());
        member.setChamberStartYear(latestTerm.getStartYear());

        if(source.getDepiction() != null)
        {
            member.setImageUrl(source.getDepiction().getImageUrl());
            member.setImageAttribution(source.getDepiction().getAttribution());
        }

        return member;
    }

    public Member updateDetails(Member member, MemberDetailDto memberDetailDto)
    {
        member.setOfficialWebsiteUrl(memberDetailDto.getOfficialWebsiteUrl());
        member.setCurrentMember(memberDetailDto.getCurrentMember());
        String updateDate = memberDetailDto.getUpdateDate();
        if (updateDate == null || updateDate.isBlank()) {
            member.setSourceUpdateDate(null);
        } else {
            try {
                member.setSourceUpdateDate(Instant.parse(updateDate));
            } catch (DateTimeParseException exception) {
                throw new IllegalArgumentException(
                        "Congress.gov returned an invalid update date for ID " + member.getBioguideId(), exception);
            }
        }

        return member;
    }
}
