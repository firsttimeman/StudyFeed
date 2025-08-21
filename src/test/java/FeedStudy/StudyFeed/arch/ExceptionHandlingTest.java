package FeedStudy.StudyFeed.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@AnalyzeClasses(packages = "FeedStudy.StudyFeed")
public class ExceptionHandlingTest {

    @Test
    void must_have_at_least_one_global_exception_handler() {
        JavaClasses classes = new ClassFileImporter().importPackages("FeedStudy.StudyFeed");

        boolean exists =
                classes.stream().anyMatch(c ->
                        c.isAnnotatedWith(RestControllerAdvice.class) ||
                        c.isAnnotatedWith(ControllerAdvice.class));

        assertThat(exists)
                .as("At least one @RestControllerAdvice/@ControllerAdvice must exist")
                .isTrue();
    }

    @ArchTest
    static final ArchRule handlers_return_standard_error =
            methods()
                    .that().areAnnotatedWith(ExceptionHandler.class)
                    .should().haveRawReturnType(assignableTo(ResponseEntity.class))   // ResponseEntity<...>
                    .orShould().haveRawReturnType(simpleNameEndingWith("ErrorResponse")); // 또는 ErrorResponse 직접 반환

}
