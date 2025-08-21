package FeedStudy.StudyFeed.arch;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "FeedStudy.StudyFeed")
public class LayeredArchTest {

    @ArchTest
    static final Architectures.LayeredArchitecture layered =
            Architectures.layeredArchitecture()

                    .consideringOnlyDependenciesInLayers()

                    .layer("Controller").definedBy("..controller..")
                    .layer("Service").definedBy("..service..")
                    .layer("Repository").definedBy("..repository..")

                    // 허용 방향
                    .whereLayer("Controller").mayOnlyAccessLayers("Service")
                    .whereLayer("Service").mayOnlyAccessLayers("Service", "Repository")
                    .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");
}
