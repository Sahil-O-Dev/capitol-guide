package dev.capitolguide.member;

import dev.capitolguide.congress.CongressApiClient;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailTermDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.PartyHistoryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberImportService {

    private final CongressApiClient congressApiClient;
    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;
    private final MemberTermRepository memberTermRepository;
    private final MemberPartyHistoryRepository memberPartyHistoryRepository;

    public MemberImportService(CongressApiClient congressApiClient, MemberMapper memberMapper, MemberRepository memberRepository,MemberTermRepository memberTermRepository, MemberPartyHistoryRepository memberPartyHistoryRepository) {
        this.congressApiClient = congressApiClient;
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
        this.memberTermRepository = memberTermRepository;
        this.memberPartyHistoryRepository = memberPartyHistoryRepository;
    }

    public PreparedMembers prepareMembers() {
        List<MemberSummaryDto> summaries = congressApiClient.fetchAllMembers();
        List<Member> membersList = new ArrayList<>();
        List<MemberTerm> memberTermsList = new ArrayList<>();
        List<MemberPartyHistory> memberPartyHistoryList = new ArrayList<>();


        for (MemberSummaryDto summary : summaries) {
            String bioguideId = summary.getBioguideId();
            if (bioguideId == null || bioguideId.isBlank()) {
                throw new IllegalStateException("Congress.gov returned a member summary without a Bioguide ID");
            }

            MemberDetailDto detail = congressApiClient.fetchDetailMember(bioguideId).getMember();

            if (!bioguideId.equals(detail.getBioguideId())) {throw new IllegalStateException("Congress.gov returned mismatched member details for ID " + bioguideId);
            }

            Member member = memberMapper.toMember(summary);
            memberMapper.updateDetails(member, detail);
            membersList.add(member);
            for(MemberDetailTermDto memberDetailTermDto: detail.getTerms())
            {
                MemberTerm memberTerm = memberMapper.toMemberTerm(memberDetailTermDto, member);
                memberTermsList.add(memberTerm);
            }
            for(PartyHistoryDto partyHistoryDto: detail.getPartyHistory())
            {
                MemberPartyHistory memberPartyHistory = memberMapper.toMemberPartyHistory(partyHistoryDto, member);
                memberPartyHistoryList.add(memberPartyHistory);
            }
        }
        return new PreparedMembers(membersList, memberTermsList, memberPartyHistoryList);
    }

    @Transactional
    public List<Member> saveMembers(List<Member> members) {
        return memberRepository.saveAll(members);
    }
}
