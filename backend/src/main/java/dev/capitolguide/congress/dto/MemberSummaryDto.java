package dev.capitolguide.congress.dto;

public class MemberSummaryDto {
    private String bioguideId;
    private Integer district;
    private String name;
    private String partyName;
    private String state;
    private DepictionDto depiction;
    private TermListDto terms;

    public String getBioguideId() {
        return bioguideId;
    }

    public void setBioguideId(String bioguideId) {
        this.bioguideId = bioguideId;
    }

    public Integer getDistrict() {
        return district;
    }

    public void setDistrict(Integer district) {
        this.district = district;
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

    public DepictionDto getDepiction() {
        return depiction;
    }

    public void setDepiction(DepictionDto depiction) {
        this.depiction = depiction;
    }

    public TermListDto getTerms() {
        return terms;
    }

    public void setTerms(TermListDto terms) {
        this.terms = terms;
    }
}