package com.suda.yzune.wakeupschedule

import android.content.Context
import androidx.core.content.edit
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.suda.yzune.wakeupschedule.bean.*
import com.suda.yzune.wakeupschedule.dao.*
import com.suda.yzune.wakeupschedule.utils.Const
import com.suda.yzune.wakeupschedule.utils.getPrefer

@Database(entities = [CourseBaseBean::class, CourseDetailBean::class, AppWidgetBean::class, TimeDetailBean::class,
    TimeTableBean::class, TableBean::class],
        version = 9, exportSchema = false)

abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                AppDatabase::class.java, "wakeup")
                                .allowMainThreadQueries()
                                .addMigrations(migration7to8)
                                .addMigrations(_8to9Migration(context))
                                .build()
                    }
                }
            }
            return INSTANCE!!
        }

        private val migration7to8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE TimeTableBean (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL);")
                database.execSQL("INSERT INTO TimeTableBean VALUES(1, '默认');")
                database.execSQL("CREATE TABLE TableBean (\n" +
                        "    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                        "    tableName TEXT NOT NULL, \n" +
                        "    nodes INTEGER NOT NULL DEFAULT 11, \n" +
                        "    background TEXT NOT NULL DEFAULT '',\n" +
                        "    timeTable INTEGER NOT NULL DEFAULT 1,\n" +
                        "    startDate TEXT NOT NULL DEFAULT '2019-02-25',\n" +
                        "    maxWeek INTEGER NOT NULL DEFAULT 30,\n" +
                        "    itemHeight INTEGER NOT NULL DEFAULT 56,\n" +
                        "    itemAlpha INTEGER NOT NULL DEFAULT 60,\n" +
                        "    itemTextSize INTEGER NOT NULL DEFAULT 12,\n" +
                        "    widgetItemHeight INTEGER NOT NULL DEFAULT 56,\n" +
                        "    widgetItemAlpha INTEGER NOT NULL DEFAULT 60,\n" +
                        "    widgetItemTextSize INTEGER NOT NULL DEFAULT 12,\n" +
                        "    strokeColor INTEGER NOT NULL DEFAULT 0x80ffffff,\n" +
                        "    widgetStrokeColor INTEGER NOT NULL DEFAULT 0x80ffffff,\n" +
                        "    textColor INTEGER NOT NULL DEFAULT 0xff000000,\n" +
                        "    widgetTextColor INTEGER NOT NULL DEFAULT 0xff000000,\n" +
                        "    courseTextColor INTEGER NOT NULL DEFAULT 0xff000000,\n" +
                        "    widgetCourseTextColor INTEGER NOT NULL DEFAULT 0xff000000,\n" +
                        "    showSat INTEGER NOT NULL DEFAULT 1,\n" +
                        "    showSun INTEGER NOT NULL DEFAULT 1,\n" +
                        "    sundayFirst INTEGER NOT NULL DEFAULT 0,\n" +
                        "    showOtherWeekCourse INTEGER NOT NULL DEFAULT 0,\n" +
                        "    showTime INTEGER NOT NULL DEFAULT 0,\n" +
                        "    type INTEGER NOT NULL DEFAULT 0,\n" +
                        "    FOREIGN KEY (timeTable) REFERENCES TimeTableBean (id) ON DELETE SET DEFAULT ON UPDATE CASCADE\n" +
                        ");")
                database.execSQL("CREATE INDEX index_TableBean_id_timeTable ON TableBean (timeTable ASC);")
                database.execSQL("ALTER TABLE CourseBaseBean RENAME TO CourseBaseBean_old;")
                database.execSQL("CREATE TABLE CourseBaseBean(id INTEGER NOT NULL, courseName TEXT NOT NULL, color TEXT NOT NULL, tableId INTEGER NOT NULL, PRIMARY KEY (id, tableId), FOREIGN KEY (tableId) REFERENCES TableBean (id) ON DELETE CASCADE ON UPDATE CASCADE);")
                database.execSQL("INSERT INTO TableBean (tableName) VALUES('');")
                database.execSQL("INSERT INTO TableBean (tableName) VALUES('情侣课表');")
                database.execSQL("INSERT INTO CourseBaseBean (id, courseName, color, tableId) SELECT id, courseName, color, CASE WHEN tableName = '' THEN 1 ELSE 2 END FROM CourseBaseBean_old;")
                database.execSQL("CREATE INDEX index_CourseBaseBean_tableId ON CourseBaseBean (tableId ASC);")
                database.execSQL("DROP TABLE CourseBaseBean_old;")
                database.execSQL("DROP INDEX index_CourseDetailBean_id_tableName;")
                database.execSQL("ALTER TABLE CourseDetailBean RENAME TO CourseDetailBean_old;")
                database.execSQL("CREATE TABLE CourseDetailBean (\n" +
                        "  id INTEGER NOT NULL,\n" +
                        "  day INTEGER NOT NULL,\n" +
                        "  room TEXT,\n" +
                        "  teacher TEXT,\n" +
                        "  startNode INTEGER NOT NULL,\n" +
                        "  step INTEGER NOT NULL,\n" +
                        "  startWeek INTEGER NOT NULL,\n" +
                        "  endWeek INTEGER NOT NULL,\n" +
                        "  type INTEGER NOT NULL,\n" +
                        "  tableId INTEGER NOT NULL,\n" +
                        "  PRIMARY KEY (day, startNode, startWeek, type, tableId, id),\n" +
                        "  FOREIGN KEY (\"id\", \"tableId\") REFERENCES \"CourseBaseBean\" (\"id\", \"tableId\") ON DELETE CASCADE ON UPDATE CASCADE\n" +
                        ");")
                database.execSQL("INSERT INTO CourseDetailBean (id, day, room, teacher, startNode, step, startWeek, endWeek, type, tableId) SELECT id, day, room, teacher, startNode, step, startWeek, endWeek, type, CASE WHEN tableName = '' THEN 1 ELSE 2 END FROM CourseDetailBean_old;")
                database.execSQL("CREATE INDEX index_CourseDetailBean_id_tableId ON CourseDetailBean (id ASC, tableId ASC);")
                database.execSQL("DROP TABLE CourseDetailBean_old")

                database.execSQL("ALTER TABLE TimeDetailBean RENAME TO TimeDetailBean_old;")
                database.execSQL("CREATE TABLE TimeDetailBean (node INTEGER NOT NULL, startTime TEXT NOT NULL, endTime TEXT NOT NULL, timeTable INTEGER NOT NULL DEFAULT 1, PRIMARY KEY (node, timeTable), FOREIGN KEY (timeTable) REFERENCES TimeTableBean (id) ON DELETE CASCADE ON UPDATE CASCADE);")
                database.execSQL("INSERT INTO TimeDetailBean (node, startTime, endTime) SELECT node, startTime, endTime FROM TimeDetailBean_old;")
                database.execSQL("CREATE INDEX index_TimeDetailBean_id_timeTable ON TimeDetailBean(timeTable ASC);")
                database.execSQL("DROP TABLE TimeDetailBean_old;")
                database.execSQL("ALTER TABLE TimeTableBean ADD COLUMN sameLen INTEGER NOT NULL DEFAULT 1;")
                database.execSQL("ALTER TABLE TimeTableBean ADD COLUMN courseLen INTEGER NOT NULL DEFAULT 50;")
            }
        }
    }

    abstract fun courseDao(): CourseDao

    abstract fun appWidgetDao(): AppWidgetDao

    abstract fun timeTableDao(): TimeTableDao

    abstract fun timeDetailDao(): TimeDetailDao

    abstract fun tableDao(): TableDao
}

