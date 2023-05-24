package org.p2p.wallet.smsinput

import org.junit.Test
import kotlin.test.assertTrue
import org.p2p.wallet.auth.ui.pin.newcreate.NewCreatePinFragment
import org.p2p.wallet.smsinput.onboarding.OnboardingSmsInputFragment
import org.p2p.wallet.smsinput.striga.StrigaSmsInputFragment

class SmsInputFactoryTest {

    @Test
    fun `GIVEN onboarding target WHEN sms input factory creates onboarding THEN check OnboardingSmsInputFragment is result`() {
        val type = SmsInputFactory.Type.Onboarding
        val destinationFragment = NewCreatePinFragment::class.java
        val destinationArgs = null
        val fragment = SmsInputFactory.create(type, destinationFragment, destinationArgs)

        assertTrue(fragment is OnboardingSmsInputFragment)
    }

    @Test
    fun `GIVEN striga target WHEN sms input factory creates striga THEN check StrigaSmsInputFragment is result`() {
        val type = SmsInputFactory.Type.Striga
        val destinationFragment = NewCreatePinFragment::class.java
        val destinationArgs = null
        val fragment = SmsInputFactory.create(type, destinationFragment, destinationArgs)

        assertTrue(fragment is StrigaSmsInputFragment)
    }
}
