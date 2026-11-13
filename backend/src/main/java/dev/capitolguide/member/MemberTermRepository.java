package dev.capitolguide.member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberTermRepository extends JpaRepository<MemberTerm, Long> {
    List<MemberTerm> findByMember_BioguideIdOrderByStartYearAscIdAsc(String bioguideId);
}
