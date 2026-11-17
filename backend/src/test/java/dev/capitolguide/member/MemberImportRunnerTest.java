package dev.capitolguide.member;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class MemberImportRunnerTest {
    @Test
    void savesPreparedMembers() {
        MemberImportService service = mock(MemberImportService.class);
        PreparedMembers prepared = new PreparedMembers(
                List.of(new Member()), List.of(new MemberTerm()), List.of(new MemberPartyHistory()));
        when(service.prepareMembers()).thenReturn(prepared);

        new MemberImportRunner(service).run(null);

        var calls = inOrder(service);
        calls.verify(service).prepareMembers();
        calls.verify(service).saveMembers(same(prepared));
    }

    @Test
    void doesNotSaveWhenPreparationFails() {
        MemberImportService service = mock(MemberImportService.class);
        when(service.prepareMembers()).thenThrow(new IllegalStateException("Fictional API failure"));

        assertThrows(IllegalStateException.class, () -> new MemberImportRunner(service).run(null));

        verify(service, never()).saveMembers(any());
    }
}
