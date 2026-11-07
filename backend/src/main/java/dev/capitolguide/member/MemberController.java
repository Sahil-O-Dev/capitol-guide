package dev.capitolguide.member;

import dev.capitolguide.member.dto.MemberResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MemberController
{
    private final MemberResponseMapper memberResponseMapper;
    private final MemberRepository memberRepository;

    public MemberController(MemberResponseMapper memberResponseMapper, MemberRepository memberRepository)
    {
        this.memberResponseMapper = memberResponseMapper;
        this.memberRepository = memberRepository;
    }


    @GetMapping("/api/members")
    public List<MemberResponseDto> getMembers()
    {
        return memberResponseMapper.toDtoList(memberRepository.findAll());
    }
}
