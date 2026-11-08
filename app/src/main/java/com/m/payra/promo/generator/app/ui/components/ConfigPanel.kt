package com.m.payra.promo.generator.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Pattern
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.payra.promo.generator.app.engine.CasingMode
import com.m.payra.promo.generator.app.engine.GeneratorConfig
import com.m.payra.promo.generator.app.engine.PromoGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigPanel(
    config: GeneratorConfig,
    onConfigChange: (GeneratorConfig) -> Unit,
    onGenerate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandPoolSection by remember { mutableStateOf(true) }
    var expandPatternSection by remember { mutableStateOf(false) }
    var expandSecuritySection by remember { mutableStateOf(false) }
    var expandMetadataSection by remember { mutableStateOf(false) }

    var activeHelpInfo by remember { mutableStateOf<OptionHelpInfo?>(null) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ⚡ Core Parameters (Top Level for Quick Access)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuantityInput(
                    value = config.quantity,
                    onValueChange = { onConfigChange(config.copy(quantity = it)) },
                    onResetClick = { onConfigChange(config.copy(quantity = 10)) },
                    onInfoClick = {
                        activeHelpInfo = OptionHelpInfo(
                            title = "Quantity (1-100,000)",
                            summary = "Controls total number of promo codes generated in a single batch.",
                            detailedDescription = "Controls the total volume of unique promo codes generated in a single batch process. The generator supports batch sizes anywhere from 1 single code up to 100,000 codes in a single run. High-volume batch generation utilizes optimized background Kotlin coroutines and memory-efficient data structures to build, deduplicate, and assemble codes rapidly without freezing the user interface. Deduplication logic ensures every single code in the generated batch is 100% unique.",
                            howToUse = "Tap one of the quick preset chips (10, 50, 100, 500, 1000, 5000, 10000) or type your desired total number directly into the numeric quantity input field.",
                            whenToUse = "Use small quantities (1-50) for testing, single VIP rewards, or custom store vouchers. Use large quantities (1,000-100,000) for mass marketing, affiliate campaigns, or nationwide retail promotions.",
                            whyToUse = "Provides instant scalability to generate massive coupon batches in seconds with guaranteed uniqueness across the entire dataset.",
                            conditionsToUse = "Enter a number between 1 and 100,000. For large batches (>10,000), ensure your device has sufficient memory when exporting to PDF or Excel formats.",
                            example = "Preset 100 -> Generates exactly 100 unique, non-duplicate promo codes."
                        )
                    }
                )

                if (!config.usePatternMask) {
                    M3Stepper(
                        label = "Code Length (4-64)",
                        value = config.length,
                        min = 4,
                        max = 64,
                        presets = listOf(8, 12, 16, 24),
                        onValueChange = { onConfigChange(config.copy(length = it)) },
                        onResetClick = { onConfigChange(config.copy(length = 8)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Code Length",
                                summary = "Sets character length of raw promo codes.",
                                detailedDescription = "Specifies how many characters long each raw generated code will be, configurable between 4 and 64 characters. Code length dictates the mathematical entropy and total combinatorial capacity of your campaign. For instance, an 8-character uppercase code provides over 208 billion unique combinations (26^8), whereas a 16-character code provides 26^16 combinations, rendering brute-force guessing computationally impossible.",
                                howToUse = "Adjust the stepper using the + and - buttons or pick a preset length chip (8, 12, 16, 24).",
                                whenToUse = "Use short lengths (4-8) for customer-facing codes meant to be typed manually. Use longer lengths (12-64) for digital API keys, software licenses, and secure authorization tokens.",
                                whyToUse = "Allows you to strike the perfect balance between human typing convenience and mathematical security.",
                                conditionsToUse = "Hidden when Pattern Template Mask is enabled, as pattern masks define their own structural length.",
                                example = "Length 8 -> 'A9X4B2KW'\nLength 16 -> 'K9X4B2W7P1M8V3L5'"
                            )
                        }
                    )
                }

                M3TextField(
                    label = "PRNG Seed (Optional)",
                    value = config.seed,
                    placeholder = "Leave blank for SecureRandom",
                    onValueChange = { onConfigChange(config.copy(seed = it)) },
                    supportingText = "Blank = Hardware SecureRandom. Seeded = Reproducible Audit Mode.",
                    onResetClick = { onConfigChange(config.copy(seed = "")) },
                    onInfoClick = {
                        activeHelpInfo = OptionHelpInfo(
                            title = "PRNG Seed (Optional)",
                            summary = "Seed key for reproducible pseudo-random code generation.",
                            detailedDescription = "Provides deterministic pseudo-random number generator (PRNG) capabilities. When left blank, the generator uses hardware-level SecureRandom for cryptographically strong, unpredictable randomness. When a specific seed string is provided, the mathematical algorithm produces the exact same deterministic list of promo codes every time under identical settings.",
                            howToUse = "Leave blank for live production codes. Enter any string or number into the seed box to enable deterministic audit mode.",
                            whenToUse = "Use during automated unit testing, software debugging, compliance auditing, or when re-creating an identical historical code batch.",
                            whyToUse = "Guarantees 100% reproducible results for QA teams and auditors across different devices and builds.",
                            conditionsToUse = "Always leave blank for live production marketing campaigns to ensure codes are cryptographically unpredictable.",
                            example = "Seed 'Audit2026' -> Always generates identical code list every run."
                        )
                    }
                )
            }

            // Section 1: CHARACTER POOL & EXCLUSIONS
            CollapsibleSectionHeader(
                title = "CHARACTER POOL & EXCLUSIONS",
                icon = Icons.Outlined.CheckCircle,
                isExpanded = expandPoolSection,
                onToggle = { expandPoolSection = !expandPoolSection }
            )

            AnimatedVisibility(visible = expandPoolSection) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Lowercase
                    OptionSwitchRow(
                        checked = config.includeLower,
                        onCheckedChange = { onConfigChange(config.copy(includeLower = it)) },
                        label = "Lowercase Letters",
                        preview = "a-z",
                        onResetClick = { onConfigChange(config.copy(includeLower = true)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Lowercase Letters (a-z)",
                                summary = "Includes lowercase English alphabet letters ('a' through 'z') in the code generation pool.",
                                detailedDescription = "Incorporate standard ASCII lowercase English letters ('a' through 'z', 26 total characters) into the random generation pool. Including lowercase characters expands pool size and increases entropy. Lowercase characters are ideal for web URLs, API headers, and software access tokens where backend servers support case sensitivity.",
                                howToUse = "Toggle the switch ON under 'CHARACTER POOL & EXCLUSIONS' -> 'Lowercase Letters'.",
                                whenToUse = "Use for digital coupon URLs, web API access tokens, software license keys, and case-sensitive online redemption portals.",
                                whyToUse = "Increases character pool entropy while enabling lowercase web URL formatting.",
                                conditionsToUse = "Enable if your backend redemption server supports case sensitivity. Disable for physical print flyers where customers type codes on touchscreen keypads.",
                                example = "Output: a9x4b2k, r7m1v8p, m3k8w2q"
                            )
                        }
                    )

                    // Uppercase
                    OptionSwitchRow(
                        checked = config.includeUpper,
                        onCheckedChange = { onConfigChange(config.copy(includeUpper = it)) },
                        label = "Uppercase Letters",
                        preview = "A-Z",
                        onResetClick = { onConfigChange(config.copy(includeUpper = true)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Uppercase Letters (A-Z)",
                                summary = "Includes uppercase English alphabet letters ('A' through 'Z') in the code generation pool.",
                                detailedDescription = "Adds capital English letters ('A' through 'Z', 26 characters) into the random pool. Uppercase letters are the global gold standard for retail coupons, gift cards, and paper vouchers due to high contrast, crisp legibility, and reduced visual ambiguity on both physical paper and digital screens.",
                                howToUse = "Toggle the switch ON under 'CHARACTER POOL & EXCLUSIONS' -> 'Uppercase Letters'.",
                                whenToUse = "Use for retail promo codes, store gift cards, print flyers, SMS marketing, and e-commerce discount vouchers.",
                                whyToUse = "Maximizes visual readability for human users while offering strong cryptographic entropy when combined with numbers.",
                                conditionsToUse = "Recommended as the primary default character set for almost all general retail and e-commerce campaigns.",
                                example = "Output: K9X4B2W, R7M1V8P, W3K8M2Q"
                            )
                        }
                    )

                    // Numbers
                    OptionSwitchRow(
                        checked = config.includeNumbers,
                        onCheckedChange = { onConfigChange(config.copy(includeNumbers = it)) },
                        label = "Numerical Digits",
                        preview = "0-9",
                        onResetClick = { onConfigChange(config.copy(includeNumbers = true)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Numbers (0-9)",
                                summary = "Includes numerical digits ('0' through '9') in the code generation pool.",
                                detailedDescription = "Includes Arabic numerals ('0' through '9', 10 total digits) into the random pool. Numbers are universally recognized across all languages and keypads. Adding digits to alphabetic pools significantly increases combinatorial entropy and is mandatory for PINs, store vouchers, and Luhn checksum verification.",
                                howToUse = "Toggle the switch ON under 'CHARACTER POOL & EXCLUSIONS' -> 'Numerical Digits'.",
                                whenToUse = "Use for e-commerce discount codes, store vouchers, SMS verification PINs, and single-use gift cards.",
                                whyToUse = "Universal user familiarity across all mobile keypads and fast typing speed for end customers.",
                                conditionsToUse = "Recommended for virtually all promo code generation tasks.",
                                example = "Output: 849201, 739104, X92K41"
                            )
                        }
                    )

                    // Symbols
                    OptionSwitchRow(
                        checked = config.includeSymbols,
                        onCheckedChange = { onConfigChange(config.copy(includeSymbols = it)) },
                        label = "Special Symbols",
                        preview = "!@#...",
                        onResetClick = { onConfigChange(config.copy(includeSymbols = false, customSymbols = "'\"!?.,:;\$%&@~#()<>{}[]_*-+^=/|\\")) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Symbols (!@#...)",
                                summary = "Includes special punctuation symbols and custom characters in the code pool.",
                                detailedDescription = "Adds special punctuation characters (!, @, #, $, %, ^, &, *, -, _) into the random selection pool. Incorporating symbols yields high-security access codes, administrative tokens, and encrypted discount vouchers that offer maximum resistance against automated guessing.",
                                howToUse = "Toggle the switch ON under 'CHARACTER POOL & EXCLUSIONS' -> 'Special Symbols'. Optionally customize allowed symbols in the field below.",
                                whenToUse = "Use for admin access passcodes, encrypted API keys, VIP access tokens, and high-security credentials.",
                                whyToUse = "Provides maximum cryptographic security against automated brute-force attacks.",
                                conditionsToUse = "Best for copy-paste digital workflows. Avoid for physical print flyers where users manually type codes on touchscreen devices.",
                                example = "Output: K9#4B!2W, R7$1V%8P"
                            )
                        }
                    )
                    AnimatedVisibility(visible = config.includeSymbols) {
                        M3TextField(
                            label = "Custom Symbols Pool (No Letters or Digits)",
                            value = config.customSymbols,
                            onValueChange = { input ->
                                val symbolsOnly = PromoGenerator.filterSymbolsOnly(input)
                                onConfigChange(config.copy(customSymbols = symbolsOnly))
                            },
                            onResetClick = { onConfigChange(config.copy(customSymbols = "'\"!?.,:;\$%&@~#()<>{}[]_*-+^=/|\\")) },
                            onInfoClick = {
                                activeHelpInfo = OptionHelpInfo(
                                    title = "Custom Symbols Pool",
                                    summary = "Defines the exact set of special characters allowed when symbols are enabled.",
                                    detailedDescription = "Restricts symbol generation strictly to a user-defined list of punctuation characters. Letters and numbers are automatically filtered out. This is crucial for avoiding characters (like quotes, slashes, or ampersands) that might disrupt database queries, JSON payloads, or URL parameters.",
                                    howToUse = "Enable 'Special Symbols', then type allowed punctuation marks into the 'Custom Symbols Pool' field.",
                                    whenToUse = "Use when selecting safe symbols (e.g. '-', '_', '!') while avoiding characters like '<' or '>' in web backend environments.",
                                    whyToUse = "Prevents code parsing errors in backend databases and APIs while retaining symbol security benefits.",
                                    conditionsToUse = "Enable whenever special symbols are active and your database imposes strict character encoding rules.",
                                    example = "Custom input '!@#' -> Output: A!9#B@2, X#7@Y!1"
                                )
                            }
                        )
                    }

                    // Emojis
                    OptionSwitchRow(
                        checked = config.includeEmoji,
                        onCheckedChange = { onConfigChange(config.copy(includeEmoji = it)) },
                        label = "Emoji Icons",
                        preview = "🎉🔥🎁",
                        onResetClick = { onConfigChange(config.copy(includeEmoji = false, customEmoji = "🎉🔥🎁⭐💎🚀💥⚡🏆🎯👑❤️🍀💰✨💯")) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Emoji Icons (🎉, 🔥, 🎁...)",
                                summary = "Includes colorful promotional emojis in the code generation pool.",
                                detailedDescription = "Injects multi-byte Unicode emoji characters (such as 🎉, 🔥, 🎁, 💎, 🚀) into the code pool. Emoji-infused promo codes are a high-converting marketing strategy for social media (Instagram, TikTok), messaging apps, and push notifications, making vouchers visually striking and memorable.",
                                howToUse = "Toggle the switch ON under 'CHARACTER POOL & EXCLUSIONS' -> 'Emoji Icons'.",
                                whenToUse = "Use for social media marketing campaigns, push notification deals, holiday sales, and youth-focused brand promotions.",
                                whyToUse = "Dramatically boosts click-through rates, social media engagement, and brand recall.",
                                conditionsToUse = "Use for modern digital messaging and app campaigns. Ensure redemption portals support UTF-8 Unicode emojis.",
                                example = "Output: PROMO-🎉🔥99, GIFT-🎁💎20"
                            )
                        }
                    )
                    AnimatedVisibility(visible = config.includeEmoji) {
                        M3TextField(
                            label = "Custom Emojis Pool",
                            value = config.customEmoji,
                            onValueChange = { input ->
                                val emojiOnly = PromoGenerator.filterEmojiOnly(input)
                                onConfigChange(config.copy(customEmoji = emojiOnly))
                            },
                            onResetClick = { onConfigChange(config.copy(customEmoji = "🎉🔥🎁⭐💎🚀💥⚡🏆🎯👑❤️🍀💰✨💯")) },
                            onInfoClick = {
                                activeHelpInfo = OptionHelpInfo(
                                    title = "Custom Emojis Pool",
                                    summary = "Defines the exact list of emojis allowed during code generation.",
                                    detailedDescription = "Allows you to paste a specific selection of brand-approved emojis into the generator. This ensures generated promo codes feature emojis that align perfectly with seasonal themes (e.g. 🎃 for Halloween, 🎄 for Christmas, 🎆 for New Year).",
                                    howToUse = "Enable 'Emoji Icons', then paste or type your chosen emojis into the 'Custom Emojis Pool' field.",
                                    whenToUse = "Use during holiday sales, festive events, product launches, or seasonal marketing drives.",
                                    whyToUse = "Aligns visual promo codes directly with campaign themes and visual branding.",
                                    conditionsToUse = "Enable whenever you want to restrict emoji generation to campaign-specific icons.",
                                    example = "Custom input '🚀⚡' -> Output: FLASH-🚀⚡99, POWER-⚡🚀50"
                                )
                            }
                        )
                    }

                    // Whitelist Character
                    OptionSwitchRow(
                        checked = config.useCustomWhitelist,
                        onCheckedChange = { onConfigChange(config.copy(useCustomWhitelist = it)) },
                        label = "Whitelist Character",
                        onResetClick = { onConfigChange(config.copy(useCustomWhitelist = false, customWhitelist = "")) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Whitelist Character",
                                summary = "Restricts code generation exclusively to specified whitelist characters.",
                                detailedDescription = "Acts as a strict master override that disables standard character category toggles and restricts the code engine to draw characters exclusively from your custom Whitelist string. Characters in the Whitelist cannot be written to the Blacklist.",
                                howToUse = "Toggle 'Whitelist Character' ON, then enter allowed characters in the 'Whitelist Characters Only' box.",
                                whenToUse = "Use when creating codes restricted to hexadecimal (0-9, A-F), Octal (0-7), or custom branded letter sets.",
                                whyToUse = "Guarantees 100% strict adherence to specialized character sets required by legacy hardware or specific encryption algorithms.",
                                conditionsToUse = "Use when strict character set boundaries are required. Disable when standard mixed alphanumeric generation is desired.",
                                example = "Whitelist '0123456789ABCDEF' -> Output: 4F8A2D, B91E7C"
                            )
                        }
                    )
                    AnimatedVisibility(visible = config.useCustomWhitelist) {
                        M3TextField(
                            label = "Whitelist Characters Only",
                            value = config.customWhitelist,
                            placeholder = "e.g. ABCDEF0123456789",
                            onValueChange = { input ->
                                val newWhitelist = input
                                val newBlacklist = config.blacklistChars.filter { it !in newWhitelist }
                                onConfigChange(config.copy(customWhitelist = newWhitelist, blacklistChars = newBlacklist))
                            },
                            onResetClick = { onConfigChange(config.copy(customWhitelist = "")) },
                            onInfoClick = {
                                activeHelpInfo = OptionHelpInfo(
                                    title = "Whitelist Characters Only",
                                    summary = "The explicit string of allowed characters used to construct promo codes.",
                                    detailedDescription = "This text field contains the exact string of characters from which all code positions will be populated. The generator de-duplicates input characters and treats each unique character with equal selection probability.",
                                    howToUse = "Type or paste the complete set of allowed characters into this text box.",
                                    whenToUse = "Use whenever the Whitelist Character toggle is active.",
                                    whyToUse = "Provides absolute precision over the exact pool of characters used in batch generation.",
                                    conditionsToUse = "Must contain at least 2 unique characters to allow random variation.",
                                    example = "Input 'ABC123' -> Output: A1B2C3, 3C2B1A, 2A3B1C"
                                )
                            }
                        )
                    }

                    // Blacklist Character
                    OptionSwitchRow(
                        checked = config.useBlacklist,
                        onCheckedChange = { onConfigChange(config.copy(useBlacklist = it)) },
                        label = "Blacklist Character",
                        onResetClick = { onConfigChange(config.copy(useBlacklist = false, blacklistChars = "")) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Blacklist Character",
                                summary = "Specifies characters to strictly exclude from code generation.",
                                detailedDescription = "Enables a negative filter that strips specified characters out of the active character pool before random selection. Whitelisted characters are automatically protected from being blacklisted to prevent logical conflicts.",
                                howToUse = "Toggle 'Blacklist Character' ON, then type excluded characters into the 'Blacklist Characters' field.",
                                whenToUse = "Use to remove unwanted letters, specific symbols, or characters that cause printing flaws on physical gift cards.",
                                whyToUse = "Prevents problematic characters from appearing without disabling entire character categories.",
                                conditionsToUse = "Use when fine-tuning character pools for physical print vouchers or customer support ease.",
                                example = "Blacklist 'F, U, C, K' -> Ensures no crude words containing these letters are generated."
                            )
                        }
                    )
                    AnimatedVisibility(visible = config.useBlacklist) {
                        M3TextField(
                            label = "Blacklist Characters (Omit from Pool)",
                            value = config.blacklistChars,
                            placeholder = "e.g. y, z",
                            onValueChange = { input ->
                                val whitelistSet = config.customWhitelist.toSet()
                                val newBlacklist = input.filter { it !in whitelistSet }
                                onConfigChange(config.copy(blacklistChars = newBlacklist))
                            },
                            onResetClick = { onConfigChange(config.copy(blacklistChars = "")) },
                            onInfoClick = {
                                activeHelpInfo = OptionHelpInfo(
                                    title = "Blacklist Characters List",
                                    summary = "Exact characters to omit from all generated promo codes.",
                                    detailedDescription = "Type individual characters or words into this text box to strip them out of the generation pool. Case matching is handled automatically to ensure complete exclusion.",
                                    howToUse = "Type the exact characters you wish to ban into this text input box.",
                                    whenToUse = "Use alongside active Blacklist mode.",
                                    whyToUse = "Ensures absolute exclusion of specific high-risk characters.",
                                    conditionsToUse = "Avoid blacklisting all available characters in a pool, as that leaves no characters for code generation.",
                                    example = "Input 'XYZ' -> Ensures no generated codes contain 'X', 'Y', or 'Z'."
                                )
                            }
                        )
                    }

                    // Avoid Ambiguous
                    OptionSwitchRow(
                        checked = config.avoidAmbiguous,
                        onCheckedChange = { onConfigChange(config.copy(avoidAmbiguous = it)) },
                        label = "Avoid ambiguous (i, l, 1, I, o, 0, O)",
                        onResetClick = { onConfigChange(config.copy(avoidAmbiguous = false)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Avoid ambiguous (i, l, 1, I, o, 0, O)",
                                summary = "Excludes visually confusing characters that look similar.",
                                detailedDescription = "Automatically purges visually ambiguous characters—lowercase 'i', lowercase 'l', digit '1', uppercase 'I', lowercase 'o', digit '0', and uppercase 'O'—from the active pool. In many fonts and prints, '1', 'l', and 'I' look identical, causing customer redemption errors and support tickets.",
                                howToUse = "Toggle the switch ON under 'CHARACTER POOL & EXCLUSIONS' -> 'Avoid ambiguous'.",
                                whenToUse = "Essential for printed flyers, SMS voucher codes, paper coupons, and offline event tickets.",
                                whyToUse = "Dramatically reduces customer redemption friction and support costs by eliminating mistyped codes.",
                                conditionsToUse = "Highly recommended for all customer-facing promotional campaigns.",
                                example = "Excluded: i, l, 1, I, o, 0, O -> Clean output: K9X4B2W, R7M8V2P"
                            )
                        }
                    )
                }
            }

            // Section 2: PATTERN & FORMATTING
            CollapsibleSectionHeader(
                title = "PATTERN & FORMATTING",
                icon = Icons.Outlined.Pattern,
                isExpanded = expandPatternSection,
                onToggle = { expandPatternSection = !expandPatternSection }
            )

            AnimatedVisibility(visible = expandPatternSection) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            M3TextField(
                                label = "Prefix",
                                value = config.prefix,
                                placeholder = "SUMMER-",
                                onValueChange = { onConfigChange(config.copy(prefix = it)) },
                                onResetClick = { onConfigChange(config.copy(prefix = "")) },
                                onInfoClick = {
                                    activeHelpInfo = OptionHelpInfo(
                                        title = "Code Prefix",
                                        summary = "Attaches fixed static text to the start of every generated promo code.",
                                        detailedDescription = "Prepends a static text string (e.g., 'SUMMER-', 'VIP-', 'WELCOME-') to the front of every code in the batch. Prefixes simplify campaign tracking, channel attribution, and database index querying.",
                                        howToUse = "Enter your desired prefix string into the 'Prefix' text field in 'PATTERN & FORMATTING'.",
                                        whenToUse = "Use for seasonal sales, affiliate partner tracking, and multi-channel marketing campaigns.",
                                        whyToUse = "Provides instant visual brand recognition and simplifies backend campaign auditing.",
                                        conditionsToUse = "Use whenever codes belong to a specific named marketing campaign.",
                                        example = "Prefix 'SAVE20-' -> Output: SAVE20-X9K2M, SAVE20-P4W8L"
                                    )
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            M3TextField(
                                label = "Suffix",
                                value = config.suffix,
                                placeholder = "-2026",
                                onValueChange = { onConfigChange(config.copy(suffix = it)) },
                                onResetClick = { onConfigChange(config.copy(suffix = "")) },
                                onInfoClick = {
                                    activeHelpInfo = OptionHelpInfo(
                                        title = "Code Suffix",
                                        summary = "Attaches fixed static text to the end of every generated promo code.",
                                        detailedDescription = "Appends a fixed static string (e.g., '-2026', '-OFF', '-VIP') to the tail end of every code. Suffixes help denote expiration years, discount values, or membership tiers.",
                                        howToUse = "Enter your chosen suffix string into the 'Suffix' field in 'PATTERN & FORMATTING'.",
                                        whenToUse = "Use to indicate year of issue, discount percentage, or store location.",
                                        whyToUse = "Clear visual indicators for cashiers and customers regarding voucher terms.",
                                        conditionsToUse = "Useful when discount terms need to be visible directly on the code.",
                                        example = "Suffix '-2026' -> Output: X9K2M-2026, P4W8L-2026"
                                    )
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1.5f)) {
                            M3Stepper(
                                label = "Chunk Size (0=Off)",
                                value = config.chunkSize,
                                min = 0,
                                max = 16,
                                presets = listOf(0, 4, 5, 8),
                                onValueChange = { onConfigChange(config.copy(chunkSize = it)) },
                                onResetClick = { onConfigChange(config.copy(chunkSize = 0)) },
                                onInfoClick = {
                                    activeHelpInfo = OptionHelpInfo(
                                        title = "Chunk Size",
                                        summary = "Splits long promo codes into smaller grouped blocks.",
                                        detailedDescription = "Divides long code strings into equal-length character blocks (e.g., groups of 4: 'XXXX-XXXX-XXXX'). Chunking greatly improves visual readability and reduces typing errors.",
                                        howToUse = "Use the stepper or quick preset buttons (0, 4, 5, 8) to set chunk length.",
                                        whenToUse = "Essential for codes longer than 8 characters, especially on mobile or printed media.",
                                        whyToUse = "Enhances cognitive readability and lowers user typing fatigue.",
                                        conditionsToUse = "Set to 0 to disable chunking for compact short codes.",
                                        example = "Chunk size 4 -> Output: ABCD-EFGH-1234"
                                    )
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            M3TextField(
                                label = "Delimiter",
                                value = config.delimiter,
                                onValueChange = { onConfigChange(config.copy(delimiter = it)) },
                                onResetClick = { onConfigChange(config.copy(delimiter = "-")) },
                                onInfoClick = {
                                    activeHelpInfo = OptionHelpInfo(
                                        title = "Chunk Delimiter",
                                        summary = "Separator symbol inserted between code chunks.",
                                        detailedDescription = "Defines the character inserted between grouped chunks. Default is hyphen ('-'), but space, slash ('/'), or dot ('.') are also supported.",
                                        howToUse = "Type your preferred delimiter symbol into the 'Delimiter' text box.",
                                        whenToUse = "Use to match UI design themes or legacy system formatting requirements.",
                                        whyToUse = "Matches corporate style guides and improves visual contrast.",
                                        conditionsToUse = "Active only when Chunk Size is greater than 0.",
                                        example = "Delimiter '/' -> Output: ABCD/EFGH/1234"
                                    )
                                }
                            )
                        }
                    }

                    OptionSwitchRow(
                        checked = config.usePatternMask,
                        onCheckedChange = { onConfigChange(config.copy(usePatternMask = it)) },
                        label = "Use Pattern Template Mask",
                        onResetClick = { onConfigChange(config.copy(usePatternMask = false, patternMask = "PROMO-###-@@@")) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Pattern Template Mask",
                                summary = "Generates codes adhering to a fixed structural pattern.",
                                detailedDescription = "Enables template masking where special tokens ('#' for digits, '@' for uppercase, '$' for lowercase, '*' for symbols) are replaced with random characters while literal text remains fixed.",
                                howToUse = "Toggle 'Use Pattern Template Mask' ON, then define your template string.",
                                whenToUse = "Use for complex serial numbers, license keys, and structured voucher formats.",
                                whyToUse = "Offers complete architectural control over code structure.",
                                conditionsToUse = "Overrides standard random length generation while active.",
                                example = "Mask 'DEAL-###-@@@' -> Output: DEAL-482-XKP"
                            )
                        }
                    )

                    AnimatedVisibility(visible = config.usePatternMask) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            M3TextField(
                                label = "Pattern Mask",
                                value = config.patternMask,
                                placeholder = "PROMO-###-@@@",
                                onValueChange = { onConfigChange(config.copy(patternMask = it)) },
                                supportingText = "Mask Symbols: # (Digit), @ (Upper), $ (Lower), * (Symbol)",
                                onResetClick = { onConfigChange(config.copy(patternMask = "PROMO-###-@@@")) },
                                onInfoClick = {
                                    activeHelpInfo = OptionHelpInfo(
                                        title = "Pattern Mask Definition",
                                        summary = "Structural pattern string defining character types at exact positions.",
                                        detailedDescription = "Defines the exact structural pattern layout for generated codes. Built-in mask tokens include: '#' (replaced with random digit 0-9), '@' (replaced with uppercase letter A-Z), '$' (replaced with lowercase letter a-z), and '*' (replaced with special symbol). Any other character entered into the pattern string (such as 'SAVE-###-@@@') remains fixed as literal text.",
                                        howToUse = "Type your pattern string into the 'Pattern Mask' input field.",
                                        whenToUse = "Use whenever Pattern Template Mask is enabled.",
                                        whyToUse = "Enforces exact formatting standards required by enterprise ERPs and legacy POS software.",
                                        conditionsToUse = "Must contain valid mask tokens to generate random variations.",
                                        example = "Mask 'GIFT-####-@@' -> Output: GIFT-9281-KX"
                                    )
                                }
                            )
                        }
                    }

                    // Casing Mode Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Casing Mode",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onConfigChange(config.copy(casingMode = CasingMode.DEFAULT)) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reset casing mode",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        activeHelpInfo = OptionHelpInfo(
                                            title = "Casing Mode",
                                            summary = "Enforces uniform uppercase or lowercase casing across all codes.",
                                            detailedDescription = "Applies post-generation case transformation across all assembled codes. Options include 'Preserve Case' (maintains mixed case), 'Force UPPERCASE' (converts all characters to capital letters), and 'Force lowercase' (converts all characters to small letters).",
                                            howToUse = "Select desired casing mode using the segmented button row.",
                                            whenToUse = "Use to ensure 100% case consistency across multi-channel exports.",
                                            whyToUse = "Prevents backend database indexing issues caused by mixed casing.",
                                            conditionsToUse = "Recommended for all automated code export workflows.",
                                            example = "Force UPPERCASE -> 'promo123' becomes 'PROMO123'"
                                        )
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Option details",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CasingMode.entries.forEachIndexed { index, mode ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = CasingMode.entries.size
                                    ),
                                    onClick = { onConfigChange(config.copy(casingMode = mode)) },
                                    selected = config.casingMode == mode
                                ) {
                                    Text(
                                        text = mode.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: GENERATION & SECURITY RULES
            CollapsibleSectionHeader(
                title = "GENERATION & SECURITY RULES",
                icon = Icons.Outlined.Lock,
                isExpanded = expandSecuritySection,
                onToggle = { expandSecuritySection = !expandSecuritySection }
            )

            AnimatedVisibility(visible = expandSecuritySection) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Unique Characters in Code
                    OptionSwitchRow(
                        checked = config.uniqueCharacters,
                        onCheckedChange = { onConfigChange(config.copy(uniqueCharacters = it)) },
                        label = "Unique characters in code",
                        onResetClick = { onConfigChange(config.copy(uniqueCharacters = false)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Unique characters in code",
                                summary = "Ensures every character within a single generated promo code is distinct without repeating.",
                                detailedDescription = "Enforces non-repetition of characters within any single generated promo code string (sampling without replacement). For example, 'ABC1234' is permitted, whereas 'AAB1123' is rejected. Note: requested code length cannot exceed total available pool size.",
                                howToUse = "Toggle the switch ON under 'GENERATION & SECURITY RULES' -> 'Unique characters in code'.",
                                whenToUse = "Use when generating short security PINs, puzzle codes, or high-variety voucher keys.",
                                whyToUse = "Prevents repetitive character patterns (like 'AAAA-1111') that look non-random or visually weak.",
                                conditionsToUse = "Ensure available pool size is greater than or equal to requested code length.",
                                example = "Valid unique code: 84920153 (No repeating digits)"
                            )
                        }
                    )

                    // Shuffle Code
                    OptionSwitchRow(
                        checked = config.shuffleCode,
                        onCheckedChange = { onConfigChange(config.copy(shuffleCode = it)) },
                        label = "Shuffle character positions",
                        onResetClick = { onConfigChange(config.copy(shuffleCode = true)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Shuffle character positions",
                                summary = "Randomizes the positions of characters within each generated code.",
                                detailedDescription = "When enabled, characters (including minimum digits, uppercase letters, and symbols) are randomly shuffled throughout the code. When disabled, characters remain in their assigned sequence (e.g. required digits first, then letters).",
                                howToUse = "Toggle 'Shuffle character positions' ON under 'GENERATION & SECURITY RULES'.",
                                whenToUse = "Use to ensure random distribution of character types across all positions in the code.",
                                whyToUse = "Prevents predictable character positioning patterns, increasing cryptographic entropy.",
                                conditionsToUse = "Should remain ON for all general random code generation tasks.",
                                example = "Enabled: 3BA21C vs Disabled: 123ABC"
                            )
                        }
                    )

                    // Pronounceable Mode
                    OptionSwitchRow(
                        checked = config.pronounceable,
                        onCheckedChange = { onConfigChange(config.copy(pronounceable = it)) },
                        label = "Pronounceable / Readability Mode",
                        onResetClick = { onConfigChange(config.copy(pronounceable = false)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Pronounceable / Phone Readable Mode",
                                summary = "Creates phonetically readable codes alternating consonants and vowels.",
                                detailedDescription = "Uses a specialized phonetic algorithm alternating consonants and vowels to generate readable word-like codes (e.g., 'KABEXI', 'VORAPU'). Easily spoken over phone or radio without spelling ambiguity.",
                                howToUse = "Toggle 'Pronounceable / Readability Mode' ON in 'GENERATION & SECURITY RULES'.",
                                whenToUse = "Ideal for telephone customer support, radio ads, podcasts, and voice assistants.",
                                whyToUse = "Eliminates verbal spelling confusion and memory fatigue.",
                                conditionsToUse = "Best used with 6-10 character lengths.",
                                example = "Phonetic output: KABEXI, VORAPU, ZELOME"
                            )
                        }
                    )

                    // Profanity Filter
                    OptionSwitchRow(
                        checked = config.enableProfanityFilter,
                        onCheckedChange = { onConfigChange(config.copy(enableProfanityFilter = it)) },
                        label = "Enable Profanity Filter",
                        onResetClick = { onConfigChange(config.copy(enableProfanityFilter = false)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Profanity & Bad Word Filter",
                                summary = "Filters out crude or inappropriate word combinations.",
                                detailedDescription = "Scans every generated code against an internal dictionary of offensive words across multiple languages. Matching codes are discarded and regenerated automatically.",
                                howToUse = "Toggle 'Enable Profanity Filter' ON under 'GENERATION & SECURITY RULES'.",
                                whenToUse = "Essential for public marketing campaigns, family brands, and customer vouchers.",
                                whyToUse = "Protects brand reputation and avoids customer embarrassment.",
                                conditionsToUse = "Should remain ON for all customer-facing campaigns.",
                                example = "Discards unwanted substrings and regenerates clean codes."
                            )
                        }
                    )

                    // Luhn Checksum
                    OptionSwitchRow(
                        checked = config.appendChecksum,
                        onCheckedChange = { onConfigChange(config.copy(appendChecksum = it)) },
                        label = "Append Luhn Mod-36 Checksum",
                        onResetClick = { onConfigChange(config.copy(appendChecksum = false)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Luhn Mod-36 Checksum",
                                summary = "Appends a mathematical checksum verification character to every code.",
                                detailedDescription = "Calculates a Luhn Mod-36 checksum character and appends it to the code. Allows servers and POS terminals to validate code integrity without database lookups.",
                                howToUse = "Toggle 'Append Luhn Mod-36 Checksum' ON under 'GENERATION & SECURITY RULES'.",
                                whenToUse = "Use for gift cards, high-volume retail vouchers, and financial tokens.",
                                whyToUse = "Instantly catches typos and manual entry errors at checkout.",
                                conditionsToUse = "Requires backend validation logic to verify the checksum character.",
                                example = "'CODE1234' -> Checksum appended: 'CODE1234X'"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MINIMUM CHARACTER CONSTRAINTS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    M3Stepper(
                        label = "Min Digits",
                        value = config.minDigits,
                        min = 0,
                        max = 16,
                        presets = listOf(0, 1, 2, 4),
                        onValueChange = { onConfigChange(config.copy(minDigits = it)) },
                        onResetClick = { onConfigChange(config.copy(minDigits = 0)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Minimum Digits",
                                summary = "Guarantees a minimum number of numerical digits in every code.",
                                detailedDescription = "Enforces a mandatory minimum count of numerical digits (0-9) per code. Codes failing the threshold are automatically regenerated.",
                                howToUse = "Adjust the stepper or select presets (0, 1, 2, 4) under 'Min Digits'.",
                                whenToUse = "Use when security policies mandate a numeric presence in codes.",
                                whyToUse = "Ensures balanced complexity and satisfies internal compliance.",
                                conditionsToUse = "Min Digits must not exceed total code length.",
                                example = "Min Digits = 2 -> Ensures at least two numbers exist per code."
                            )
                        }
                    )

                    M3Stepper(
                        label = "Min Uppercase",
                        value = config.minUpper,
                        min = 0,
                        max = 16,
                        presets = listOf(0, 1, 2, 4),
                        onValueChange = { onConfigChange(config.copy(minUpper = it)) },
                        onResetClick = { onConfigChange(config.copy(minUpper = 0)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Minimum Uppercase",
                                summary = "Guarantees a minimum number of uppercase letters in every code.",
                                detailedDescription = "Enforces a mandatory minimum count of capital letters (A-Z) per code.",
                                howToUse = "Adjust the stepper under 'Min Uppercase'.",
                                whenToUse = "Use when requiring capital letter presence for security.",
                                whyToUse = "Guarantees visual structure and complexity.",
                                conditionsToUse = "Min Uppercase must not exceed total code length.",
                                example = "Min Upper = 2 -> Guarantees at least two capital letters per code."
                            )
                        }
                    )

                    M3Stepper(
                        label = "Min Symbols",
                        value = config.minSymbols,
                        min = 0,
                        max = 16,
                        presets = listOf(0, 1, 2, 4),
                        onValueChange = { onConfigChange(config.copy(minSymbols = it)) },
                        onResetClick = { onConfigChange(config.copy(minSymbols = 0)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Minimum Symbols",
                                summary = "Guarantees a minimum number of special symbols in every code.",
                                detailedDescription = "Enforces a mandatory minimum count of special punctuation symbols per code.",
                                howToUse = "Adjust the stepper under 'Min Symbols'.",
                                whenToUse = "Use for high-security passwords and access tokens.",
                                whyToUse = "Mandates symbol density for cryptographic security.",
                                conditionsToUse = "Only active when Special Symbols are enabled.",
                                example = "Min Symbols = 1 -> Ensures at least one symbol exists per code."
                            )
                        }
                    )
                }
            }

            // Section 4: CAMPAIGN & EXPORT METADATA
            CollapsibleSectionHeader(
                title = "CAMPAIGN & EXPORT METADATA",
                icon = Icons.Outlined.Sell,
                isExpanded = expandMetadataSection,
                onToggle = { expandMetadataSection = !expandMetadataSection }
            )

            AnimatedVisibility(visible = expandMetadataSection) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    M3TextField(
                        label = "Discount Offer Text",
                        value = config.discountText,
                        placeholder = "20% OFF or $10 Credit",
                        onValueChange = { onConfigChange(config.copy(discountText = it)) },
                        onResetClick = { onConfigChange(config.copy(discountText = "")) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Discount Offer Text",
                                summary = "Attaches promotional discount notes to export files.",
                                detailedDescription = "Associates descriptive offer text (e.g., '20% OFF', '$10 Credit') with the generated batch. Included in CSV, Excel, and PDF exports for audit tracking.",
                                howToUse = "Type offer details into 'Discount Offer Text' in 'CAMPAIGN & EXPORT METADATA'.",
                                whenToUse = "Use when exporting codes for merchant partners or accounting.",
                                whyToUse = "Keeps offer metadata attached directly to export files.",
                                conditionsToUse = "Optional, but recommended for professional reporting.",
                                example = "Offer text: '20% OFF Summer Sale'"
                            )
                        }
                    )

                    M3Stepper(
                        label = "Expiry Days (0 = Unlimited)",
                        value = config.expiryDays,
                        min = 0,
                        max = 365,
                        presets = listOf(0, 7, 30, 90),
                        onValueChange = { onConfigChange(config.copy(expiryDays = it)) },
                        onResetClick = { onConfigChange(config.copy(expiryDays = 0)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Expiry Days",
                                summary = "Sets code validity period recorded in export reports.",
                                detailedDescription = "Defines campaign lifespan in days (e.g., 7, 30, 90 days). Calculates exact expiration date timestamps in exported reports.",
                                howToUse = "Set days using stepper or presets (0, 7, 30, 90). 0 = Unlimited.",
                                whenToUse = "Use for time-sensitive promotions and seasonal discounts.",
                                whyToUse = "Automates expiration date calculation for export reports.",
                                conditionsToUse = "Set to 0 for non-expiring promo codes.",
                                example = "Expiry Days = 30 -> Exports show calculated date 30 days from today."
                            )
                        }
                    )

                    M3Stepper(
                        label = "Max Usage Limit (0 = Unlimited)",
                        value = config.maxUsageCount,
                        min = 0,
                        max = 100,
                        presets = listOf(0, 1, 5, 10),
                        onValueChange = { onConfigChange(config.copy(maxUsageCount = it)) },
                        onResetClick = { onConfigChange(config.copy(maxUsageCount = 0)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Max Usage Limit",
                                summary = "Records maximum redemption limit per code.",
                                detailedDescription = "Specifies redemption count limits (e.g., 1 for single-use, 100 for multi-use). Saved in export metadata for database ingestion.",
                                howToUse = "Set limit using stepper or presets (0, 1, 5, 10). 0 = Unlimited.",
                                whenToUse = "Use for single-use gift cards or limited-use VIP vouchers.",
                                whyToUse = "Provides clear redemption limits for backend system rules.",
                                conditionsToUse = "Set to 1 for unique single-use coupon campaigns.",
                                example = "Max Usage = 1 -> Indicates single-use voucher."
                            )
                        }
                    )

                    OptionSwitchRow(
                        checked = config.includeHashInExport,
                        onCheckedChange = { onConfigChange(config.copy(includeHashInExport = it)) },
                        label = "Include SHA-256 Hash in Export Files",
                        onResetClick = { onConfigChange(config.copy(includeHashInExport = false)) },
                        onInfoClick = {
                            activeHelpInfo = OptionHelpInfo(
                                title = "Include SHA-256 Hash",
                                summary = "Calculates cryptographic SHA-256 hashes for export files.",
                                detailedDescription = "Generates a 64-character SHA-256 cryptographic hash for each promo code. Allows secure zero-knowledge backend verification without storing plaintext codes.",
                                howToUse = "Toggle 'Include SHA-256 Hash in Export Files' ON in 'CAMPAIGN & EXPORT METADATA'.",
                                whenToUse = "Use for high-security gift cards, vouchers, and secure authentication.",
                                whyToUse = "Protects backend databases against data leaks and unauthorized code discovery.",
                                conditionsToUse = "Recommended when security regulations require hashed storage.",
                                example = "Code 'PROMO123' -> Hash: 8f3c...6a2b"
                            )
                        }
                    )
                }
            }

            // Action Button: GENERATE
            Button(
                onClick = onGenerate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 3.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GENERATE CODES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    if (activeHelpInfo != null) {
        OptionDetailSheet(
            info = activeHelpInfo!!,
            onDismiss = { activeHelpInfo = null }
        )
    }
}

