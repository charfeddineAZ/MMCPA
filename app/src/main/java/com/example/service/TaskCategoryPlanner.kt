package com.example.service

import org.json.JSONArray
import org.json.JSONObject

data class CategoryDefinition(
    val id: String,
    val labelEn: String,
    val labelAr: String,
    val emoji: String,
    val priority: Int, // Lower number executes earlier in funnel
    val description: String,
    val matchingKeywords: List<String>
)

data class PlannedStep(
    val order: Int,
    val id: String,
    val labelEn: String,
    val labelAr: String,
    val emoji: String,
    val priority: Int,
    val description: String
)

object TaskCategoryPlanner {

    val PRESET_CATEGORIES = listOf(
        CategoryDefinition(
            id = "email_submit",
            labelEn = "Email Submit",
            labelAr = "إدخال البريد",
            emoji = "✉️",
            priority = 10,
            description = "Detect single email input field on landing page, enter email, and proceed",
            matchingKeywords = listOf("email", "email submit", "mail", "بريد", "ايميل")
        ),
        CategoryDefinition(
            id = "zip_submit",
            labelEn = "Zip Submit",
            labelAr = "رمز بريدي",
            emoji = "📍",
            priority = 20,
            description = "Detect postal/zip code input for geo-targeting and submit",
            matchingKeywords = listOf("zip", "postal", "zip submit", "رمز بريدي", "كود بريدي")
        ),
        CategoryDefinition(
            id = "lead_gen",
            labelEn = "Lead Gen Form",
            labelAr = "استمارة بيانات",
            emoji = "📋",
            priority = 30,
            description = "Fill complete contact info (first & last name, address, phone, city, state)",
            matchingKeywords = listOf("lead", "lead gen", "form", "contact", "بيانات", "استمارة")
        ),
        CategoryDefinition(
            id = "survey_quiz",
            labelEn = "Survey / Quiz",
            labelAr = "استبيان وأسئلة",
            emoji = "📝",
            priority = 40,
            description = "Answer qualification questions, select interactive button answers and radios",
            matchingKeywords = listOf("survey", "quiz", "questions", "poll", "استبيان", "اسئلة", "كويز")
        ),
        CategoryDefinition(
            id = "skip_upsells",
            labelEn = "Skip Upsells",
            labelAr = "تخطي العروض",
            emoji = "⏭️",
            priority = 50,
            description = "Identify sponsored co-reg offers and click 'No Thanks', 'Skip', 'Not interested'",
            matchingKeywords = listOf("skip", "upsell", "no thanks", "sponsor", "تخطي", "عروض اضافية")
        ),
        CategoryDefinition(
            id = "sign_up",
            labelEn = "Sign Up",
            labelAr = "تسجيل حساب",
            emoji = "👤",
            priority = 60,
            description = "Fill account registration fields, generate secure password, and submit register",
            matchingKeywords = listOf("sign up", "signup", "register", "create account", "تسجيل", "انشاء حساب")
        ),
        CategoryDefinition(
            id = "pin_submit",
            labelEn = "PIN Submit",
            labelAr = "تأكيد الهاتف",
            emoji = "📱",
            priority = 70,
            description = "Enter mobile phone number and prepare for carrier verification / SMS",
            matchingKeywords = listOf("pin", "pin submit", "sms", "phone verify", "تاكيد هاتف", "رقم الهاتف")
        ),
        CategoryDefinition(
            id = "terms_agreement",
            labelEn = "Terms Agreement",
            labelAr = "موافقة الشروط",
            emoji = "📜",
            priority = 80,
            description = "Ensure terms, privacy policy, and 18+ majority age checkboxes are checked",
            matchingKeywords = listOf("terms", "agree", "checkbox", "18", "شروط", "موافقة")
        ),
        CategoryDefinition(
            id = "completion_confirm",
            labelEn = "Confirmation",
            labelAr = "تأكيد الإكمال",
            emoji = "🏆",
            priority = 90,
            description = "Confirm reward claim, detect thank-you/success receipt and finalize conversion",
            matchingKeywords = listOf("confirm", "claim", "thank you", "complete", "تاكيد", "مكافأة")
        )
    )

    fun parseCategories(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.split(",", ";", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    fun findDefinition(categoryName: String): CategoryDefinition? {
        val normalized = categoryName.trim().lowercase()
        return PRESET_CATEGORIES.firstOrNull { def ->
            def.id == normalized ||
            def.labelEn.lowercase() == normalized ||
            def.labelAr.lowercase() == normalized ||
            def.matchingKeywords.any { kw -> normalized.contains(kw) }
        }
    }

    /**
     * Intelligently arranges and orders the user-supplied categories into
     * an optimal CPA conversion funnel execution pipeline.
     */
    fun orderCategories(categories: List<String>): List<PlannedStep> {
        if (categories.isEmpty()) return emptyList()

        // Match each user category to known definitions or assign dynamic priority
        val matchedList = categories.map { userCat ->
            val def = findDefinition(userCat)
            if (def != null) {
                PlannedStep(
                    order = 0,
                    id = def.id,
                    labelEn = def.labelEn,
                    labelAr = def.labelAr,
                    emoji = def.emoji,
                    priority = def.priority,
                    description = def.description
                )
            } else {
                // Custom user category
                val inferredPriority = when {
                    userCat.contains("email", ignoreCase = true) || userCat.contains("mail", ignoreCase = true) -> 12
                    userCat.contains("zip", ignoreCase = true) || userCat.contains("postal", ignoreCase = true) -> 22
                    userCat.contains("form", ignoreCase = true) || userCat.contains("info", ignoreCase = true) -> 32
                    userCat.contains("survey", ignoreCase = true) || userCat.contains("quiz", ignoreCase = true) -> 42
                    userCat.contains("skip", ignoreCase = true) || userCat.contains("pass", ignoreCase = true) -> 52
                    userCat.contains("sign", ignoreCase = true) || userCat.contains("reg", ignoreCase = true) -> 62
                    userCat.contains("pin", ignoreCase = true) || userCat.contains("sms", ignoreCase = true) -> 72
                    userCat.contains("term", ignoreCase = true) || userCat.contains("agree", ignoreCase = true) -> 82
                    else -> 55 // Default mid-priority
                }
                PlannedStep(
                    order = 0,
                    id = userCat.lowercase().replace(" ", "_"),
                    labelEn = userCat,
                    labelAr = userCat,
                    emoji = "🎯",
                    priority = inferredPriority,
                    description = "Custom automated step for '$userCat'"
                )
            }
        }

        // Sort by funnel priority and re-index
        return matchedList
            .distinctBy { it.id }
            .sortedBy { it.priority }
            .mapIndexed { index, step ->
                step.copy(order = index + 1)
            }
    }

    /**
     * Builds a human-readable summary of the AI execution plan.
     * E.g.: "1. ✉️ Email Submit ➔ 2. 📝 Survey / Quiz ➔ 3. 👤 Sign Up"
     */
    fun formatPlanSummary(categories: List<String>): String {
        val steps = orderCategories(categories)
        if (steps.isEmpty()) return "Standard Full-Auto Flow"
        return steps.joinToString(" ➔ ") { "${it.order}. ${it.emoji} ${it.labelEn}" }
    }

    /**
     * Builds a JSON string representing the ordered plan for JavaScript automation injection.
     */
    fun buildPlanJson(categories: List<String>): String {
        val steps = orderCategories(categories)
        val jsonArray = JSONArray()
        for (s in steps) {
            val obj = JSONObject()
            obj.put("order", s.order)
            obj.put("id", s.id)
            obj.put("label", s.labelEn)
            obj.put("priority", s.priority)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }
}
