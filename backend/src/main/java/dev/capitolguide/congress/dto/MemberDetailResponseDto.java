package dev.capitolguide.congress.dto;

public class MemberDetailResponseDto {
    private MemberDetailDto member;

    public MemberDetailDto getMember() {
        return member;
    }

    public void setMember(MemberDetailDto member) {
        this.member = member;
    }
}
