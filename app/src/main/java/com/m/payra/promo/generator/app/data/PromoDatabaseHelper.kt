package com.m.payra.promo.generator.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.m.payra.promo.generator.app.engine.ChecksumHelper
import com.m.payra.promo.generator.app.engine.GeneratorConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CampaignRecord(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val quantity: Int,
    val configSummary: String
) {
    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(createdAt))
}

data class CodeRecord(
    val id: Long,
    val campaignId: Long,
    val code: String,
    val createdAt: Long,
    val isRedeemed: Boolean,
    val redeemedAt: Long?
)

data class CodeVerificationStatus(
    val code: String,
    val existsInDatabase: Boolean,
    val isRedeemed: Boolean,
    val redeemedAt: Long?,
    val campaignName: String?,
    val isChecksumValid: Boolean,
    val checksumTypeDetected: String?
)

data class BatchVerificationStats(
    val totalChecked: Int,
    val validInDb: Int,
    val alreadyRedeemed: Int,
    val unredeemed: Int,
    val invalidChecksum: Int,
    val notFoundInDb: Int
)

class PromoDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "promocodes.db"
        const val DATABASE_VERSION = 1

        private const val TABLE_CAMPAIGNS = "campaigns"
        private const val TABLE_CODES = "promo_codes"

        @Volatile
        private var instance: PromoDatabaseHelper? = null

        fun getInstance(context: Context): PromoDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: PromoDatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CAMPAIGNS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                config_summary TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_CODES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                campaign_id INTEGER NOT NULL,
                code TEXT NOT NULL UNIQUE,
                created_at INTEGER NOT NULL,
                is_redeemed INTEGER NOT NULL DEFAULT 0,
                redeemed_at INTEGER,
                FOREIGN KEY (campaign_id) REFERENCES $TABLE_CAMPAIGNS(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL("CREATE INDEX idx_code ON $TABLE_CODES(code)")
        db.execSQL("CREATE INDEX idx_campaign ON $TABLE_CODES(campaign_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CODES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CAMPAIGNS")
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    /**
     * Save a generated batch of promo codes into a new campaign
     */
    fun saveBatch(
        name: String,
        config: GeneratorConfig,
        codes: List<String>
    ): Long {
        if (codes.isEmpty()) return -1

        val db = writableDatabase
        db.beginTransaction()
        try {
            val now = System.currentTimeMillis()
            val summary = buildString {
                append("Len: ${config.length}")
                if (config.prefix.isNotBlank()) append(", Pre: ${config.prefix}")
                if (config.suffix.isNotBlank()) append(", Suf: ${config.suffix}")
                if (config.appendChecksum) {
                    append(", Checksum: Luhn Mod-36")
                }
            }

            val campaignValues = ContentValues().apply {
                put("name", name)
                put("created_at", now)
                put("quantity", codes.size)
                put("config_summary", summary)
            }

            val campaignId = db.insert(TABLE_CAMPAIGNS, null, campaignValues)
            if (campaignId == -1L) return -1

            for (code in codes) {
                val codeValues = ContentValues().apply {
                    put("campaign_id", campaignId)
                    put("code", code)
                    put("created_at", now)
                    put("is_redeemed", 0)
                }
                db.insertWithOnConflict(TABLE_CODES, null, codeValues, SQLiteDatabase.CONFLICT_IGNORE)
            }

            db.setTransactionSuccessful()
            return campaignId
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Get all campaigns ordered by newest first
     */
    fun getAllCampaigns(): List<CampaignRecord> {
        val list = mutableListOf<CampaignRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, created_at, quantity, config_summary FROM $TABLE_CAMPAIGNS ORDER BY created_at DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CampaignRecord(
                        id = it.getLong(0),
                        name = it.getString(1),
                        createdAt = it.getLong(2),
                        quantity = it.getInt(3),
                        configSummary = it.getString(4)
                    )
                )
            }
        }
        return list
    }

    /**
     * Get codes for a specific campaign
     */
    fun getCodesForCampaign(campaignId: Long): List<CodeRecord> {
        val list = mutableListOf<CodeRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, campaign_id, code, created_at, is_redeemed, redeemed_at FROM $TABLE_CODES WHERE campaign_id = ? ORDER BY id ASC",
            arrayOf(campaignId.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    CodeRecord(
                        id = it.getLong(0),
                        campaignId = it.getLong(1),
                        code = it.getString(2),
                        createdAt = it.getLong(3),
                        isRedeemed = it.getInt(4) == 1,
                        redeemedAt = if (it.isNull(5)) null else it.getLong(5)
                    )
                )
            }
        }
        return list
    }

    /**
     * Delete a campaign and all its codes
     */
    fun deleteCampaign(campaignId: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_CAMPAIGNS, "id = ?", arrayOf(campaignId.toString())) > 0
    }

    /**
     * Verify a promo code (checks DB existence + Checksum mathematical validity)
     */
    fun verifyCode(rawCode: String): CodeVerificationStatus {
        val cleanCode = rawCode.trim()
        val db = readableDatabase

        var exists = false
        var isRedeemed = false
        var redeemedAt: Long? = null
        var campaignName: String? = null

        val query = """
            SELECT c.is_redeemed, c.redeemed_at, cmp.name 
            FROM $TABLE_CODES c
            INNER JOIN $TABLE_CAMPAIGNS cmp ON c.campaign_id = cmp.id
            WHERE c.code = ? LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(cleanCode))
        cursor.use {
            if (it.moveToFirst()) {
                exists = true
                isRedeemed = it.getInt(0) == 1
                redeemedAt = if (it.isNull(1)) null else it.getLong(1)
                campaignName = it.getString(2)
            }
        }

        // Mathematical Checksum verification
        val checksumValid = ChecksumHelper.verifyChecksum(cleanCode)
        val detectedType = if (checksumValid) "Luhn Mod-36" else null

        return CodeVerificationStatus(
            code = cleanCode,
            existsInDatabase = exists,
            isRedeemed = isRedeemed,
            redeemedAt = redeemedAt,
            campaignName = campaignName,
            isChecksumValid = checksumValid,
            checksumTypeDetected = detectedType
        )
    }

    /**
     * Mark a code as redeemed
     */
    fun redeemCode(code: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_redeemed", 1)
            put("redeemed_at", System.currentTimeMillis())
        }
        val rows = db.update(TABLE_CODES, values, "code = ?", arrayOf(code.trim()))
        return rows > 0
    }

    /**
     * Un-redeem a code
     */
    fun unredeemCode(code: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_redeemed", 0)
            putNull("redeemed_at")
        }
        val rows = db.update(TABLE_CODES, values, "code = ?", arrayOf(code.trim()))
        return rows > 0
    }

    /**
     * Batch verify a list of codes
     */
    fun verifyBatch(codes: List<String>): BatchVerificationStats {
        var validInDb = 0
        var alreadyRedeemed = 0
        var unredeemed = 0
        var invalidChecksum = 0
        var notFoundInDb = 0

        for (c in codes) {
            val status = verifyCode(c)
            if (status.existsInDatabase) {
                validInDb++
                if (status.isRedeemed) alreadyRedeemed++ else unredeemed++
            } else {
                notFoundInDb++
            }

            if (!status.isChecksumValid) {
                invalidChecksum++
            }
        }

        return BatchVerificationStats(
            totalChecked = codes.size,
            validInDb = validInDb,
            alreadyRedeemed = alreadyRedeemed,
            unredeemed = unredeemed,
            invalidChecksum = invalidChecksum,
            notFoundInDb = notFoundInDb
        )
    }
}
