package com.wafflestudio.csereal.common.config

import org.hibernate.boot.model.FunctionContributions
import org.hibernate.dialect.DatabaseVersion
import org.hibernate.dialect.MySQLDialect
import org.hibernate.type.StandardBasicTypes

class MySQLDialectCustom : MySQLDialect(
    DatabaseVersion.make(8)
) {
    override fun initializeFunctionRegistry(functionContributions: FunctionContributions?) {
        super.initializeFunctionRegistry(functionContributions)

        val basicTypeRegistry = functionContributions?.typeConfiguration?.basicTypeRegistry
        val functionRegistry = functionContributions?.functionRegistry

        if (basicTypeRegistry != null && functionRegistry != null) {
            functionRegistry.registerPattern(
                "match",
                "match (?1) against (?2 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match2",
                "match (?1, ?2) against (?3 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match3",
                "match (?1, ?2, ?3) against (?4 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match4",
                "match (?1, ?2, ?3, ?4) against (?5 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match5",
                "match (?1, ?2, ?3, ?4, ?5) against (?6 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match6",
                "match (?1, ?2, ?3, ?4, ?5, ?6) against (?7 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match7",
                "match (?1, ?2, ?3, ?4, ?5, ?6, ?7) against (?8 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )

            functionRegistry.registerPattern(
                "match8",
                "match (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8) against (?9 in boolean mode)",
                basicTypeRegistry.resolve(StandardBasicTypes.DOUBLE)
            )
        }
    }
}
