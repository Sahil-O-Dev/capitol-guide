package dev.capitolguide.member;

import dev.capitolguide.member.dto.MemberResponseDto;
import dev.capitolguide.member.dto.MemberProfileResponseDto;
import dev.capitolguide.member.dto.MemberTermResponseDto;
import dev.capitolguide.member.dto.PartyHistoryResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemberResponseMapper
{
    public MemberProfileResponseDto toProfileDto(Member member, List<MemberTerm> terms,
                                                 List<MemberPartyHistory> partyHistory) {
        MemberProfileResponseDto response = new MemberProfileResponseDto();
        response.setBioguideId(member.getBioguideId());
        response.setName(member.getName());
        response.setPartyName(member.getPartyName());
        response.setState(member.getState());
        response.setChamber(member.getChamber());
        response.setDistrict(member.getDistrict());
        response.setChamberStartYear(member.getChamberStartYear());
        response.setImageUrl(member.getImageUrl());
        response.setImageAttribution(member.getImageAttribution());
        response.setOfficialWebsiteUrl(member.getOfficialWebsiteUrl());
        response.setCurrentMember(member.getCurrentMember());
        response.setSourceUpdateDate(member.getSourceUpdateDate());

        List<MemberTermResponseDto> termResponses = new ArrayList<>();
        for (MemberTerm term : terms) {
            termResponses.add(toTermDto(term));
        }
        response.setTerms(termResponses);

        List<PartyHistoryResponseDto> partyResponses = new ArrayList<>();
        for (MemberPartyHistory entry : partyHistory) {
            partyResponses.add(toPartyHistoryDto(entry));
        }
        response.setPartyHistory(partyResponses);
        return response;
    }

    private MemberTermResponseDto toTermDto(MemberTerm term) {
        MemberTermResponseDto response = new MemberTermResponseDto();
        response.setChamber(term.getChamber());
        response.setCongress(term.getCongress());
        response.setDistrict(term.getDistrict());
        response.setMemberType(term.getMemberType());
        response.setStartYear(term.getStartYear());
        response.setEndYear(term.getEndYear());
        response.setStateCode(term.getStateCode());
        response.setStateName(term.getStateName());
        return response;
    }

    private PartyHistoryResponseDto toPartyHistoryDto(MemberPartyHistory entry) {
        PartyHistoryResponseDto response = new PartyHistoryResponseDto();
        response.setPartyAbbreviation(entry.getPartyAbbreviation());
        response.setPartyName(entry.getPartyName());
        response.setStartYear(entry.getStartYear());
        response.setEndYear(entry.getEndYear());
        return response;
    }

    public MemberResponseDto toDto(Member member) {
        MemberResponseDto responseMember = new MemberResponseDto();

        responseMember.setBioguideId(member.getBioguideId());
        responseMember.setName(member.getName());
        responseMember.setPartyName(member.getPartyName());
        responseMember.setState(member.getState());
        responseMember.setChamber(member.getChamber());
        responseMember.setDistrict(member.getDistrict());
        responseMember.setImageUrl(member.getImageUrl());
        responseMember.setImageAttribution(member.getImageAttribution());

        return responseMember;
    }


    public List<MemberResponseDto> toDtoList(List<Member> members)
    {
        List<MemberResponseDto> responseMemberList = new ArrayList<>();
        for (Member member : members) {
            responseMemberList.add(toDto(member));
        }
        return responseMemberList;
    }
}
