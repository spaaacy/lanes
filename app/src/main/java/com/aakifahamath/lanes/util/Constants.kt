package com.aakifahamath.lanes.util

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

const val LOG_TAG = "PROJECT FACECHECK"
const val BASE_URL = "https://lanes-15470-default-rtdb.asia-southeast1.firebasedatabase.app/"
const val MONTH_IN_MS = 2592000000L // 30 days
const val TIMEOUT_IN_MS = 10000L // 10 seconds
const val ANONYMOUS_ID = "-1"
val DEFAULT_PADDING = 16.dp

/*
* Firebase paths
* */
const val PLATE_PATH = "plates"
const val USER_PATH = "users"
const val PREFIX_PATH = "prefix"
const val NUMBER_PATH = "number"
const val TIMESTAMP_PATH = "timestamp"
const val REPUTATION_PATH = "reputation"
const val UPVOTE_PATH = "upvoted_plates"
const val DOWNVOTE_PATH = "downvoted_plates"
const val OWNED_PATH = "owned_plates"

/*
* Exceptions
* */
const val INVALID_PLATE_KEY = "INVALID_PLATE_KEY"
const val NETWORK_FAILURE = "NETWORK_FAILURE"
const val TIMEOUT_FAILURE = "TIMEOUT_FAILURE"
const val USER_ALREADY_EXISTS = "USER_EXISTS_ERROR"
const val WEAK_PASSWORD = "WEAK_PASSWORD"
const val USER_NON_EXISTENT = "USER_NON_EXISTENT"
const val INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
const val ANONYMOUS_USER = "ANONYMOUS_USER"

/*
* License Plate Sizing
* */
val PLATE_FONT_LARGE = 48.sp
val PLATE_FONT_MEDIUM = 32.sp
val PLATE_FONT_SMALL = 16.sp
val PLATE_VERTICAL_LARGE = 24.dp
val PLATE_VERTICAL_MEDIUM = 16.dp
val PLATE_VERTICAL_SMALL = 8.dp
val PLATE_HORIZONTAL_LARGE = 16.dp
val PLATE_HORIZONTAL_MEDIUM = 12.dp
val PLATE_HORIZONTAL_SMALL = 6.dp

/*
* Regex patterns
* */
const val EMAIL_REGEX_PATTERN = "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
const val PASSWORD_REGEX_PATTERN = "[a-zA-Z0-9!@#$%^&*]{0,32}"
const val PREFIX_REGEX_PATTERN = "[a-zA-Z]{0,3}"
const val NUMBER_REGEX_PATTERN = "[0-9]{0,4}"
const val PREFIX_REJECT_REJECT_PATTERN = "[ioIO]+"

/*
* Testing
* */
const val LOCALHOST = "10.0.2.2"
const val AUTHENTICATION_PORT = 9099
const val TEST_URL = "http://10.0.2.2:9000/?ns=driverrep-9cb39"
const val AUTH_LOCAL_URL = "http://10.0.2.2:9099/"
const val ACCOUNT_DELETE = "emulator/v1/projects/driverrep-9cb39/accounts"
const val ACCOUNT_CREATE = "identitytoolkit.googleapis.com/v1/accounts:signUp?key=AIzaSyAFmKXPYqo6anFEWfWFJjvZYz9KcI_miCI"
const val EMAIL = "email"
const val PASSWORD = "password"
const val RETURN_SCORE_TOKEN = "returnSecureToken"

/*
* Test tags
* */
const val PREFIX_TEXT_FIELD = "PREFIX_TEXT_FIELD"