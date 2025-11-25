package com.example.mad_assignment.data.Database
import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.Arrays

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val uid: String,

    val email: String?,
    val displayName: String?,
    val address: String?,

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val profilePicture: ByteArray?
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserProfile
        if (uid != other.uid) return false
        if (email != other.email) return false
        if (displayName != other.displayName) return false
        if (profilePicture != null) {
            if (other.profilePicture == null) return false
            if (!profilePicture.contentEquals(other.profilePicture)) return false
        } else if (other.profilePicture != null) return false
        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(profilePicture)
    }
}


@Dao
interface UserDao {
    @Upsert
    suspend fun upsertUser(user: UserProfile)

    @androidx.room.Query("SELECT * FROM user_profile WHERE uid = :uid")
    fun getUser(uid: String): Flow<UserProfile?>
}



@Database(entities = [UserProfile::class], version = 2, exportSchema = false) // <-- STEP 1: Version bumped to 2
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}