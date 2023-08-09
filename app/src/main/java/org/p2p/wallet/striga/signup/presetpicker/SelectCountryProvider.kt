package org.p2p.wallet.striga.signup.presetpicker

import org.p2p.wallet.R
import org.p2p.wallet.auth.repository.CountryCodeRepository

class SelectCountryProvider(
    private val countryCodeRepository: CountryCodeRepository,
) : SelectItemProvider {
    override fun provideItemsName(): SelectItemsStrings {
        return SelectItemsStrings(
            toolbarTitleRes = R.string.select_country_toolbar_title,
            plural = org.p2p.uikit.R.plurals.plural_country
        )
    }

    override suspend fun provideItems(): List<SelectableItem> {
        return countryCodeRepository.getCountryCodes().map {
            SelectableItem(
                id = it.nameCodeAlpha3,
                itemIcon = null,
                itemEmoji = it.flagEmoji,
                itemTitle = it.countryName,
                itemSubtitle = null
            )
        }
    }

    override fun enableSearch(): Boolean = true
}
