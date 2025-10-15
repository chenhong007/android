package com.example.time.ui.charts

import com.example.time.ui.charts.data.ChartDataProvider
import com.example.time.ui.charts.models.*
import com.example.time.data.model.AppUsageSummary
import com.example.time.data.model.DailyUsageSummary
import com.example.time.data.model.UsageTracking
import com.example.time.data.repository.UsageRepository
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import io.mockk.*
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.ZoneId

class ChartDataProviderTest {
    
    private lateinit var chartDataProvider: ChartDataProvider
    private lateinit var mockRepository: UsageRepository
    
    @Before
    fun setup() {
        mockRepository = mockk()
        chartDataProvider = ChartDataProvider(mockRepository)
    }
    
    @Test
    fun testGetAppUsageRanking() = runTest {
        // 创建测试数据
        val testData = listOf(
            AppUsageSummary(
                packageName = "com.example.app1",
                appName = "App 1",
                category = "SOCIAL",
                totalDuration = 3600000,
                sessionCount = 30,
                lastUsedTimestamp = System.currentTimeMillis()
            ),
            AppUsageSummary(
                packageName = "com.example.app2",
                appName = "App 2",
                category = "ENTERTAINMENT",
                totalDuration = 7200000,
                sessionCount = 20,
                lastUsedTimestamp = System.currentTimeMillis()
            )
        )
        
        val startTime = System.currentTimeMillis() - 86400000
        val endTime = System.currentTimeMillis()
        
        coEvery { mockRepository.getAppUsageSummaries(startTime, endTime) } returns testData
        
        val result = chartDataProvider.getAppUsageRanking(startTime, endTime, 5, null)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        assertEquals(2, result.data?.entries?.size)
        
        coVerify { mockRepository.getAppUsageSummaries(startTime, endTime) }
    }
    
    @Test
    fun testGetCalendarHeatmapData() = runTest {
        val startDate = LocalDate.now().minusDays(7)
        val endDate = LocalDate.now()
        
        val testData = listOf(
            DailyUsageSummary(
                date = startDate.toString(),
                totalDuration = 3600000,
                unlockCount = 30,
                screenOnDuration = 3400000,
                topApps = emptyList()
            )
        )
        
        val startTime = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        coEvery { mockRepository.getDailyUsageSummaries(startTime, endTime) } returns testData
        
        val result = chartDataProvider.getCalendarHeatmapData(startDate, endDate, null)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        
        coVerify { mockRepository.getDailyUsageSummaries(startTime, endTime) }
    }
    
    @Test
    fun testGetTimelineSessions() = runTest {
        val date = LocalDate.now()
        val startTime = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endTime = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val testRecords = listOf(
            UsageTracking(
                id = 1,
                packageName = "com.example.app1",
                appName = "App 1",
                category = "SOCIAL",
                sessionId = "session1",
                timestamp = startTime,
                endTimestamp = startTime + 3600000,
                duration = 3600000
            )
        )
        
        coEvery { mockRepository.getUsageInTimeRange(startTime, endTime) } returns testRecords
        
        val result = chartDataProvider.getTimelineSessions(date, null)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        
        coVerify { mockRepository.getUsageInTimeRange(startTime, endTime) }
    }
    
    @Test
    fun testGetTimeSeriesData() = runTest {
        val startTime = System.currentTimeMillis() - 7 * 86400000
        val endTime = System.currentTimeMillis()
        
        val testData = listOf(
            DailyUsageSummary(
                date = LocalDate.now().minusDays(1).toString(),
                totalDuration = 3600000,
                unlockCount = 30,
                screenOnDuration = 3400000,
                topApps = emptyList()
            )
        )
        
        coEvery { mockRepository.getDailyUsageSummaries(startTime, endTime) } returns testData
        
        val result = chartDataProvider.getTimeSeriesData(startTime, endTime, TimeGranularity.DAY, null)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        
        coVerify { mockRepository.getDailyUsageSummaries(startTime, endTime) }
    }
    
    @Test
    fun testGetComparisonData() = runTest {
        val startTime1 = System.currentTimeMillis() - 14 * 86400000
        val endTime1 = System.currentTimeMillis() - 7 * 86400000
        val startTime2 = System.currentTimeMillis() - 7 * 86400000
        val endTime2 = System.currentTimeMillis()
        
        val timeRange1 = DateRange(startTime1, endTime1)
        val timeRange2 = DateRange(startTime2, endTime2)
        
        val testData = listOf(
            DailyUsageSummary(
                date = LocalDate.now().minusDays(1).toString(),
                totalDuration = 3600000,
                unlockCount = 30,
                screenOnDuration = 3400000,
                topApps = emptyList()
            )
        )
        
        coEvery { mockRepository.getDailyUsageSummaries(any(), any()) } returns testData
        
        val result = chartDataProvider.getComparisonData(timeRange1, timeRange2, ComparisonType.TIME_PERIOD, null)
        
        assertTrue(result.success)
        assertNotNull(result.data)
        
        coVerify(atLeast = 1) { mockRepository.getDailyUsageSummaries(any(), any()) }
    }
}