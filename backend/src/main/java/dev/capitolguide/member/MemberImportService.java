package dev.capitolguide.member;

import dev.capitolguide.congress.CongressApiClient;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberImportService {

    private final CongressApiClient congressApiClient;
    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;

    public MemberImportService(CongressApiClient congressApiClient, MemberMapper memberMapper,
                               MemberRepository memberRepository) {
        this.congressApiClient = congressApiClient;
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
    }

    public List<Member> prepareMembers() {
        List<MemberSummaryDto> summaries = congressApiClient.fetchAllMembers();
        List<Member> members = new ArrayList<>();

        for (MemberSummaryDto summary : summaries) {
            String bioguideId = summary.getBioguideId();
            if (bioguideId == null || bioguideId.isBlank()) {
                throw new IllegalStateException("Congress.gov returned a member summary without a Bioguide ID");
            }

            MemberDetailDto detail = congressApiClient.fetchDetailMember(bioguideId).getMember();
            if (!bioguideId.equals(detail.getBioguideId())) {
                throw new IllegalStateException("Congress.gov returned mismatched member details for ID " + bioguideId);
            }

            Member member = memberMapper.toMember(summary);
            memberMapper.updateDetails(member, detail);
            members.add(member);
        }

        return members;
    }

    @Transactional
    public List<Member> saveMembers(List<Member> members) {
        return memberRepository.saveAll(members);
    }
}
