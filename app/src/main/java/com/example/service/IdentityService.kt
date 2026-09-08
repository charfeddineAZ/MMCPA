package com.example.service

import com.example.data.model.ExtractedInfo
import com.example.data.model.GeneratedIdentity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Proxy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.concurrent.TimeUnit

data class UserAgentOption(val label: String, val value: String)

object IdentityService {

    val USER_AGENTS = listOf(
        UserAgentOption(
            label = "Chrome 122 · Windows",
            value = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
        ),
        UserAgentOption(
            label = "Chrome 121 · macOS",
            value = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
        ),
        UserAgentOption(
            label = "Safari 17 · iPhone",
            value = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.3 Mobile/15E148 Safari/604.1"
        ),
        UserAgentOption(
            label = "Safari 17 · macOS",
            value = "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15"
        ),
        UserAgentOption(
            label = "Firefox 123 · Windows",
            value = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0"
        ),
        UserAgentOption(
            label = "Edge 122 · Windows",
            value = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0"
        ),
        UserAgentOption(
            label = "Chrome 122 · Android",
            value = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.64 Mobile Safari/537.36"
        )
    )

    val RANDOM_REFERRERS = listOf(
        "https://www.google.com/search?q=deals+and+rewards",
        "https://www.bing.com/search?q=online+surveys+and+offers",
        "https://www.facebook.com/",
        "https://twitter.com/promotions",
        "https://www.instagram.com/",
        "https://www.tiktok.com/",
        "https://www.reddit.com/r/freebies/",
        "https://www.youtube.com/"
    )

    private val FIRST_NAMES_MALE = listOf(
        "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph",
        "Thomas", "Charles", "Daniel", "Matthew", "Anthony", "Mark", "Donald", "Steven",
        "Paul", "Andrew", "Joshua", "Kenneth", "Kevin", "Brian", "George", "Timothy",
        "Ronald", "Jason", "Edward", "Jeffrey", "Ryan", "Jacob", "Gary", "Nicholas"
    )

    private val FIRST_NAMES_FEMALE = listOf(
        "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica",
        "Sarah", "Karen", "Lisa", "Nancy", "Betty", "Margaret", "Sandra", "Ashley",
        "Kimberly", "Emily", "Donna", "Michelle", "Carol", "Amanda", "Melissa", "Deborah",
        "Stephanie", "Rebecca", "Sharon", "Laura", "Cynthia", "Kathleen", "Amy", "Angela"
    )

    private val LAST_NAMES = listOf(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
        "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
        "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker"
    )

    private val US_CITIES = listOf(
        Triple("New York", "NY", "10001"),
        Triple("Los Angeles", "CA", "90001"),
        Triple("Chicago", "IL", "60601"),
        Triple("Houston", "TX", "77001"),
        Triple("Phoenix", "AZ", "85001"),
        Triple("Philadelphia", "PA", "19101"),
        Triple("San Antonio", "TX", "78201"),
        Triple("San Diego", "CA", "92101"),
        Triple("Dallas", "TX", "75201"),
        Triple("Austin", "TX", "78701"),
        Triple("Jacksonville", "FL", "32201"),
        Triple("Fort Worth", "TX", "76101"),
        Triple("Columbus", "OH", "43201"),
        Triple("Charlotte", "NC", "28201"),
        Triple("Indianapolis", "IN", "46201"),
        Triple("Seattle", "WA", "98101"),
        Triple("Denver", "CO", "80201"),
        Triple("Washington", "DC", "20001"),
        Triple("Boston", "MA", "02101"),
        Triple("Nashville", "TN", "37201")
    )

    private val STREET_NAMES = listOf(
        "Main St", "Oak Ave", "Maple St", "Cedar Ln", "Elm St", "Washington Blvd",
        "Park Ave", "Lakeview Dr", "Pine St", "Sunset Blvd", "Broadway", "Highland Ave",
        "Church Rd", "Lincoln Way", "Hillcrest Ave", "Valley Dr", "River Rd"
    )

    private val BANKS = listOf(
        "Chase Bank", "Bank of America", "Wells Fargo", "Citibank", "Capital One",
        "PNC Bank", "US Bank", "TD Bank", "Truist", "Discover Bank"
    )

    private val EMAIL_DOMAINS = listOf(
        "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "icloud.com", "proton.me"
    )

    private val rnd = Random()

