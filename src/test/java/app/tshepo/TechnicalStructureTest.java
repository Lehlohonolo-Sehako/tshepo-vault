package app.tshepo;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.belongToAnyOf;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packagesOf = TshepoVaultApp.class, importOptions = DoNotIncludeTests.class)
class TechnicalStructureTest {

    // prettier-ignore
    @ArchTest
    static final ArchRule respectsTechnicalArchitectureLayers = layeredArchitecture()
        .consideringAllDependencies()
        .layer("Config").definedBy("..config..")
        .layer("Web").definedBy("..web..")
        .optionalLayer("Service").definedBy("..service..")
        .layer("Security").definedBy("..security..")
        .optionalLayer("Persistence").definedBy("..repository..")
        .layer("Domain").definedBy("..domain..")

        .whereLayer("Config").mayNotBeAccessedByAnyLayer()
        .whereLayer("Web").mayOnlyBeAccessedByLayers("Config")
        .whereLayer("Service").mayOnlyBeAccessedByLayers("Web", "Config")
        .whereLayer("Security").mayOnlyBeAccessedByLayers("Config", "Service", "Web")
        .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Service", "Security", "Web", "Config")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Persistence", "Service", "Security", "Web", "Config")

        .ignoreDependency(belongToAnyOf(TshepoVaultApp.class), alwaysTrue())
        .ignoreDependency(alwaysTrue(), belongToAnyOf(
            app.tshepo.config.Constants.class,
            app.tshepo.config.ApplicationProperties.class
        ))
        // OpenAPI-generated VMs in web.rest.vm are plain DTOs with no HTTP concerns.
        // Services use them directly to avoid a redundant service-DTO mapping layer.
        .ignoreDependency(resideInAPackage("..service.."), resideInAPackage("..web.rest.vm.."))
        // InvestecClient adapter lives in integration.investec — not a layered package
        .ignoreDependency(resideInAPackage("..service.."), resideInAPackage("..integration.."))
        .ignoreDependency(resideInAPackage("..web.."), resideInAPackage("..integration.."));
}
