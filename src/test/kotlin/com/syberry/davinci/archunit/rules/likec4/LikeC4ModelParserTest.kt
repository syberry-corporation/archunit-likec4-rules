package com.syberry.davinci.archunit.rules.likec4

import com.syberry.davinci.archunit.rules.likec4.model.LikeC4ModulesModel
import com.syberry.davinci.archunit.rules.likec4.parser.LikeC4ModelParserImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

class LikeC4ModelParserTest {
    private lateinit var model: LikeC4ModulesModel

    @BeforeEach
    fun setUp() {
        loadModel("likec4.json", "omega.crm")
    }

    private fun loadModel(
        resourcePath: String,
        component: String,
    ) {
        val resource = LikeC4ModelParserTest::class.java.classLoader.getResource(resourcePath)!!
        val parser = LikeC4ModelParserImpl()
        model =
            parser.parse(
                Files.readString(Path.of(resource.toURI())),
                component,
            )
    }

    @Test
    fun `List of modules`() {
        val modules = model.getAllModulesFlatten()

        assertEquals(
            listOf(
                "omega.crm.audit" to "..crm.audit..",
                "omega.crm.domain" to "..crm.domain..",
                "omega.crm.domain.account" to "..crm.domain.account..",
                "omega.crm.domain.contact" to "..crm.domain.contact..",
                "omega.crm.domain.lead" to "..crm.domain.lead..",
                "omega.crm.domain.opportunity" to "..crm.domain.opportunity..",
                "omega.crm.utils" to "..crm.utils..",
            ),
            modules.map { it.id to it.modulePackage }.sortedBy { it.second },
        )
    }

    @Test
    fun `List of modules within`() {
        val modules =
            model.getAllModulesWithin(
                "omega.crm.domain",
            )

        assertEquals(
            listOf(
                "omega.crm.domain.account" to "..crm.domain.account..",
                "omega.crm.domain.contact" to "..crm.domain.contact..",
                "omega.crm.domain.lead" to "..crm.domain.lead..",
                "omega.crm.domain.opportunity" to "..crm.domain.opportunity..",
            ),
            modules.map { it.id to it.modulePackage }.sortedBy { it.second },
        )
    }

    @Test
    fun `Dependencies of lead`() {
        val modules = model.getAllModulesFlatten()
        val lead = modules.find { it.id == "omega.crm.domain.lead" }!!
        assertEquals(
            listOf(
                "omega.crm.audit",
                "omega.crm.utils",
            ),
            model.getRelationsFrom(lead).map { it.id }.sorted(),
        )
        assertEquals(
            listOf(
                "omega.crm.domain.account",
                "omega.crm.domain.contact",
                "omega.crm.domain.opportunity",
            ),
            model.getRelationsTo(lead).map { it.id }.sorted(),
        )
    }

    @Test
    fun `Dependencies of opportunity`() {
        val modules = model.getAllModulesFlatten()
        val accountingCycle = modules.find { it.id == "omega.crm.domain.opportunity" }!!
        assertEquals(
            listOf(
                "omega.crm.audit",
                "omega.crm.domain.account",
                "omega.crm.domain.contact",
                "omega.crm.domain.lead",
                "omega.crm.utils",
            ),
            model.getRelationsFrom(accountingCycle).map { it.id }.sorted(),
        )
        assertEquals(
            listOf(),
            model.getRelationsTo(accountingCycle).map { it.id }.sorted(),
        )
    }

    @Test
    fun `Dependencies of asset`() {
        val modules = model.getAllModulesFlatten()
        val accountingCycle = modules.find { it.id == "omega.crm.domain.account" }!!
        assertEquals(
            listOf(
                "omega.crm.audit",
                "omega.crm.domain.lead",
                "omega.crm.utils",
            ),
            model.getRelationsFrom(accountingCycle).map { it.id }.sorted(),
        )
        assertEquals(
            listOf(
                "omega.crm.domain.opportunity",
            ),
            model.getRelationsTo(accountingCycle).map { it.id }.sorted(),
        )
    }

    @Test
    fun `Dependencies of utils`() {
        val modules = model.getAllModulesFlatten()
        val accountingCycle = modules.find { it.id == "omega.crm.utils" }!!
        assertEquals(
            listOf(),
            model.getRelationsFrom(accountingCycle).map { it.id }.sorted(),
        )
        assertEquals(
            listOf(
                "omega.crm.domain",
                "omega.crm.domain.account",
                "omega.crm.domain.contact",
                "omega.crm.domain.lead",
                "omega.crm.domain.opportunity",
            ),
            model.getRelationsTo(accountingCycle).map { it.id }.sorted(),
        )
    }
}