    fun generateUTM(baseUrl: String): String {
        val sources = listOf("google", "facebook", "newsletter", "twitter", "partner_direct")
        val mediums = listOf("cpc", "social", "email", "banner", "cpa_affiliate")
        val campaigns = listOf("spring_promo", "rewards_2026", "signup_bonus", "special_offer", "lead_gen")

        val source = sources[rnd.nextInt(sources.size)]
        val medium = mediums[rnd.nextInt(mediums.size)]
        val campaign = campaigns[rnd.nextInt(campaigns.size)]
        val content = "var_${rnd.nextInt(900) + 100}"

        val separator = if (baseUrl.contains("?")) "&" else "?"
        return "$baseUrl${separator}utm_source=$source&utm_medium=$medium&utm_campaign=$campaign&utm_content=$content"
    }

    fun generateIdentity(countryCode: String = "US", providedEmail: String? = null): GeneratedIdentity {
        val isMale = rnd.nextBoolean()
        val gender = if (isMale) "Male" else "Female"
        val firstName = if (isMale) {
            FIRST_NAMES_MALE[rnd.nextInt(FIRST_NAMES_MALE.size)]
        } else {
            FIRST_NAMES_FEMALE[rnd.nextInt(FIRST_NAMES_FEMALE.size)]
        }
        val lastName = LAST_NAMES[rnd.nextInt(LAST_NAMES.size)]
        val fullName = "$firstName $lastName"

        val email = providedEmail?.trim()?.takeIf { it.isNotEmpty() } ?: run {
            val domain = EMAIL_DOMAINS[rnd.nextInt(EMAIL_DOMAINS.size)]
            val num = rnd.nextInt(900) + 100
            "${firstName.lowercase(Locale.US)}.${lastName.lowercase(Locale.US)}$num@$domain"
        }

        val username = "${firstName.lowercase(Locale.US)}_${lastName.lowercase(Locale.US)}${rnd.nextInt(90) + 10}"
        val password = "${firstName.take(3)}!${lastName.take(3)}#${rnd.nextInt(9000) + 1000}"

        val streetNum = rnd.nextInt(8999) + 100
        val streetName = STREET_NAMES[rnd.nextInt(STREET_NAMES.size)]
        val address = "$streetNum $streetName"

        val cityData = US_CITIES[rnd.nextInt(US_CITIES.size)]
        val city = cityData.first
        val state = cityData.second
        val postalCode = cityData.third

        // Phone: +1 (Area) XXX-XXXX
        val areaCode = rnd.nextInt(800) + 200
        val phoneMid = rnd.nextInt(900) + 100
        val phoneEnd = rnd.nextInt(9000) + 1000
        val phone = "+1 ($areaCode) $phoneMid-$phoneEnd"

        // Birth date (age 20-55)
        val cal = Calendar.getInstance()
        val age = rnd.nextInt(35) + 20
        cal.add(Calendar.YEAR, -age)
        cal.set(Calendar.MONTH, rnd.nextInt(12))
        cal.set(Calendar.DAY_OF_MONTH, rnd.nextInt(27) + 1)
        val birthDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

        // Card generation with Luhn algorithm
        val cardType = when (rnd.nextInt(4)) {
            0 -> "Visa"
            1 -> "Mastercard"
            2 -> "Amex"
            else -> "Discover"
        }
        val cardNumber = generateValidLuhnCard(cardType)
        val expMonth = String.format(Locale.US, "%02d", rnd.nextInt(12) + 1)
        val expYear = (Calendar.getInstance().get(Calendar.YEAR) % 100) + rnd.nextInt(5) + 2
        val cardExpiry = "$expMonth/$expYear"
        val cardCvv = String.format(Locale.US, if (cardType == "Amex") "%04d" else "%03d", rnd.nextInt(if (cardType == "Amex") 9000 else 900) + 100)
        val bankName = BANKS[rnd.nextInt(BANKS.size)]

        return GeneratedIdentity(
            firstName = firstName,
            lastName = lastName,
            fullName = fullName,
            email = email,
            phone = phone,
            username = username,
            password = password,
            gender = gender,
            birthDate = birthDate,
            address = address,
            city = city,
            state = state,
            postalCode = postalCode,
            country = "United States",
            cardNumber = cardNumber,
            cardExpiry = cardExpiry,
            cardCvv = cardCvv,
            cardType = cardType,
            cardHolder = fullName.uppercase(Locale.US),
            bankName = bankName
        )
    }

