package dev.capitolguide.member;

import java.util.List;

public record PreparedMembers(
        List<Member> members,
        List<MemberTerm> terms,
        List<MemberPartyHistory> partyHistory
) {}