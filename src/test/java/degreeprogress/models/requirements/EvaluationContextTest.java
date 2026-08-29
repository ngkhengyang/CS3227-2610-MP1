package degreeprogress.models.requirements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import degreeprogress.models.modules.Module;

class EvaluationContextTest {
    @Test
    void summarize_completedModules_returnsCountsAndCachesResult() {
        Module completedModule = new Module("CS2040S", 4, true);
        Module incompleteModule = new Module("CS2100", 4, false);
        EvaluationContext context = new EvaluationContext(List.of(completedModule, incompleteModule));
        ModuleSelector selector = ModuleSelector.forCodes("CS2040S", "CS2100");

        SelectorSummary first = context.summarize(selector);
        SelectorSummary second = context.summarize(selector);

        assertSame(first, second);
        assertEquals(1, first.matchedModules());
        assertEquals(4, first.matchedUnits());
    }

    @Test
    void countCompletedModules_ignoresIncompleteModules() {
        EvaluationContext context = new EvaluationContext(List.of(
                new Module("CS1231S", 4, true),
                new Module("CS2040S", 4, false)));

        assertEquals(1, context.countCompletedModules(Set.of("CS1231S", "CS2040S")));
    }

    @Test
    void constructor_duplicateCodes_rejectsContext() {
        assertThrows(IllegalArgumentException.class,
                () -> new EvaluationContext(List.of(
                        new Module("CS2040S", 4, true),
                        new Module("cs2040s", 4, false))));
    }
}
