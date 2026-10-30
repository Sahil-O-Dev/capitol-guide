package dev.capitolguide.congress.dto;

public class TermDto
{
    private String chamber;
    private Integer startYear;

    public String getChamber() {
        return chamber;
    }

    public void setChamber(String chamber) {
        this.chamber = chamber;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }
}