@Composable
private fun CollapsibleSectionHeader(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        onClick = onToggle,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Section",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun M3TextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    supportingText: String? = null,
    onResetClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = if (placeholder.isNotEmpty()) {
                { Text(placeholder, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)) }
            } else null,
            supportingText = if (!supportingText.isNullOrEmpty()) {
                { Text(supportingText, style = MaterialTheme.typography.bodySmall) }
            } else null,
            trailingIcon = if (onResetClick != null || onInfoClick != null) {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (onResetClick != null) {
                            IconButton(onClick = onResetClick, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset field",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        if (onInfoClick != null) {
                            IconButton(onClick = onInfoClick, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun OptionSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    preview: String? = null,
    onResetClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (preview != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (onResetClick != null) {
                    IconButton(
                        onClick = onResetClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset option",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (onInfoClick != null) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Option details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    onResetClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var textState by remember(value) { mutableStateOf(value.toString()) }
    val presets = listOf(10, 50, 100, 500, 1000, 5000, 10000)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Batch Quantity (1-100,000)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onResetClick != null) {
                    IconButton(
                        onClick = onResetClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset quantity",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (onInfoClick != null) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Option details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { preset ->
                val isSelected = preset == value
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = {
                        textState = preset.toString()
                        onValueChange(preset)
                    },
                    label = {
                        Text(
                            text = preset.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }
        }

        OutlinedTextField(
            value = textState,
            onValueChange = { input ->
                textState = input
                val digitsOnly = input.filter { it.isDigit() }
                if (digitsOnly.isNotEmpty()) {
                    val parsed = digitsOnly.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onValueChange(parsed.coerceAtMost(100000))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun M3Stepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    presets: List<Int>,
    onValueChange: (Int) -> Unit,
    onResetClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onResetClick != null) {
                    IconButton(
                        onClick = onResetClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset stepper",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                if (onInfoClick != null) {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Option details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (presets.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = preset == value
                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { onValueChange(preset) },
                        label = {
                            Text(
                                text = preset.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedIconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrement",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            var textState by remember(value) { mutableStateOf(value.toString()) }

            OutlinedTextField(
                value = textState,
                onValueChange = { text ->
                    textState = text
                    val cleanText = text.filter { it.isDigit() }
                    if (cleanText.isNotEmpty()) {
                        val parsed = cleanText.toIntOrNull()
                        if (parsed != null && parsed in min..max) {
                            onValueChange(parsed)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.weight(1f)
            )

            OutlinedIconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increment",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
