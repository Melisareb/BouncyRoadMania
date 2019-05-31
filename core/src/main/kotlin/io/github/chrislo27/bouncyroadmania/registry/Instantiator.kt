package io.github.chrislo27.bouncyroadmania.registry

import io.github.chrislo27.bouncyroadmania.engine.Engine
import io.github.chrislo27.bouncyroadmania.engine.event.InstantiatedEvent
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.version.Version


class Instantiator(val id: String, val deprecatedIDs: List<String>, val sinceVersion: Version,
                   val name: String, val nameIsLocalizationKey: Boolean,
                   val summary: String, val summaryIsLocalizationKey: Boolean,
                   val desc: String, val descIsLocalizationKey: Boolean,
                   val factory: Instantiator.(Engine) -> InstantiatedEvent) {

    val displayName: String
        get() = if (nameIsLocalizationKey) Localization[name] else name

    val displaySummary: String
        get() = if (summaryIsLocalizationKey) Localization[summary] else summary

    val displayDesc: String
        get() = if (descIsLocalizationKey) Localization[desc] else desc

}
