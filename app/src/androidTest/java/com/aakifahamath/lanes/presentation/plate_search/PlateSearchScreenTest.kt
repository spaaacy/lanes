package com.aakifahamath.lanes.presentation.plate_search

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.aakifahamath.lanes.R
import com.aakifahamath.lanes.data.authentication.FirebaseAuthentication
import com.aakifahamath.lanes.domain.repository.Repository
import com.aakifahamath.lanes.presentation.theme.AppTheme
import com.aakifahamath.lanes.util.LOG_TAG
import com.aakifahamath.lanes.util.NUMBER_REGEX_PATTERN
import com.aakifahamath.lanes.util.PREFIX_REGEX_PATTERN
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class PlateSearchScreenTest {

    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var authentication: FirebaseAuthentication


    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.setContent {
            AppTheme {
                PlateSearchScreen(
                    navigator = EmptyDestinationsNavigator,
                    PlateSearchViewModel(repository, authentication)
                )
            }
        }

        val prefixLabel = composeRule.activity.getString(R.string.prefix_label)
        prefixTextField = composeRule.onNodeWithText(prefixLabel)

        val numberLabel = composeRule.activity.getString(R.string.number_label)
        numberTextField = composeRule.onNodeWithText(numberLabel)
    }

    private val asciiRange = 32..126 // 'SPACE' to '~'
    private val prefixCharLimit = 3
    private val numberCharLimit = 4
    private lateinit var prefixTextField: SemanticsNodeInteraction
    private lateinit var numberTextField: SemanticsNodeInteraction

    /*
    * Prefix text field
    * */
    @Test
    fun shouldInputValuesToPrefixTextField_whenValuesAreValid() {
        for (value in asciiRange) {
            val char = value.toChar().toString()
            prefixTextField.performTextInput(char)

            if (char.matches(Regex(PREFIX_REGEX_PATTERN))) {
                prefixTextField.assertTextContains(char.uppercase())
                prefixTextField.performTextClearance()
            } else {
                prefixTextField.assertTextContains("")
            }
        }
    }

    @Test
    fun shouldInputValuesToPrefixTextField_whenWithinAllowedRange() {
        var prefixString = ""
        while(prefixString.length < prefixCharLimit) {
            val char = asciiRange.random().toChar().toString()
            if (char.matches(Regex(PREFIX_REGEX_PATTERN))) {
                    prefixString += char
            }
        }

        prefixTextField.performTextInput(prefixString)
        prefixTextField.assertTextContains(prefixString.uppercase())
    }

    @Test
    fun shouldNotInputValuesToPrefixTextField_whenNotWithinAllowedRange() {
        var prefixString = ""
        while(prefixString.length < prefixCharLimit + 1) {
            val char = asciiRange.random().toChar().toString()
            if (char.matches(Regex(PREFIX_REGEX_PATTERN))) {
                prefixString += char.uppercase()
            }
        }

        Log.d(LOG_TAG, "PlateSearchScreenTest: prefixString = $prefixString")
        prefixTextField.performTextInput(prefixString)
        prefixTextField.assertTextContains("")
    }

    /*
    * Number text field
    * */
    @Test
    fun shouldInputValuesToNumberTextField_whenValuesAreValid() {
        for (value in asciiRange) {
            val char = value.toChar().toString()
            numberTextField.performTextInput(char)

            if (char.matches(Regex(NUMBER_REGEX_PATTERN))) {
                numberTextField.assertTextContains(char)
                numberTextField.performTextClearance()
            } else {
                numberTextField.assertTextContains("")
            }
        }
    }

    @Test
    fun shouldInputValuesToNumberTextField_whenWithinAllowedRange() {
        var numberString = ""
        while(numberString.length < numberCharLimit) {
            val char = asciiRange.random().toChar().toString()
            if (char.matches(Regex(NUMBER_REGEX_PATTERN))) {
                numberString += char
            }
        }

        numberTextField.performTextInput(numberString)
        numberTextField.assertTextContains(numberString)
    }

    @Test
    fun shouldNotInputValuesToNumberTextField_whenNotWithinAllowedRange() {
        var numberString = ""
        while(numberString.length < numberCharLimit + 1) {
            val char = asciiRange.random().toChar().toString()
            if (char.matches(Regex(NUMBER_REGEX_PATTERN))) {
                numberString += char
            }
        }

        Log.d(LOG_TAG, "PlateSearchScreenTest: numberString = $numberString")
        numberTextField.performTextInput(numberString)
        numberTextField.assertTextContains("")
    }

}