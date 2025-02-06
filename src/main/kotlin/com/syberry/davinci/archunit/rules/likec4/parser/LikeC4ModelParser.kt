package com.syberry.davinci.archunit.rules.likec4.parser

import com.syberry.davinci.archunit.rules.likec4.model.LikeC4ModulesModel

/**
 * Interface for parsing LikeC4 models.
 */
interface LikeC4ModelParser {
    /**
     * Parses the given JSON string and returns a LikeC4ModulesModel.
     *
     * @param json the JSON string to parse
     * @param component the component to parse
     * @return the parsed LikeC4ModulesModel
     */
    fun parse(
        json: String,
        component: String,
    ): LikeC4ModulesModel
}
