package dev.capitolguide.member;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;


@Entity
@Table(name = "members")
public class Member
{
    @Id
    private String bioguideId;
    private String name;
    private String partyName;
    private String state;
    private String chamber;
    private String imageUrl;
    private String imageAttribution;
    private Integer district;
    private Integer chamberStartYear;
    private String officialWebsiteUrl;
    private Boolean currentMember;
    private Instant sourceUpdateDate;


    public String getBioguideId() {
        return bioguideId;
    }

    public void setBioguideId(String bioguideId) {
        this.bioguideId = bioguideId;
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

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getDistrict() {
        return district;
    }

    public void setDistrict(Integer district) {
        this.district = district;
    }

    public Boolean getCurrentMember() {
        return currentMember;
    }

    public void setCurrentMember(Boolean currentMember) {
        this.currentMember = currentMember;
    }

    public String getOfficialWebsiteUrl() {
        return officialWebsiteUrl;
    }

    public void setOfficialWebsiteUrl(String officialWebsiteUrl) {
        this.officialWebsiteUrl = officialWebsiteUrl;
    }

    public Instant getSourceUpdateDate() {
        return sourceUpdateDate;
    }

    public void setSourceUpdateDate(Instant sourceUpdateDate) {
        this.sourceUpdateDate = sourceUpdateDate;
    }
}
