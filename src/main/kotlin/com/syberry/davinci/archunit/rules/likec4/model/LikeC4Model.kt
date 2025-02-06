package com.syberry.davinci.archunit.rules.likec4.model

/**
 * Data class representing a module in the LikeC4 model.
 *
 * @property id the ID of the module
 * @property name the name of the module
 * @property modulePackage the package of the module
 * @property children the list of child modules
 */
data class Module(
    val id: String,
    val name: String,
    val modulePackage: String,
    val children: List<Module>,
) {
    private var _parent: Module? = null

    init {
        children.forEach { it._parent = this }
    }

    /**
     * The parent module of this module.
     */
    val parent: Module?
        get() {
            return _parent
        }
}

/**
 * Data class representing a relation between modules in the LikeC4 model.
 *
 * @property sourceModuleId the ID of the source module
 * @property targetModuleId the ID of the target module
 * @property isExternal whether the relation is external
 */
data class Relation(
    val sourceModuleId: String,
    val targetModuleId: String,
    val isExternal: Boolean,
)

/**
 * Interface representing the LikeC4 modules model.
 */
interface LikeC4ModulesModel {
    /**
     * Returns the list of modules that the given module has relations from.
     *
     * @param module the module to get relations from
     * @return the list of modules
     */
    fun getRelationsFrom(module: Module): List<Module>

    /**
     * Returns the list of modules that have relations to the given module.
     *
     * @param module the module to get relations to
     * @return the list of modules
     */
    fun getRelationsTo(module: Module): List<Module>

    /**
     * Returns the list of all modules within the given module ID.
     *
     * @param moduleId the ID of the module
     * @return the list of modules
     */
    fun getAllModulesWithin(moduleId: String): List<Module>

    /**
     * Returns the list of all modules flattened.
     *
     * @return the list of modules
     */
    fun getAllModulesFlatten(): List<Module>
}
