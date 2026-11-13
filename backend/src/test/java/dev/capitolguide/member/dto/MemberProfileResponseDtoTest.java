package dev.capitolguide.member.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemberProfileResponseDtoTest {
    @Test
    void serializesNestedHistoriesAndTheSourceDate() {
        MemberProfileResponseDto profile = new MemberProfileResponseDto();
        profile.setBioguideId("DEV_PROFILE001");
        profile.setName("Fictional Profile Member");
        profile.setPartyName("Test Party");
        profile.setState("Virginia");
        profile.setChamber("Senate");
        profile.setChamberStartYear(2025);
        profile.setImageUrl("https://example.com/fictional-photo.jpg");
        profile.setImageAttribution("Fictional photo credit");
        profile.setOfficialWebsiteUrl("https://example.com/fictional-profile");
        profile.setCurrentMember(true);
        profile.setSourceUpdateDate(Instant.parse("2026-11-13T12:00:00Z"));

        MemberTermResponseDto term = new MemberTermResponseDto();
        term.setChamber("Senate");
        term.setCongress(119);
        term.setStartYear(2025);
        profile.setTerms(List.of(term));
        PartyHistoryResponseDto party = new PartyHistoryResponseDto();
        party.setPartyAbbreviation("T");
        party.setPartyName("Test Party");
        party.setStartYear(2025);
        profile.setPartyHistory(List.of(party));

        JsonMapper mapper = JsonMapper.builder().build();
        var json = mapper.readTree(mapper.writeValueAsString(profile));

        assertEquals("DEV_PROFILE001", json.get("bioguideId").asString());
        assertEquals("Fictional Profile Member", json.get("name").asString());
        assertEquals("https://example.com/fictional-photo.jpg", json.get("imageUrl").asString());
        assertEquals("Fictional photo credit", json.get("imageAttribution").asString());
        assertEquals("2026-11-13T12:00:00Z", json.get("sourceUpdateDate").asString());
        assertEquals(1, json.get("terms").size());
        assertEquals(119, json.get("terms").get(0).get("congress").asInt());
        assertTrue(json.get("terms").get(0).get("endYear").isNull());
        assertEquals(1, json.get("partyHistory").size());
        assertEquals("Test Party", json.get("partyHistory").get(0).get("partyName").asString());
        assertTrue(json.get("partyHistory").get(0).get("endYear").isNull());
        assertFalse(json.get("terms").get(0).has("member"));
        assertFalse(json.get("partyHistory").get(0).has("member"));
        assertFalse(json.has("depiction"));
        assertFalse(json.has("updateDate"));
    }

    @Test
    void serializesEmptyHistoriesAsArrays() {
        JsonMapper mapper = JsonMapper.builder().build();
        var json = mapper.readTree(mapper.writeValueAsString(new MemberProfileResponseDto()));

        assertTrue(json.get("terms").isArray());
        assertEquals(0, json.get("terms").size());
        assertTrue(json.get("partyHistory").isArray());
        assertEquals(0, json.get("partyHistory").size());
        assertTrue(json.get("sourceUpdateDate").isNull());
    }
}
