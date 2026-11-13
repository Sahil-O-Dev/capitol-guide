package dev.capitolguide.member;

import dev.capitolguide.member.dto.MemberProfileResponseDto;
import dev.capitolguide.member.dto.MemberResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class MemberController
{
    private final MemberResponseMapper memberResponseMapper;
    private final MemberRepository memberRepository;
    private final MemberTermRepository memberTermRepository;
    private final MemberPartyHistoryRepository memberPartyHistoryRepository;

    public MemberController(MemberResponseMapper memberResponseMapper, MemberRepository memberRepository,
                            MemberTermRepository memberTermRepository,
                            MemberPartyHistoryRepository memberPartyHistoryRepository)
    {
        this.memberResponseMapper = memberResponseMapper;
        this.memberRepository = memberRepository;
        this.memberTermRepository = memberTermRepository;
        this.memberPartyHistoryRepository = memberPartyHistoryRepository;
    }


    @GetMapping("/api/members")
    public List<MemberResponseDto> getMembers(@RequestParam("state") String state, @RequestParam("chamber") String chamber)
    {
        List<Member> members = memberRepository.findByStateAndChamber(state, chamber);

        return memberResponseMapper.toDtoList(members);
    }

    @GetMapping("/api/members/{bioguideId}")
    public MemberProfileResponseDto getMemberProfile(@PathVariable("bioguideId") String bioguideId)
    {
        Member member = memberRepository.findById(bioguideId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
        List<MemberTerm> terms = memberTermRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(bioguideId);
        List<MemberPartyHistory> partyHistory =
                memberPartyHistoryRepository.findByMember_BioguideIdOrderByStartYearAscIdAsc(bioguideId);

        return memberResponseMapper.toProfileDto(member, terms, partyHistory);
    }
}
