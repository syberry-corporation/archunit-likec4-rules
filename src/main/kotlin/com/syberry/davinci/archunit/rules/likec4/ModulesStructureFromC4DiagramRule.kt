package com.syberry.davinci.archunit.rules.likec4

import com.syberry.davinci.archunit.rules.likec4.loader.LikeC4ModelLoader
import com.syberry.davinci.archunit.rules.likec4.parser.LikeC4ModelParserImpl
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.Architectures.layeredArchitecture

/**
 * Rule checks that modules structure aligns with likec4 diagram.
 *
 * @param component - id of software component
 * @param loader - function that'll provide json of likec4 diagram (run `likec4 export json` to export diagram to json)
 * @return the ArchRule that checks the module structure
 */
fun moduleStructureFromLikec4DiagramRule(
    component: String,
    loader: LikeC4ModelLoader,
): ArchRule {
    val c4diagamJson = loader.load()
    val parser = LikeC4ModelParserImpl()
    val model = parser.parse(component = component, json = c4diagamJson)
    val modules = model.getAllModulesFlatten()

    var architecture =
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()

    modules.forEach {
        architecture =
            architecture
                .layer(it.id)
                .definedBy(it.modulePackage)
    }

    modules.forEach {
        val relationsFrom = model.getRelationsFrom(it)
        val relationsTo = model.getRelationsTo(it)

        architecture =
            architecture
                .whereLayer(it.id)
                .mayOnlyAccessLayers(relationsFrom.map { module -> module.id })
                .whereLayer(it.id)
                .mayOnlyBeAccessedByLayers(relationsTo.map { module -> module.id })
    }

    return architecture.`as`("Modules structure must align with likec4 diagram")
}

/**
 * Extension function to specify which layers a layer may only access.
 *
 * @param layers the list of layers that may be accessed
 * @return the updated LayeredArchitecture
 */
@Suppress("SpreadOperator") // we cannot skip spread operator because it's part of archunit syntax
private fun Architectures.LayeredArchitecture.LayerDependencySpecification.mayOnlyAccessLayers(
    layers: List<String>,
): Architectures.LayeredArchitecture =
    if (layers.isEmpty()) {
        this.mayNotAccessAnyLayer()
    } else {
        this.mayOnlyAccessLayers(*layers.toTypedArray())
    }

/**
 * Extension function to specify which layers a layer may only be accessed by.
 *
 * @param layers the list of layers that may access this layer
 * @return the updated LayeredArchitecture
 */
@Suppress("SpreadOperator") // we cannot skip spread operator because it's part of archunit syntax
private fun Architectures.LayeredArchitecture.LayerDependencySpecification.mayOnlyBeAccessedByLayers(
    layers: List<String>,
): Architectures.LayeredArchitecture =
    if (layers.isEmpty()) {
        this.mayNotBeAccessedByAnyLayer()
    } else {
        this.mayOnlyBeAccessedByLayers(*layers.toTypedArray())
    }
