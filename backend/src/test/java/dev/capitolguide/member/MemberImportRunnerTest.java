package dev.capitolguide.member;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MemberImportRunnerTest {
    @Test
    void savesPreparedMembers() {
        MemberImportService service = mock(MemberImportService.class);
        List<Member> members = List.of(new Member());
        when(service.prepareMembers()).thenReturn(members);

        new MemberImportRunner(service).run(null);

        var calls = inOrder(service);
        calls.verify(service).prepareMembers();
        calls.verify(service).saveMembers(same(members));
    }

    @Test
    void doesNotSaveWhenPreparationFails() {
        MemberImportService service = mock(MemberImportService.class);
        when(service.prepareMembers()).thenThrow(new IllegalStateException("Fictional API failure"));

        assertThrows(IllegalStateException.class, () -> new MemberImportRunner(service).run(null));

        verify(service, never()).saveMembers(anyList());
    }
}
