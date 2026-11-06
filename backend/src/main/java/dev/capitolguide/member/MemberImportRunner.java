package dev.capitolguide.member;

import dev.capitolguide.congress.CongressApiClient;
import dev.capitolguide.congress.dto.MemberSummaryDto;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("congress-import")
public class MemberImportRunner implements ApplicationRunner
{
    private final MemberImportService memberImportService;
    private final CongressApiClient congressApiClient;

    public MemberImportRunner(MemberImportService memberImportService, CongressApiClient congressApiClient)
    {
        this.memberImportService = memberImportService;
        this.congressApiClient = congressApiClient;
    }


    @Override
    public void run(ApplicationArguments args)
    {
        List<MemberSummaryDto> memberSummaryDtoList = congressApiClient.fetchAllMembers();
        memberImportService.saveMembers(memberSummaryDtoList);
    }
}
