# Xendit Components Android

A drop-in payment UI SDK for Android that lets you accept payments through Xendit with minimal integration effort. Present a fully featured payment sheet in just a few lines of code.

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/xendit/xendit-components-android)
[![Version](https://img.shields.io/badge/version-1.2.2-blue)](https://github.com/xendit/xendit-components-android/releases)
[![License](https://img.shields.io/badge/license-Apache%202.0-lightgrey)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%2026%2B-green)](https://developer.android.com/)

> 💡 **Migrating from `xendit-sdk-android`?** Check out our [Migration Guide](MIGRATION_GUIDE.md) for step-by-step instructions.

## Features

- **Pre-built payment sheet** — Full payment UI with channel selection, form validation, and error handling out of the box.
- **3DS & redirect handling** — Built-in WebView for 3D Secure challenges and redirect-based payment flows.
- **Customizable appearance** — Configure colors, fonts, and corner radius to match your brand.
- **Jetpack Compose native** — Built entirely with Material 3 and modern Compose components.
- **Secure by default** — Client-side encryption of sensitive card data using ECDH + AES-GCM.

## Requirements
| Requirement | Minimum              |
|-------------|----------------------|
| Android     | API 26 (Android 8.0) |
| Compile SDK | 37                   |
| Kotlin      | 2.4+                 |
| Compose Bom | 2026.05.01+          |

## Getting Started

### Quick Start (Kotlin)

```kotlin
import co.xendit.components.XenditComponents
import co.xendit.components.data.model.XenditPaymentResult

// 1. Initialize once at app startup (Application class or main Activity)
XenditComponents.initialize(appearance = XenditAppearance())

// 2. Present the payment sheet from a ComponentActivity
XenditComponents.present(
    activity = this,
    componentsSdkKey = "<your_components_sdk_key>"
) { result ->
    when (result) {
        is XenditPaymentResult.Success ->
            println("Payment succeeded: ${result.paymentRequestId}")
        is XenditPaymentResult.Failed ->
            println("Payment failed: ${result.error.message}")
        is XenditPaymentResult.Canceled ->
            println("Session canceled")
        is XenditPaymentResult.Expired ->
            println("Session expired")
        is XenditPaymentResult.Dismissed ->
            println("User dismissed")
    }
}
```

### Quick Start (Java)

```java
import co.xendit.components.XenditComponents;
import co.xendit.components.data.model.XenditPaymentResult;
import co.xendit.components.ui.style.XenditAppearance;

// 1. Initialize once at app startup
XenditComponents.INSTANCE.initialize(new XenditAppearance(), null);

// 2. Present the payment sheet from a ComponentActivity
XenditComponents.INSTANCE.present(
    this,
    "<your_components_sdk_key>",
    null,
    result -> {
        if (result instanceof XenditPaymentResult.Success) {
            XenditPaymentResult.Success success = (XenditPaymentResult.Success) result;
            System.out.println("Paid: " + success.getPaymentRequestId());
        } else if (result instanceof XenditPaymentResult.Failed) {
            XenditPaymentResult.Failed failed = (XenditPaymentResult.Failed) result;
            System.out.println("Error: " + failed.getError().getMessage());
        }
    }
);
```

The `components_sdk_key` is obtained from the [Create Session](https://developers.xendit.co) API response on your backend.

## Payment Method Preference

Merchants can control which payment methods are shown in the payment sheet by passing `merchantPreferredPaymentMethod`.

The SDK currently supports filtering to these payment methods only:

- `XenditComponentsPaymentType.CARDS`
- `XenditComponentsPaymentType.EWALLET`
- `XenditComponentsPaymentType.QR_CODE`

### Set It Globally During Initialization

Use this when you want the same payment method preference for every `present()` call.

```kotlin
import co.xendit.components.XenditComponents
import co.xendit.components.XenditComponentsPaymentType
import co.xendit.components.ui.style.XenditAppearance

XenditComponents.initialize(
    appearance = XenditAppearance(),
    merchantPreferredPaymentMethod = listOf(
        XenditComponentsPaymentType.CARDS,
        XenditComponentsPaymentType.EWALLET
    )
)
```

### Override It Per Session

Use this when the payment methods should vary depending on the checkout flow.

```kotlin
import co.xendit.components.XenditComponents
import co.xendit.components.XenditComponentsPaymentType

XenditComponents.present(
    activity = this,
    componentsSdkKey = "<your_components_sdk_key>",
    merchantPreferredPaymentMethod = listOf(
        XenditComponentsPaymentType.QR_CODE
    )
) { result ->
    // handle result
}
```

### Java

```java
import java.util.Arrays;
import java.util.Collections;
import co.xendit.components.XenditComponents;
import co.xendit.components.XenditComponentsPaymentType;
import co.xendit.components.ui.style.XenditAppearance;

XenditComponents.INSTANCE.initialize(
    new XenditAppearance(),
    Arrays.asList(
        XenditComponentsPaymentType.CARDS,
        XenditComponentsPaymentType.EWALLET
    )
);

XenditComponents.INSTANCE.present(
    this,
    "<your_components_sdk_key>",
    Collections.singletonList(XenditComponentsPaymentType.QR_CODE),
    result -> {
        // handle result
    }
);
```

> **Note:** Although `XenditComponentsPaymentType` includes other enum values, only `CARDS`, `EWALLET`, and `QR_CODE` are currently supported for merchant preference in the Android SDK. Unsupported values are ignored, and if no supported preference remains, the SDK falls back to the supported payment methods available in the session.

## Appearance Customization

Use `XenditAppearance` to match the payment sheet to your app's brand. All color properties have built-in defaults — pass only the values you want to override.

### Property Reference

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `fontFamily` | `FontFamily?` | `null` | Custom font family for all text. Falls back to the bundled Inter font. |
| `colorPrimary` | `Color` | `#0052FF` | Primary CTA color — pay button, selected states, checkmarks. |
| `colorText` | `Color` | `#1A1C1E` | Main body text color. |
| `colorTextSecondary` | `Color` | `#6B7280` | Subtitle and caption text color. |
| `colorTextPlaceholder` | `Color` | `#9CA3AF` | Placeholder text inside input fields. |
| `colorDisabled` | `Color` | `#E5E7EB` | Background of disabled buttons and controls. |
| `colorDanger` | `Color` | `#BA1A1A` | Error messages and invalid field borders. |
| `colorBorder` | `Color` | `#E6E6E6` | Input field outlines and dividers. |
| `colorBackground` | `Color` | `#FFFFFF` | Sheet and page background. |
| `qrForegroundColor` | `Color` | `#000000` | Tint of the container shown around the QR code image. |
| `qrBackgroundColor` | `Color` | `#FFFFFF` | Background of the container box behind the QR code image. |
| `borderRadius` | `Dp` | `8.dp` | Corner radius for buttons, fields, and cards. |
| `isLightStatusBars` | `Boolean?`| `null` | Forces status bar icon color. `true` for dark icons, `false` for white icons. Defaults to automatic adjustment based on `colorBackground` luminance. |

### Kotlin

```kotlin
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import co.xendit.components.XenditComponents
import co.xendit.components.ui.style.XenditAppearance

val customFont = FontFamily(
    Font(R.font.your_font_regular,   FontWeight.Normal,   FontStyle.Normal),
    Font(R.font.your_font_medium,    FontWeight.Medium,   FontStyle.Normal),
    Font(R.font.your_font_semibold,  FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.your_font_bold,      FontWeight.Bold,     FontStyle.Normal)
)

val appearance = XenditAppearance(
    fontFamily            = customFont,
    colorPrimary          = Color(0xFF0052FF),
    colorText             = Color(0xFF1A1C1E),
    colorTextSecondary    = Color(0xFF6B7280),
    colorTextPlaceholder  = Color(0xFF9CA3AF),
    colorDisabled         = Color(0xFFE5E7EB),
    colorDanger           = Color(0xFFBA1A1A),
    colorBorder           = Color(0xFFE6E6E6),
    colorBackground       = Color(0xFFFFFFFF),
    qrForegroundColor     = Color(0xFF000000),
    qrBackgroundColor     = Color(0xFFFFFFFF),
    borderRadius          = 12.dp,
    isLightStatusBars     = true
)

XenditComponents.initialize(appearance = appearance)

XenditComponents.present(
    activity = this,
    componentsSdkKey = "<your_components_sdk_key>"
) { result ->
    when (result) {
        is XenditPaymentResult.Success ->
            println("Payment succeeded: ${result.paymentRequestId}")
        is XenditPaymentResult.Failed ->
            println("Payment failed: ${result.error.message}")
        is XenditPaymentResult.Canceled ->
            println("Session canceled")
        is XenditPaymentResult.Expired ->
            println("Session expired")
        is XenditPaymentResult.Dismissed ->
            println("User dismissed")
    }
}
```

### Java

```java
import androidx.compose.ui.graphics.Color;
import androidx.compose.ui.text.font.Font;
import androidx.compose.ui.text.font.FontFamily;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.unit.DpKt;
import co.xendit.components.XenditComponents;
import co.xendit.components.data.model.XenditPaymentResult;
import co.xendit.components.ui.style.XenditAppearance;
import java.util.Arrays;

FontFamily customFont = new FontFamily(Arrays.asList(
    new Font(R.font.your_font_regular,  FontWeight.Companion.getNormal(),   FontStyle.Companion.getNormal()),
    new Font(R.font.your_font_semibold, FontWeight.Companion.getSemiBold(), FontStyle.Companion.getNormal()),
    new Font(R.font.your_font_bold,     FontWeight.Companion.getBold(),     FontStyle.Companion.getNormal())
));

XenditAppearance appearance = new XenditAppearance(
    customFont,
    new Color(0xFF0052FFL),  // colorPrimary
    new Color(0xFF1A1C1EL),  // colorText
    new Color(0xFF6B7280L),  // colorTextSecondary
    new Color(0xFF9CA3AFL),  // colorTextPlaceholder
    new Color(0xFFE5E7EBL),  // colorDisabled
    new Color(0xFFBA1A1AL),  // colorDanger
    new Color(0xFFE6E6E6L),  // colorBorder
    new Color(0xFFFFFFFFL),  // colorBackground
    new Color(0xFF000000L),  // qrForegroundColor
    new Color(0xFFFFFFFFL),  // qrBackgroundColor
    DpKt.getDp(12),          // borderRadius
    true                     // isLightStatusBars
);

XenditComponents.INSTANCE.initialize(appearance);

XenditComponents.INSTANCE.present(
    this,
    "<your_components_sdk_key>",
    null,
    result -> {
        if (result instanceof XenditPaymentResult.Success) {
            String id = ((XenditPaymentResult.Success) result).getPaymentRequestId();
            System.out.println("Paid: " + id);
        } else if (result instanceof XenditPaymentResult.Failed) {
            String msg = ((XenditPaymentResult.Failed) result).getError().getMessage();
            System.out.println("Error: " + msg);
        }
    }
);
```

> **Tip:** `XenditComponents.initialize()` can be called multiple times. Each call replaces the active appearance for all subsequent `present()` calls, allowing runtime theme switching without restarting the session.

## Dismissing Manually

```kotlin
XenditComponents.dismiss()
```

Calling `dismiss()` triggers the `onPaymentResult` callback with `XenditPaymentResult.Canceled` and removes the payment UI from the activity.

## Performance & Reliability Telemetry

To ensure the payment UI is reliable and to identify regressions, fraud patterns, and performance issues quickly, the SDK sends a small, best-effort stream of in-session lifecycle events to Xendit's telemetry endpoint. This telemetry is **session-scoped and non-transactional** — losing events on process death is acceptable, and no data is persisted to disk on the device.

### What is collected (and what is NOT)

**Collected:**
- Session identifiers issued by Xendit: `payment_session_id` and `session_auth_id` (not user emails, names, or phone numbers)
- UI lifecycle event types: `CHECKOUT_LOADED`, `CHECKOUT_CHANNEL`, `CHECKOUT_CHANNEL_FORM_INPUT`, `CHECKOUT_ATTEMPT_BEGIN`, `CHECKOUT_ATTEMPT`, `CHECKOUT_ACTION_BEGIN`, `CHECKOUT_ACTION_CLOSE`, `CHECKOUT_DIGITAL_WALLET_BEGIN/CLOSE`, `CHECKOUT_ACTION_COPY_TEXT`, `CHECKOUT_END`, `CHECKOUT_PENDING`, `CHECKOUT_ABANDON`
- Static, non-PII diagnostic context: Xendit payment channel codes (e.g. `BCA`, `SHOPEEPAY`), payment-method group names (`cards`, `ewallet`, `qr_code`), **static field labels** (`card_number`, `expiry`, `cvv`, `phone`, `name` — never the typed value), error codes, validation keys, and status keys
- Event ordering metadata (`event_id`, `parent_event_id`) and wall-clock timestamps
- Xendit Payment Request ID and Payment Token ID once set

**Never collected in telemetry:**
- ❌ Card PAN (full or masked), CVV/CVC, or expiry digits typed by the user
- ❌ Any value the user types into a form field — only the static `field_name` label is tracked
- ❌ Billing name, email, phone number, or address text
- ❌ Android Advertising ID (AAID/GAID), precise location, or device fingerprint
- ❌ Any cross-app or cross-site tracking identifiers

### Delivery behaviour

| Trigger | When |
|---------|------|
| Time-based flush | Every 5 seconds if there are pending events |
| Batch-based flush | When the in-memory queue reaches 25 events |
| Background / screen exit | Activity `onStop` / `onDestroy` and `ProcessLifecycleOwner` `onStop` |
| Memory pressure | `TRIM_MEMORY_BACKGROUND` discards the queue (no on-disk persistence) |

### Debug vs release

| | Debug Build | Release Build |
|-|-------------|---------------|
| **Logcat prints of telemetry payloads** | ✅ Enabled by default (via `BuildConfig.DEBUG`) | ❌ Completely silenced |
| **OkHttp Profiler interceptor** | ✅ Attached for Android Studio Profiler visibility | ❌ Not attached |
| **Network transmission (actual flush)** | ✅ Sends data (test environment) | ✅ Sends data (production telemetry — required for reliability) |

> **Privacy & compliance note.** Full Play-Data-Safety-ready disclosure, exact field-by-field schema, and source-code cross-references for every telemetry value are documented in [PRIVACY.md — Performance Telemetry](PRIVACY.md#performance-telemetry--what-exactly-is-sent). Use that section together with your legal counsel to complete your Google Play Data Safety form and your app's privacy policy.

## Installation

### Gradle (Kotlin DSL)

Add Maven Central to your `settings.gradle.kts` if not already present:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

Then add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("co.xendit:components:1.2.2")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'co.xendit:components:1.2.2'
}
```

### Google Pay Production Requirement

Before you offer Google Pay in production, you must register and approve your Android app with Google Pay using the exact production package name that will be distributed to end users.

If your app is not registered and approved for Google Pay production access, Google Pay can fail at runtime with error code `OR_BIBED_11`.

> **Important:** Complete the Google Pay production app registration and approval before going live. This requirement applies to production usage; test and sandbox flows may still work before production registration is finalized.

## Documentation & Examples

| Resource | Description |
|----------|-------------|
| [Migration Guide](MIGRATION_GUIDE.md) | Guide for migrating from legacy `xendit-sdk-android` to Components SDK. |
| [API Reference](https://developers.xendit.co) | Full API documentation for session creation and payment flows. |
| [Example App](example-app/) | Sample Jetpack Compose app demonstrating SDK integration. |

## Privacy

See [PRIVACY.md](PRIVACY.md) for a full breakdown of data collected, how it is used, whether it is linked to the end user's identity, and whether it is used for tracking — formatted for [Google Play Data Safety](https://support.google.com/googleplay/android-developer/answer/10787469).

## License

Xendit Components Android is available under the Apache License 2.0. See the [LICENSE](LICENSE) file for more info.
