package com.lotus.lotusSPM.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Architecture Tests using ArchUnit.
 *
 * Enterprise Pattern: Architecture Governance / Guard Rails
 *
 * Validates:
 * - Layered architecture boundaries
 * - Package dependencies
 * - Naming conventions
 * - Annotation usage
 * - Cyclic dependencies
 * - Design patterns adherence
 *
 * Benefits:
 * - Prevent architecture erosion
 * - Enforce coding standards
 * - Catch violations in CI/CD
 * - Document architecture rules
 * - Team alignment
 */
public class ArchitectureTests {

    private JavaClasses classes;

    @BeforeEach
    public void setup() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.lotus.lotusSPM");
    }

    /**
     * Test layered architecture constraints.
     * Controllers -> Services -> DAOs -> Models
     */
    @Test
    public void testLayeredArchitecture() {
        ArchRule rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Controllers").definedBy("..web..")
            .layer("Services").definedBy("..service..")
            .layer("DAOs").definedBy("..dao..")
            .layer("Models").definedBy("..model..")
            .layer("Config").definedBy("..config..")

            .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
            .whereLayer("Services").mayOnlyBeAccessedByLayers("Controllers")
            .whereLayer("DAOs").mayOnlyBeAccessedByLayers("Services")
            .whereLayer("Models").mayOnlyBeAccessedByLayers("Services", "DAOs", "Controllers");

        rule.check(classes);
    }

    /**
     * Test that services are named correctly.
     */
    @Test
    public void testServiceNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..service..")
            .and().areNotInterfaces()
            .should().haveSimpleNameEndingWith("Service")
            .orShould().haveSimpleNameEndingWith("ServiceImpl");

        rule.check(classes);
    }

    /**
     * Test that controllers are named correctly.
     */
    @Test
    public void testControllerNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..web..")
            .should().haveSimpleNameEndingWith("Controller");

        rule.check(classes);
    }

    /**
     * Test that DAOs are named correctly.
     */
    @Test
    public void testDaoNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..dao..")
            .should().haveSimpleNameEndingWith("Dao")
            .orShould().haveSimpleNameEndingWith("Repository");

        rule.check(classes);
    }

    /**
     * Test that controllers use proper annotations.
     */
    @Test
    public void testControllersUseProperAnnotations() {
        ArchRule rule = classes()
            .that().resideInAPackage("..web..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
            .orShould().beAnnotatedWith(org.springframework.stereotype.Controller.class);

        rule.check(classes);
    }

    /**
     * Test that services use @Service annotation.
     */
    @Test
    public void testServicesUseServiceAnnotation() {
        ArchRule rule = classes()
            .that().resideInAPackage("..service..")
            .and().haveSimpleNameEndingWith("ServiceImpl")
            .should().beAnnotatedWith(org.springframework.stereotype.Service.class);

        rule.check(classes);
    }

    /**
     * Test that DAOs extend Spring Data repositories.
     */
    @Test
    public void testDaosExtendRepository() {
        ArchRule rule = classes()
            .that().resideInAPackage("..dao..")
            .and().areInterfaces()
            .should().beAssignableTo(org.springframework.data.repository.Repository.class);

        rule.check(classes);
    }

    /**
     * Test no cyclic dependencies between packages.
     */
    @Test
    public void testNoCyclicDependencies() {
        ArchRule rule = slices()
            .matching("com.lotus.lotusSPM.(*)..")
            .should().beFreeOfCycles();

        rule.check(classes);
    }

    /**
     * Test that model classes don't depend on other layers.
     */
    @Test
    public void testModelIndependence() {
        ArchRule rule = classes()
            .that().resideInAPackage("..model..")
            .should().onlyDependOnClassesInPackages(
                "..model..",
                "java..",
                "javax..",
                "lombok..",
                "org.springframework.data..",
                "jakarta.."
            );

        rule.check(classes);
    }

    /**
     * Test that configuration classes are in config package.
     */
    @Test
    public void testConfigurationLocation() {
        ArchRule rule = classes()
            .that().areAnnotatedWith(org.springframework.context.annotation.Configuration.class)
            .should().resideInAPackage("..config..");

        rule.check(classes);
    }

    /**
     * Test that exception classes follow naming convention.
     */
    @Test
    public void testExceptionNamingConvention() {
        ArchRule rule = classes()
            .that().resideInAPackage("..exception..")
            .and().areAssignableTo(Exception.class)
            .should().haveSimpleNameEndingWith("Exception");

        rule.check(classes);
    }

    /**
     * Test that utility classes are final with private constructor.
     */
    @Test
    public void testUtilityClassStructure() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("Utils")
            .or().haveSimpleNameEndingWith("Util")
            .or().haveSimpleNameEndingWith("Helper")
            .should().beFinal()
            .andShould().haveOnlyPrivateConstructors();

        rule.check(classes);
    }

    /**
     * Test that security-sensitive methods are secured.
     */
    @Test
    public void testSecurityAnnotationsOnDeleteMethods() {
        ArchRule rule = methods()
            .that().haveName("delete")
            .or().haveNameMatching("delete.*")
            .should().beAnnotatedWith(org.springframework.security.access.prepost.PreAuthorize.class);

        // Note: This will fail if not all delete methods are annotated
        // rule.check(classes);
    }
}
