package com.example.time.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.time.ui.components.*
import com.example.time.ui.theme.*
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

/**
 * Onboarding Screen - 引导页面
 * 严格按照品牌设计规范实现
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState()
    val pages = remember { OnboardingPage.pages }
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = BrandGradientBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Logo 和跳过按钮
            TopBar(
                onSkip = onComplete,
                modifier = Modifier.padding(Spacing.ExtraLarge)
            )
            
            // ViewPager
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = pages[page],
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 底部指示器和按钮
            BottomSection(
                pagerState = pagerState,
                pageCount = pages.size,
                onNext = {
                    if (pagerState.currentPage < pages.size - 1) {
                        // 下一页
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        // 完成引导
                        onComplete()
                    }
                },
                modifier = Modifier.padding(Spacing.ExtraLarge)
            )
        }
    }
}

/**
 * 顶部栏 - Logo 和跳过按钮
 */
@Composable
private fun TopBar(
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(ContainerSize.LogoSize)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WatchLater,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
        
        // 跳过按钮
        TextButton(onClick = onSkip) {
            Text(
                text = "跳过",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 引导页内容
 */
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = Spacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(CornerRadius.ExtraLarge))
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = page.title,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(Spacing.Huge))
        
        // 标题
        Text(
            text = page.title,
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Spacing.Large))
        
        // 描述
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f)
        )
    }
}

/**
 * 底部区域 - 指示器和按钮
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
private fun BottomSection(
    pagerState: PagerState,
    pageCount: Int,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 页面指示器
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = Spacing.ExtraSmall)
                        .width(if (isSelected) 32.dp else 8.dp)
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else Color.White.copy(alpha = 0.3f)
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
        
        // 继续按钮
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = BrandBlue
            ),
            shape = RoundedCornerShape(CornerRadius.Small)
        ) {
            Text(
                text = if (pagerState.currentPage == pageCount - 1) "开始使用" else "继续",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 引导页数据
 */
data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String
) {
    companion object {
        val pages = listOf(
            OnboardingPage(
                icon = Icons.Default.WatchLater,
                title = "了解时间去向",
                description = "精确记录每个应用的使用时长，让您清楚知道时间都花在了哪里。支持毫秒级精度统计。"
            ),
            OnboardingPage(
                icon = Icons.Default.Analytics,
                title = "可视化数据分析",
                description = "通过精美的图表和热力图，直观展示您的使用习惯和趋势，帮助您发现时间管理的机会。"
            ),
            OnboardingPage(
                icon = Icons.Default.Notifications,
                title = "智能提醒助手",
                description = "设置使用时长提醒和休息提醒，帮助您保持健康的手机使用习惯，避免过度沉迷。"
            ),
            OnboardingPage(
                icon = Icons.Default.Lock,
                title = "隐私安全保护",
                description = "所有数据都使用加密存储在本地，绝不上传到服务器。您的隐私由您完全掌控。"
            )
        )
    }
}

