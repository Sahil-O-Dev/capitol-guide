package dev.capitolguide.member.dto;

import java.time.Instant;
import java.util.List;

public class MemberProfileResponseDto {
    private String bioguideId;
    private String name;
    private String partyName;
    private String state;
    private String chamber;
    private Integer district;
    private Integer chamberStartYear;
    private String imageUrl;
    private String imageAttribution;
    private String officialWebsiteUrl;
    private Boolean currentMember;
    private Instant sourceUpdateDate;
    private List<MemberTermResponseDto> terms = List.of();
    private List<PartyHistoryResponseDto> partyHistory = List.of();

    public String getBioguideId() {
        return bioguideId;
    }

    public void setBioguideId(String bioguideId) {
        this.bioguideId = bioguideId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getChamber() {
        return chamber;
    }

    public void setChamber(String chamber) {
        this.chamber = chamber;
    }

    public Integer getDistrict() {
        return district;
    }

    public void setDistrict(Integer district) {
        this.district = district;
    }

    public Integer getChamberStartYear() {
        return chamberStartYear;
    }

    public void setChamberStartYear(Integer chamberStartYear) {
        this.chamberStartYear = chamberStartYear;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageAttribution() {
        return imageAttribution;
    }

    public void setImageAttribution(String imageAttribution) {
        this.imageAttribution = imageAttribution;
    }

    public String getOfficialWebsiteUrl() {
        return officialWebsiteUrl;
    }

    public void setOfficialWebsiteUrl(String officialWebsiteUrl) {
        this.officialWebsiteUrl = officialWebsiteUrl;
    }

    public Boolean getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Boolean currentMember) {
        this.currentMember = currentMember;
    }

    public Instant getSourceUpdateDate() {
        return sourceUpdateDate;
    }

    public void setSourceUpdateDate(Instant sourceUpdateDate) {
        this.sourceUpdateDate = sourceUpdateDate;
    }

    public List<MemberTermResponseDto> getTerms() {
        return terms;
    }

    public void setTerms(List<MemberTermResponseDto> terms) {
        this.terms = terms;
    }

    public List<PartyHistoryResponseDto> getPartyHistory() {
        return partyHistory;
    }

    public void setPartyHistory(List<PartyHistoryResponseDto> partyHistory) {
        this.partyHistory = partyHistory;
    }
}
