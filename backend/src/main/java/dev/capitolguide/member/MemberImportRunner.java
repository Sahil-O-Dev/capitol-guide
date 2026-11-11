package dev.capitolguide.member;

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

    public MemberImportRunner(MemberImportService memberImportService)
    {
        this.memberImportService = memberImportService;
    }


    @Override
    public void run(ApplicationArguments args)
    {
        List<Member> members = memberImportService.prepareMembers();
        memberImportService.saveMembers(members);
    }
}
