package com.wafflestudio.csereal.common.config

import org.hibernate.dialect.DatabaseVersion
import org.hibernate.dialect.MySQLDialect
import org.hibernate.query.spi.QueryEngine
import org.hibernate.type.StandardBasicTypes


class MySQLDialectCustom: MySQLDialect(
        DatabaseVersion.make(8)
) {
    override fun initializeFunctionRegistry(queryEngine: QueryEngine?) {
        super.initializeFunctionRegistry(queryEngine)

        val basicTypeRegistry = queryEngine?.typeConfiguration?.basicTypeRegistry
        val functionRegistry = queryEngine?.sqmFunctionRegistry

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
        }
    }
}