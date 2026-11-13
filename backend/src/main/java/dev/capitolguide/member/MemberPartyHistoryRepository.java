package dev.capitolguide.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberPartyHistoryRepository extends JpaRepository<MemberPartyHistory, Long> {
    List<MemberPartyHistory> findByMember_BioguideIdOrderByStartYearAscIdAsc(String bioguideId);
}
