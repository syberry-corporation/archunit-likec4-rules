package com.syberry.davinci.archunit.rules.likec4.model.impl

import com.syberry.davinci.archunit.rules.likec4.model.LikeC4ModulesModel
import com.syberry.davinci.archunit.rules.likec4.model.Module
import com.syberry.davinci.archunit.rules.likec4.model.Relation

/**
 * Exception thrown when there is an error in the LikeC4 model.
 */
class LikeC4ModelException(
    message: String,
) : RuntimeException(message)

/**
 * Implementation of the LikeC4ModulesModel interface.
 *
 * @property modules the list of modules
 * @property relations the list of relations
 */
internal class LikeC4ModulesModelIml(
    private val modules: List<Module>,
    private val relations: List<Relation>,
) : LikeC4ModulesModel {
    /**
     * Returns the list of modules that the given module has relations from.
     *
     * @param module the module to get relations from
     * @return the list of modules
     */
    override fun getRelationsFrom(module: Module): List<Module> {
        val moduleRelations = getModuleRelations(module, RelationType.FROM)
        val parentRelations = module.parent?.let { getRelationsFrom(it) } ?: listOf()
        return moduleRelations + parentRelations
    }

    /**
     * Returns the list of modules that have relations to the given module.
     *
     * @param module the module to get relations to
     * @return the list of modules
     */
    override fun getRelationsTo(module: Module): List<Module> {
        val moduleRelations = getModuleRelations(module, RelationType.TO)
        val parentRelations = module.parent?.let { getRelationsTo(it) } ?: listOf()
        return moduleRelations + parentRelations
    }

    /**
     * Returns the list of all modules within the given module ID.
     *
     * @param moduleId the ID of the module
     * @return the list of modules
     * @throws LikeC4ModelException if the module is not found
     */
    override fun getAllModulesWithin(moduleId: String): List<Module> {
        val module = getModule(moduleId) ?: throw LikeC4ModelException("Module $moduleId not found")
        return module.children.flatMap { getModulesLeafs(it) }
    }

    /**
     * Returns the list of all modules flattened.
     *
     * @return the list of modules
     */
    override fun getAllModulesFlatten(): List<Module> = modules.flatMap { getModulesLeafs(it) }

    /**
     * Returns the list of modules related to the given module based on the relation type.
     *
     * @param module the module to get relations for
     * @param relationType the type of relation (FROM or TO)
     * @return the list of related modules
     * @throws LikeC4ModelException if a related module is not found
     */
    private fun getModuleRelations(
        module: Module,
        relationType: RelationType,
    ): List<Module> {
        val filteredRelations = relations.filter { !it.isExternal }
        val relatedModuleIds =
            when (relationType) {
                RelationType.FROM ->
                    filteredRelations
                        .filter {
                            it.sourceModuleId == module.id
                        }.map { it.targetModuleId }
                RelationType.TO -> filteredRelations.filter { it.targetModuleId == module.id }.map { it.sourceModuleId }
            }
        return relatedModuleIds
            .map {
                getModule(it)
                    ?: throw LikeC4ModelException("Invalid module in relation: $it does not exist in the model")
            }.flatMap { listOf(it) + getAllChildren(it) }
    }

    private fun getModule(id: String): Module? = findModule(modules, id)

    private fun findModule(
        modules: List<Module>,
        id: String,
    ): Module? = modules.find { it.id == id } ?: modules.mapNotNull { findModule(it.children, id) }.firstOrNull()

    private fun getAllChildren(module: Module): List<Module> =
        module.children + module.children.flatMap { getAllChildren(it) }

    private fun getModulesLeafs(module: Module): List<Module> =
        if (module.children.isEmpty()) {
            listOf(module)
        } else {
            module.children.flatMap { getModulesLeafs(it) } +
                listOf(module)
        }

    private enum class RelationType {
        FROM,
        TO,
    }
}