class _8to9Migration(val context: Context) : Migration(8, 9) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.beginTransaction()
        val cursor = database.query("SELECT * FROM TableBean")
        cursor.moveToFirst()
        val idIdx = cursor.getColumnIndex("id")
        val tableNameIdx = cursor.getColumnIndex("tableName")
        val nodesIdx = cursor.getColumnIndex("nodes")
        val backgroundIdx = cursor.getColumnIndex("background")
        val startDateIdx = cursor.getColumnIndex("startDate")
        val maxWeekIdx = cursor.getColumnIndex("maxWeek")
        val itemHeightIdx = cursor.getColumnIndex("itemHeight")
        val itemAlphaIdx = cursor.getColumnIndex("itemAlpha")
        val itemTextSizeIdx = cursor.getColumnIndex("itemTextSize")
        val widgetItemHeightIdx = cursor.getColumnIndex("widgetItemHeight")
        val widgetItemAlphaIdx = cursor.getColumnIndex("widgetItemAlpha")
        val widgetItemTextSizeIdx = cursor.getColumnIndex("widgetItemTextSize")
        val strokeColorIdx = cursor.getColumnIndex("strokeColor")
        val widgetStrokeColorIdx = cursor.getColumnIndex("widgetStrokeColor")
        val textColorIdx = cursor.getColumnIndex("textColor")
        val widgetTextColorIdx = cursor.getColumnIndex("widgetTextColor")
        val courseTextColorIdx = cursor.getColumnIndex("courseTextColor")
        val widgetCourseTextColorIdx = cursor.getColumnIndex("widgetCourseTextColor")
        val showSatIdx = cursor.getColumnIndex("showSat")
        val showSunIdx = cursor.getColumnIndex("showSun")
        val sundayFirstIdx = cursor.getColumnIndex("sundayFirst")
        val showOtherWeekCourseIdx = cursor.getColumnIndex("showOtherWeekCourse")
        val showTimeIdx = cursor.getColumnIndex("showTime")
        val typeIdx = cursor.getColumnIndex("type")

        while (cursor.count != 0) {
            val id = cursor.getInt(idIdx)
            val tableName = cursor.getString(tableNameIdx)
            val nodes = cursor.getInt(nodesIdx)
            val background = cursor.getString(backgroundIdx)
            val startDate = cursor.getString(startDateIdx)
            val maxWeek = cursor.getInt(maxWeekIdx)
            val itemHeight = cursor.getInt(itemHeightIdx)
            val itemAlpha = cursor.getInt(itemAlphaIdx)
            val itemTextSize = cursor.getInt(itemTextSizeIdx)
            val widgetItemHeight = cursor.getInt(widgetItemHeightIdx)
            val widgetItemAlpha = cursor.getInt(widgetItemAlphaIdx)
            val widgetItemTextSize = cursor.getInt(widgetItemTextSizeIdx)
            val strokeColor = cursor.getInt(strokeColorIdx)
            val widgetStrokeColor = cursor.getInt(widgetStrokeColorIdx)
            val textColor = cursor.getInt(textColorIdx)
            val widgetTextColor = cursor.getInt(widgetTextColorIdx)
            val courseTextColor = cursor.getInt(courseTextColorIdx)
            val widgetCourseTextColor = cursor.getInt(widgetCourseTextColorIdx)
            val showSat = cursor.getInt(showSatIdx)
            val showSun = cursor.getInt(showSunIdx)
            val sundayFirst = cursor.getInt(sundayFirstIdx)
            val showOtherWeekCourse = cursor.getInt(showOtherWeekCourseIdx)
            val showTime = cursor.getInt(showTimeIdx)
            val type = cursor.getInt(typeIdx)

            if (type == 1) {
                val todayWidgetCursor = database.query("SELECT * FROM AppWidgetBean WHERE detailType = 1")
                val wIdIdx = todayWidgetCursor.getColumnIndex("id")
                todayWidgetCursor.moveToFirst()
                while (todayWidgetCursor.count != 0) {
                    val wId = todayWidgetCursor.getInt(wIdIdx)
                    context.getPrefer("widget${wId}_config").edit(true) {
                        putInt("itemAlpha", widgetItemAlpha)
                        putInt("itemTextSize", widgetItemTextSize)
                        putInt("strokeColor", widgetStrokeColor)
                        putInt("textColor", widgetTextColor)
                        putInt("courseTextColor", widgetCourseTextColor)
                        putString("tableIds", "$id")

                        putBoolean(Const.KEY_APPWIDGET_BG, context.getPrefer().getBoolean(Const.KEY_APPWIDGET_BG, false))
                        putInt(Const.KEY_APPWIDGET_BG_COLOR, context.getPrefer().getInt(Const.KEY_APPWIDGET_BG_COLOR, 0x80FFFFFF.toInt()))
                    }
                    if (!todayWidgetCursor.moveToNext()) break
                }
                todayWidgetCursor.close()
                context.getPrefer().edit {
                    putInt(Const.KEY_SHOW_TABLE_ID, id)
                }
            }

            val weekWidgetCursor = database.query("SELECT * FROM AppWidgetBean WHERE info = '${id}'")
            val weekIdIdx = weekWidgetCursor.getColumnIndex("id")
            weekWidgetCursor.moveToFirst()
            while (weekWidgetCursor.count != 0) {
                val weekId = weekWidgetCursor.getInt(weekIdIdx)
                context.getPrefer("widget${weekId}_config").edit(true) {
                    putInt("itemAlpha", widgetItemAlpha)
                    putInt("itemTextSize", widgetItemTextSize)
                    putInt("strokeColor", widgetStrokeColor)
                    putInt("textColor", widgetTextColor)
                    putInt("courseTextColor", widgetCourseTextColor)
                    putInt("tableId", id)
                    putInt("itemHeight", widgetItemHeight)
                    putBoolean("showSat", showSat == 1)
                    putBoolean("showSun", showSun == 1)
                    putBoolean("sundayFirst", sundayFirst == 1)
                    putBoolean("showOtherWeekCourse", showOtherWeekCourse == 1)
                    putBoolean("showTime", showTime == 1)
                    putBoolean(Const.KEY_SCHEDULE_TEACHER, context.getPrefer().getBoolean(Const.KEY_SCHEDULE_TEACHER, true))
                    putBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, context.getPrefer().getBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, true))
                    putBoolean(Const.KEY_APPWIDGET_BG, context.getPrefer().getBoolean(Const.KEY_APPWIDGET_BG, false))
                    putInt(Const.KEY_APPWIDGET_BG_COLOR, context.getPrefer().getInt(Const.KEY_APPWIDGET_BG_COLOR, 0x80FFFFFF.toInt()))
                }
                if (!weekWidgetCursor.moveToNext()) break
            }
            weekWidgetCursor.close()

            context.getPrefer("table${id}_config").edit(true) {
                putString("tableName", tableName)
                putInt("nodes", nodes)
                putString("background", background)
                putString("startDate", startDate)
                putInt("maxWeek", maxWeek)
                putInt("itemHeight", itemHeight)
                putInt("itemAlpha", itemAlpha)
                putInt("itemTextSize", itemTextSize)
                putInt("strokeColor", strokeColor)
                putInt("textColor", textColor)
                putInt("courseTextColor", courseTextColor)
                putBoolean("showSat", showSat == 1)
                putBoolean("showSun", showSun == 1)
                putBoolean("sundayFirst", sundayFirst == 1)
                putBoolean("showOtherWeekCourse", showOtherWeekCourse == 1)
                putBoolean("showTime", showTime == 1)
                putBoolean(Const.KEY_SCHEDULE_TEACHER, context.getPrefer().getBoolean(Const.KEY_SCHEDULE_TEACHER, true))
                putBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, context.getPrefer().getBoolean(Const.KEY_SCHEDULE_DETAIL_TIME, true))
            }
            if (!cursor.moveToNext()) break
        }
        cursor.close()
        // throw Exception()
        database.endTransaction()
    }

}