package dev.capitolguide.congress.dto;

import java.util.List;

public class MemberListResponseDto
{
    private List<MemberSummaryDto> members;
    private PaginationDto pagination;

    public List<MemberSummaryDto> getMembers() {
        return members;
    }

    public void setMembers(List<MemberSummaryDto> members) {
        this.members = members;
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination;
    }
}