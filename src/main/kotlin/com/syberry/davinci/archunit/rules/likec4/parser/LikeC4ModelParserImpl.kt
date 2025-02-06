package com.syberry.davinci.archunit.rules.likec4.parser

import com.syberry.davinci.archunit.rules.likec4.model.LikeC4ModulesModel
import com.syberry.davinci.archunit.rules.likec4.model.Module
import com.syberry.davinci.archunit.rules.likec4.model.Relation
import com.syberry.davinci.archunit.rules.likec4.model.impl.LikeC4ModulesModelIml
import org.json.JSONObject

/**
 * Exception thrown when there is an error parsing the model.
 */
class ModelParsingException(
    message: String,
) : RuntimeException(message)

private const val KIND_MODULE = "module"

/**
 * Implementation of the LikeC4ModelParser interface.
 */
class LikeC4ModelParserImpl : LikeC4ModelParser {
    /**
     * Parses the given JSON string and returns a LikeC4ModulesModel.
     *
     * @param json the JSON string to parse
     * @param component the component to parse
     * @return the parsed LikeC4ModulesModel
     * @throws ModelParsingException if there is an error parsing the model
     */
    override fun parse(
        json: String,
        component: String,
    ): LikeC4ModulesModel {
        val (elements, relations, views) = DiagramParser.parseDiagram(JSONObject(json))
        val allNodes = getNodes(views, component)
        val componentNode =
            allNodes.find { it.getString("id") == component }
                ?: throw ModelParsingException("View is not found")

        val modulesElements = getModulesElements(elements)
        val modulesModels = ModuleFactory.createModules(componentNode, allNodes, modulesElements)
        val externalModuleIds = modulesElements.map { it["id"] as String } - ModuleFactory.getModuleIds(modulesModels)

        val relationsModels =
            relations.keySet().map { relations.getJSONObject(it) }.map {
                val source = it.getString("source")
                val target = it.getString("target")
                val isExternal = source in externalModuleIds || target in externalModuleIds

                Relation(source, target, isExternal)
            }

        return LikeC4ModulesModelIml(modules = modulesModels, relations = relationsModels)
    }

    /**
     * Returns a list of module elements from the given elements JSON object.
     *
     * @param elements the elements JSON object
     * @return the list of module elements
     */
    private fun getModulesElements(elements: JSONObject): List<JSONObject> =
        elements
            .keySet()
            .map {
                elements.getJSONObject(it)
            }.filter { it.getString("kind") == KIND_MODULE }

    /**
     * Returns a list of nodes from the given views JSON object and component.
     *
     * @param views the views JSON object
     * @param component the component to get nodes for
     * @return the list of nodes
     */
    private fun getNodes(
        views: JSONObject,
        component: String,
    ): List<JSONObject> {
        val view =
            views.keySet().map { views.getJSONObject(it) }.find {
                it.has("viewOf") && it.getString("viewOf") == component
            }
        return view?.getJSONArray("nodes")?.map { it as JSONObject } ?: listOf()
    }
}

/**
 * Factory object for creating Module instances.
 */
internal object ModuleFactory {
    /**
     * Creates a list of Module instances from the given component node, all nodes, and module elements.
     *
     * @param componentNode the component node
     * @param allNodes the list of all nodes
     * @param modulesElements the list of module elements
     * @return the list of Module instances
     */
    fun createModules(
        componentNode: JSONObject,
        allNodes: List<JSONObject>,
        modulesElements: List<JSONObject>,
    ): List<Module> =
        componentNode
            .getJSONArray("children")
            .map { it as String }
            .map { getModule(it, allNodes, modulesElements) }
            .mapNotNull { it }

    /**
     * Returns a Module instance for the given node ID, all nodes, and module elements.
     *
     * @param nodeId the node ID
     * @param allNodes the list of all nodes
     * @param allModulesElements the list of module elements
     * @return the Module instance, or null if not found
     */
    private fun getModule(
        nodeId: String,
        allNodes: List<JSONObject>,
        allModulesElements: List<JSONObject>,
    ): Module? {
        val model = allModulesElements.find { it.getString("id") == nodeId }
        val node = allNodes.find { it.getString("id") == nodeId }!!
        return model?.let {
            if (!model.has("metadata")) throw ModelParsingException("Invalid element $node without metadata")

            Module(
                id = model.getString("id"),
                name = model.getString("title"),
                modulePackage = model.getJSONObject("metadata").getString("package"),
                children =
                    node
                        .getJSONArray("children")
                        .map { it as String }
                        .map { childId -> getModule(childId, allNodes, allModulesElements) }
                        .mapNotNull { it },
            )
        }
    }

    /**
     * Returns a set of module IDs from the given list of modules.
     *
     * @param modules the list of modules
     * @param ids the set of IDs to add to (default is an empty set)
     * @return the set of module IDs
     */
    fun getModuleIds(
        modules: List<Module>,
        ids: MutableSet<String> = mutableSetOf(),
    ): Set<String> {
        modules.forEach { module ->
            ids.add(module.id)
            if (module.children.isNotEmpty()) {
                getModuleIds(module.children, ids)
            }
        }
        return ids
    }
}

/**
 * Object for parsing diagrams from JSON.
 */
internal object DiagramParser {
    /**
     * Parses a diagram from the given JSON object.
     *
     * @param json the JSON object to parse
     * @return a Triple containing the elements, relations, and views JSON objects
     */
    fun parseDiagram(json: JSONObject): Triple<JSONObject, JSONObject, JSONObject> {
        val elements = json.getJSONObject("elements")
        val relations = json.getJSONObject("relations")
        val views = json.getJSONObject("views")
        return Triple(elements, relations, views)
    }
}
