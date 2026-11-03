package dev.capitolguide.member;

import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.springframework.stereotype.Service;

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
}