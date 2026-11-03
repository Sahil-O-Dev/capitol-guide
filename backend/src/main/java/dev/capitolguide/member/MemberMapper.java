package dev.capitolguide.member;

import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.TermDto;

import org.springframework.stereotype.Component;

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
}