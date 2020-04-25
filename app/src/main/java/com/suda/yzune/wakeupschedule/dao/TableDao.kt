package com.suda.yzune.wakeupschedule.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.suda.yzune.wakeupschedule.bean.TableBean

@Dao
interface TableDao {
    @Insert
    suspend fun insertTable(tableBean: TableBean): Long

    @Update
    suspend fun updateTable(tableBean: TableBean)

    @Query("select max(id) from tablebean")
    suspend fun getLastId(): Int?

    @Query("select * from tablebean where id = :tableId")
    suspend fun getTableById(tableId: Int): TableBean?

    @Query("select * from tablebean where id = :tableId")
    fun getTableByIdSync(tableId: Int): TableBean?

    @Query("select * from tablebean")
    fun getTableListLiveData(): LiveData<List<TableBean>>

    @Query("select * from tablebean")
    suspend fun getTableList(): List<TableBean>

    @Query("delete from tablebean where id = :id")
    suspend fun deleteTable(id: Int)

    @Query("delete from coursebasebean where tableId = :id")
    suspend fun clearTable(id: Int)
}