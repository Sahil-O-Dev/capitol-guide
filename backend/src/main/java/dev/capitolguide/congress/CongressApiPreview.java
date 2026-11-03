package dev.capitolguide.congress;

import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("congress-preview")
public class CongressApiPreview implements ApplicationRunner {

    private final CongressApiClient congressApiClient;

    public CongressApiPreview(CongressApiClient congressApiClient) {
        this.congressApiClient = congressApiClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<MemberSummaryDto> members = congressApiClient.fetchAllMembers();

        System.out.println("Members received: " + members.size());
    }
}