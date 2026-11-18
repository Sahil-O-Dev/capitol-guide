package dev.capitolguide.member;

import dev.capitolguide.congress.CongressApiClient;
import dev.capitolguide.congress.dto.MemberDetailDto;
import dev.capitolguide.congress.dto.MemberDetailResponseDto;
import dev.capitolguide.congress.dto.MemberDetailTermDto;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import dev.capitolguide.congress.dto.PartyHistoryDto;
import dev.capitolguide.congress.dto.TermDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        if (summaries == null) {
            throw new IllegalStateException("Congress.gov returned no member list");
        }
        List<Member> membersList = new ArrayList<>();
        List<MemberTerm> memberTermsList = new ArrayList<>();
        List<MemberPartyHistory> memberPartyHistoryList = new ArrayList<>();


        for (MemberSummaryDto summary : summaries) {
            if (summary == null) {
                throw new IllegalStateException("Congress.gov returned a null member summary");
            }
            String bioguideId = summary.getBioguideId();
            if (bioguideId == null || bioguideId.isBlank()) {
                throw new IllegalStateException("Congress.gov returned a member summary without a Bioguide ID");
            }
            validateSummary(summary);

            MemberDetailResponseDto response = congressApiClient.fetchDetailMember(bioguideId);
            if (response == null || response.getMember() == null) {
                throw new IllegalStateException("Congress.gov returned no member details for ID " + bioguideId);
            }
            MemberDetailDto detail = response.getMember();

            if (!bioguideId.equals(detail.getBioguideId())) {throw new IllegalStateException("Congress.gov returned mismatched member details for ID " + bioguideId);
            }
            validateHistories(detail, bioguideId);

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

    private void validateSummary(MemberSummaryDto summary) {
        String id = summary.getBioguideId();
        requireText(summary.getName(), "name", id);
        requireText(summary.getPartyName(), "party name", id);
        requireText(summary.getState(), "state", id);

        if (summary.getTerms() == null || summary.getTerms().getItem() == null
                || summary.getTerms().getItem().isEmpty()) {
            throw new IllegalStateException("Congress.gov returned no summary service terms for ID " + id);
        }
        TermDto latestTerm = summary.getTerms().getItem().getLast();
        if (latestTerm == null) {
            throw new IllegalStateException("Congress.gov returned a null latest service term for ID " + id);
        }
        requireText(latestTerm.getChamber(), "chamber", id);
        if (!"Senate".equals(latestTerm.getChamber())
                && !"House of Representatives".equals(latestTerm.getChamber())) {
            throw new IllegalStateException("Congress.gov returned an unsupported chamber for ID " + id);
        }
    }

    private void requireText(String value, String field, String id) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Congress.gov returned a missing or blank " + field + " for ID " + id);
        }
    }

    private void validateHistories(MemberDetailDto detail, String id) {
        if (detail.getTerms() == null || detail.getPartyHistory() == null) {
            throw new IllegalStateException("Congress.gov returned missing history lists for ID " + id);
        }
        if (detail.getTerms().stream().anyMatch(term -> term == null)
                || detail.getPartyHistory().stream().anyMatch(party -> party == null)) {
            throw new IllegalStateException("Congress.gov returned a null history entry for ID " + id);
        }
    }

    @Transactional
    public void saveMembers(PreparedMembers preparedMembers) {
        if (preparedMembers.members().isEmpty()) {
            throw new IllegalStateException("Refusing to replace members with an empty import");
        }

        Set<String> incomingIds = new HashSet<>();
        for (Member member : preparedMembers.members()) {
            requireText(member.getBioguideId(), "Bioguide ID", member.getBioguideId());
            if (!incomingIds.add(member.getBioguideId())) {
                throw new IllegalStateException("Duplicate member ID in import: " + member.getBioguideId());
            }
        }

        for (Member member : preparedMembers.members())
        {
            String id = member.getBioguideId();
            memberTermRepository.deleteByMember_BioguideId(id);
            memberPartyHistoryRepository.deleteByMember_BioguideId(id);
        }

        memberRepository.saveAll(preparedMembers.members());
        memberTermRepository.saveAll(preparedMembers.terms());
        memberPartyHistoryRepository.saveAll(preparedMembers.partyHistory());
    }
}
