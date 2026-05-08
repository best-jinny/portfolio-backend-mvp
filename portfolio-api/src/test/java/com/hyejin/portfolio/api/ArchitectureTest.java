package com.hyejin.portfolio.api;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class ArchitectureTest {
    private final com.tngtech.archunit.core.domain.JavaClasses classes =
        new ClassFileImporter().importPackages("com.hyejin.portfolio");

    @Test
    void domainPackagesDoNotDependOnSpring() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..")
            .check(classes);
    }

    @Test
    void domainPackagesDoNotDependOnAdaptersOrInfrastructure() {
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..infrastructure..")
            .check(classes);
    }

    @Test
    void controllersDoNotCallRepositoriesOrClientsDirectly() {
        noClasses()
            .that().haveSimpleNameEndingWith("Controller")
            .should().dependOnClassesThat().haveSimpleNameContaining("Repository")
            .orShould().dependOnClassesThat().haveSimpleNameContaining("Client")
            .orShould().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .check(classes);
    }

    @Test
    void applicationUseCasesDoNotDependOnWebControllers() {
        noClasses()
            .that().resideInAnyPackage("..application..", "..application.port.in..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.in.web..")
            .check(classes);
    }

    @Test
    void adapterOutDoesNotDependOnAdapterIn() {
        noClasses()
            .that().resideInAPackage("..adapter.out..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.in..")
            .check(classes);
    }

    @Test
    void packageSlicesDoNotHaveCycles() {
        slices()
            .matching("com.hyejin.portfolio.(*)..")
            .should().beFreeOfCycles()
            .check(classes);
    }

    @Test
    void apiControllersDoNotDependOnAdapterOutPackages() {
        noClasses()
            .that().resideInAPackage("..api.adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
            .check(classes);
    }

    @Test
    void allocationModuleDoesNotDependOnOtherBusinessModules() {
        noClasses()
            .that().resideInAPackage("..allocation..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..asset..",
                "..evidence..",
                "..simulation..",
                "..recommendation..",
                "..proposal.."
            )
            .check(classes);
    }

    @Test
    void businessFeatureModulesDoNotDependOnEachOther() {
        noClasses()
            .that().resideInAnyPackage("..asset..")
            .should().dependOnClassesThat().resideInAnyPackage("..proposal..", "..allocation..", "..simulation..", "..evidence..", "..recommendation..")
            .check(classes);

        noClasses()
            .that().resideInAnyPackage("..proposal..")
            .should().dependOnClassesThat().resideInAnyPackage("..asset..", "..allocation..", "..simulation..", "..evidence..", "..recommendation..")
            .check(classes);

        noClasses()
            .that().resideInAnyPackage("..allocation..")
            .should().dependOnClassesThat().resideInAnyPackage("..asset..", "..proposal..", "..simulation..", "..evidence..", "..recommendation..")
            .check(classes);

        noClasses()
            .that().resideInAnyPackage("..simulation..")
            .should().dependOnClassesThat().resideInAnyPackage("..asset..", "..proposal..", "..allocation..", "..evidence..", "..recommendation..")
            .check(classes);

        noClasses()
            .that().resideInAnyPackage("..evidence..")
            .should().dependOnClassesThat().resideInAnyPackage("..asset..", "..proposal..", "..allocation..", "..simulation..", "..recommendation..")
            .check(classes);

        noClasses()
            .that().resideInAnyPackage("..recommendation..")
            .should().dependOnClassesThat().resideInAnyPackage("..asset..", "..proposal..", "..allocation..", "..simulation..", "..evidence..")
            .allowEmptyShould(true)
            .check(classes);
    }
}