    private fun generateValidLuhnCard(cardType: String): String {
        val (prefix, length) = when (cardType) {
            "Visa" -> Pair("4", 16)
            "Mastercard" -> Pair("5${rnd.nextInt(5) + 1}", 16)
            "Amex" -> Pair("37", 15)
            else -> Pair("6011", 16)
        }

        val digits = mutableListOf<Int>()
        for (ch in prefix) {
            digits.add(ch - '0')
        }
        while (digits.size < length - 1) {
            digits.add(rnd.nextInt(10))
        }

        // Calculate Luhn check digit
        var sum = 0
        var alt = true
        for (i in digits.size - 1 downTo 0) {
            var d = digits[i]
            if (alt) {
                d *= 2
                if (d > 9) d -= 9
            }
            sum += d
            alt = !alt
        }
        val checkDigit = (10 - (sum % 10)) % 10
        digits.add(checkDigit)

        // Format in groups of 4
        val raw = digits.joinToString("")
        return raw.chunked(4).joinToString(" ")
    }

    suspend fun fetchGeoInfo(
        proxyHost: String? = null,
        proxyPort: Int? = null,
        proxyType: String? = null
    ): ExtractedInfo = withContext(Dispatchers.IO) {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)

        if (!proxyHost.isNullOrBlank() && proxyPort != null && proxyPort > 0 && proxyType != "none") {
            try {
                val pType = if (proxyType?.equals("socks", ignoreCase = true) == true || proxyType?.startsWith("socks") == true) {
                    Proxy.Type.SOCKS
                } else {
                    Proxy.Type.HTTP
                }
                clientBuilder.proxy(Proxy(pType, InetSocketAddress(proxyHost, proxyPort)))
            } catch (e: Exception) {
                // Ignore proxy setup failure
            }
        }

        val client = clientBuilder.build()

        val endpoints = listOf(
            "https://ipapi.co/json/",
            "https://api.i.pn/json/",
            "https://ipwho.is/"
        )

        for (url in endpoints) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: continue
                    val json = JSONObject(body)

                    val ip = json.optString("ip", json.optString("query", "104.28.19.42"))
                    val country = json.optString("country_name", json.optString("country", "United States"))
                    val countryCode = json.optString("country_code", json.optString("countryCode", "US"))
                    val city = json.optString("city", "New York")
                    val region = json.optString("region", json.optString("region_code", "NY"))
                    val postal = json.optString("postal", json.optString("zip", "10001"))
                    val timezone = json.optString("timezone", "America/New_York")
                    val currency = json.optString("currency", "USD")
                    val isp = json.optString("org", json.optString("isp", "Residential Cloud"))
                    val lat = json.optDouble("latitude", 40.7128)
                    val lon = json.optDouble("longitude", -74.0060)

                    return@withContext ExtractedInfo(
                        ip = ip,
                        country = country,
                        countryCode = countryCode,
                        city = city,
                        region = region,
                        street = "",
                        postalCode = postal,
                        timezone = timezone,
                        language = "en-$countryCode",
                        currency = currency,
                        isp = isp,
                        org = isp,
                        latitude = lat,
                        longitude = lon,
                        isProxy = !proxyHost.isNullOrBlank()
                    )
                }
            } catch (e: Exception) {
                // Try next endpoint
            }
        }

        // Fallback realistic IP if network is unavailable or rate-limited
        val fakeIp = "${rnd.nextInt(180) + 40}.${rnd.nextInt(250) + 1}.${rnd.nextInt(250) + 1}.${rnd.nextInt(250) + 1}"
        val city = US_CITIES[rnd.nextInt(US_CITIES.size)]
        ExtractedInfo(
            ip = fakeIp,
            country = "United States",
            countryCode = "US",
            city = city.first,
            region = city.second,
            street = "",
            postalCode = city.third,
            timezone = "America/New_York",
            language = "en-US",
            currency = "USD",
            isp = "Charter Communications",
            org = "Spectrum Residential",
            latitude = 40.7128,
            longitude = -74.0060,
            isProxy = !proxyHost.isNullOrBlank()
        )
    }

    suspend fun checkLeadCPA(userId: String, apiKey: String, ip: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (userId.isBlank() || apiKey.isBlank()) {
            return@withContext Pair(false, "CPA Grip User ID and API Key required in Settings")
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val url = "https://www.cpagrip.com/common/lead_check_rss.php?user_id=$userId&key=$apiKey&ip=$ip"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "CPAAutomator/1.0")
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Pair(false, "API Error: HTTP ${response.code}")
            }

            val hasLead = body.contains("<lead>") ||
                    body.contains("<status>lead</status>") ||
                    (body.contains("<item>") && body.contains(ip))

            if (hasLead) {
                Pair(true, "Lead successfully verified for IP: $ip")
            } else {
                Pair(false, "No conversion detected yet for IP: $ip")
            }
        } catch (e: Exception) {
            Pair(false, "Check lead failed: ${e.localizedMessage}")
        }
    }
}
