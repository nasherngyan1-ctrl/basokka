package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BasoProjectDao {
    @Query("SELECT * FROM baso_projects ORDER BY updatedAt DESC")
    fun observeAllProjects(): Flow<List<BasoProjectEntity>>

    @Query("SELECT COUNT(*) FROM baso_projects")
    suspend fun getProjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: BasoProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(projects: List<BasoProjectEntity>)

    @Update
    suspend fun updateProject(project: BasoProjectEntity)

    @Query("DELETE FROM baso_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}

@Database(entities = [BasoProjectEntity::class], version = 2, exportSchema = false)
abstract class BasoDatabase : RoomDatabase() {
    abstract fun projectDao(): BasoProjectDao

    companion object {
        @Volatile
        private var INSTANCE: BasoDatabase? = null

        fun getInstance(context: Context): BasoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BasoDatabase::class.java,
                    "baso_studio_local.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class BasoRepository(private val dao: BasoProjectDao) {
    val allProjects: Flow<List<BasoProjectEntity>> = dao.observeAllProjects()

    suspend fun ensureSeeded() {
        if (dao.getProjectCount() == 0) {
            dao.insertAll(StarterTemplates.initialSeedProjects())
        }
    }

    suspend fun saveProject(project: BasoProjectEntity): Int {
        val rowId = dao.insertProject(project)
        return if (project.id != 0) project.id else rowId.toInt()
    }

    suspend fun updateProject(project: BasoProjectEntity) {
        dao.updateProject(project)
    }

    suspend fun deleteProject(id: Int) {
        dao.deleteProjectById(id)
    }
}
