package dev.capitolguide.congress.dto;

import java.util.List;

public class MemberDetailDto {
    private String bioguideId;
    private String directOrderName;
    private Boolean currentMember;
    private String state;
    private Integer district;
    private DepictionDto depiction;
    private String officialWebsiteUrl;
    private List<MemberDetailTermDto> terms;
    private List<PartyHistoryDto> partyHistory;
    private String updateDate;

    public String getBioguideId() {
        return bioguideId;
    }

    public void setBioguideId(String bioguideId) {
        this.bioguideId = bioguideId;
    }

    public String getDirectOrderName() {
        return directOrderName;
    }

    public void setDirectOrderName(String directOrderName) {
        this.directOrderName = directOrderName;
    }

    public Boolean getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Boolean currentMember) {
        this.currentMember = currentMember;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getDistrict() {
        return district;
    }

    public void setDistrict(Integer district) {
        this.district = district;
    }

    public DepictionDto getDepiction() {
        return depiction;
    }

    public void setDepiction(DepictionDto depiction) {
        this.depiction = depiction;
    }

    public String getOfficialWebsiteUrl() {
        return officialWebsiteUrl;
    }

    public void setOfficialWebsiteUrl(String officialWebsiteUrl) {
        this.officialWebsiteUrl = officialWebsiteUrl;
    }

    public List<MemberDetailTermDto> getTerms() {
        return terms;
    }

    public void setTerms(List<MemberDetailTermDto> terms) {
        this.terms = terms;
    }

    public List<PartyHistoryDto> getPartyHistory() {
        return partyHistory;
    }

    public void setPartyHistory(List<PartyHistoryDto> partyHistory) {
        this.partyHistory = partyHistory;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
