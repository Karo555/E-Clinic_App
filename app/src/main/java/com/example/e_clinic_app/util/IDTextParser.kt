package com.example.e_clinic_app.util

/**
 * Utility for parsing ID card text extracted via OCR.
 * Uses pattern matching to identify common fields found on medical IDs and licenses.
 */
object IDTextParser {

    /**
     * Parse OCR text from an ID card and extract structured information
     *
     * @param text Raw text from OCR processing
     * @return Map of field names to extracted values
     */
    fun parseIDText(text: String): Map<String, String> {
        val result = mutableMapOf<String, String>()

        // Extract name (assuming format "NAME: John Doe" or "Dr. John Doe")
        extractName(text)?.let { result["name"] = it }

        // Extract license number (assuming format "LIC#: 12345" or "LICENSE: 12345")
        extractLicenseNumber(text)?.let { result["licenseNumber"] = it }

        // Extract specialization if available
        extractSpecialization(text)?.let { result["specialization"] = it }

        // Extract years of experience if available (e.g., "Experience: 10 years")
        extractExperienceYears(text)?.let { result["experienceYears"] = it }

        return result
    }

    /**
     * Extract full name from ID text
     */
    private fun extractName(text: String): String? {
        // Common patterns for name on IDs
        val namePatterns = listOf(
            Regex("(?i)name[:\\s]+([A-Za-z\\s.]+)"),
            Regex("(?i)Dr\\.?\\s([A-Za-z\\s]+)")
        )

        namePatterns.forEach { pattern ->
            pattern.find(text)?.let {
                return it.groupValues[1].trim()
            }
        }

        // If no pattern matches, try to find the first line that looks like a name
        val lines = text.split("\n")
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.matches(Regex("[A-Za-z.\\s]{5,}"))) {
                return trimmed
            }
        }

        return null
    }

    /**
     * Extract license number from ID text
     */
    private fun extractLicenseNumber(text: String): String? {
        val licensePatterns = listOf(
            Regex("(?i)lic(?:ense)?(?:\\s?#)?[:\\s]+([A-Z0-9-]+)"),
            Regex("(?i)medical license[:\\s]+([A-Z0-9-]+)"),
            Regex("(?i)license[:\\s]+([A-Z0-9-]+)")
        )

        licensePatterns.forEach { pattern ->
            pattern.find(text)?.let {
                return it.groupValues[1].trim()
            }
        }

        return null
    }

    /**
     * Extract medical specialization from ID text
     */
    private fun extractSpecialization(text: String): String? {
        val specializationPatterns = listOf(
            Regex("(?i)special(?:ization|ity)[:\\s]+([A-Za-z\\s]+)"),
            Regex("(?i)field[:\\s]+([A-Za-z\\s]+)")
        )

        specializationPatterns.forEach { pattern ->
            pattern.find(text)?.let {
                return it.groupValues[1].trim()
            }
        }

        return null
    }

    /**
     * Extract years of experience from ID text
     */
    private fun extractExperienceYears(text: String): String? {
        val experiencePatterns = listOf(
            Regex("(?i)experience[:\\s]+(\\d+)\\s*years?"),
            Regex("(?i)(\\d+)\\s*years?\\s*experience")
        )

        experiencePatterns.forEach { pattern ->
            pattern.find(text)?.let {
                return it.groupValues[1].trim()
            }
        }

        return null
    }
}
