package dev.capitolguide.member;

import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberImportService {

    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;

    public MemberImportService( MemberMapper memberMapper, MemberRepository memberRepository) {
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
    }

    public Member saveMember(MemberSummaryDto source) {
        Member member = memberMapper.toMember(source);
        return memberRepository.save(member);
    }

    @Transactional
    public List<Member> saveMembers(List<MemberSummaryDto> listMemberSummaryDtos)
    {
        List<Member> members = new ArrayList<>();
        for(int i = 0; i < listMemberSummaryDtos.size(); i++)
        {
            members.add(saveMember(listMemberSummaryDtos.get(i)));
        }
        return members;
    }
}