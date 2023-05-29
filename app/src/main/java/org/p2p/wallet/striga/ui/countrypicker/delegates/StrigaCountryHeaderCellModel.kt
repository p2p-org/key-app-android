package org.p2p.wallet.striga.ui.countrypicker.delegates

import androidx.annotation.StringRes
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.auth.repository.Country

class StrigaCountryHeaderCellModel(
    @StringRes val titleResId: Int
) : AnyCellItem

class StrigaCountryCellModel(
    val country: Country
) : AnyCellItem
