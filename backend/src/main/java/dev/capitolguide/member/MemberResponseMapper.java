package dev.capitolguide.member;

import dev.capitolguide.member.dto.MemberResponseDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemberResponseMapper
{
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
