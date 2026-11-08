package dev.capitolguide.member;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MemberRepository extends JpaRepository<Member, String>
{
    List<Member> findByStateAndChamber(String state, String chamber);
}