package com.example.time.utils

import android.content.Context
import android.content.pm.PackageManager

/**
 * 应用名称映射器
 * 用于将包名转换为友好的中文名称
 */
object AppNameMapper {
    
    /**
     * 常用应用包名到中文名称的映射表
     */
    private val APP_NAME_MAP = mapOf(
        // 社交通讯
        "com.tencent.mm" to "微信",
        "com.tencent.mobileqq" to "QQ",
        "com.tencent.tim" to "TIM",
        "com.alibaba.android.rimet" to "钉钉",
        "com.sina.weibo" to "微博",
        "com.tencent.wework" to "企业微信",
        
        // 短视频
        "com.ss.android.ugc.aweme" to "抖音",
        "com.smile.gifmaker" to "快手",
        "com.tencent.qqlive" to "腾讯视频",
        "com.youku.phone" to "优酷",
        "tv.danmaku.bili" to "哔哩哔哩",
        "com.bilibili.app.in" to "哔哩哔哩国际版",
        
        // 购物
        "com.taobao.taobao" to "淘宝",
        "com.jingdong.app.mall" to "京东",
        "com.xunmeng.pinduoduo" to "拼多多",
        "com.tmall.wireless" to "天猫",
        "com.suning.mobile.ebuy" to "苏宁易购",
        "com.vipshop.android" to "唯品会",
        
        // 外卖
        "com.ele.me" to "饿了么",
        "com.sankuai.meituan" to "美团",
        "com.baidu.lbs.waimai" to "百度外卖",
        
        // 出行
        "com.sdu.didi.psnger" to "滴滴出行",
        "com.tencent.map" to "腾讯地图",
        "com.baidu.BaiduMap" to "百度地图",
        "com.autonavi.minimap" to "高德地图",
        "com.mobike.mobikeapp" to "摩拜单车",
        
        // 音乐
        "com.tencent.qqmusic" to "QQ音乐",
        "com.netease.cloudmusic" to "网易云音乐",
        "com.kugou.android" to "酷狗音乐",
        "com.kuwo.player" to "酷我音乐",
        
        // 阅读
        "com.tencent.reading" to "微信读书",
        "com.jingdong.app.reader" to "京东读书",
        "com.duokan.reader" to "多看阅读",
        "com.chaozh.iReader" to "掌阅",
        "com.zhihu.android" to "知乎",
        
        // 新闻资讯
        "com.ss.android.article.news" to "今日头条",
        "com.tencent.news" to "腾讯新闻",
        "com.netease.newsreader.activity" to "网易新闻",
        
        // 工具
        "com.android.chrome" to "Chrome",
        "com.UCMobile" to "UC浏览器",
        "com.qihoo.browser" to "360浏览器",
        "com.tencent.mtt" to "QQ浏览器",
        "com.eg.android.AlipayGphone" to "支付宝",
        "com.tencent.android.qqdownloader" to "应用宝",
        "com.ss.android.article.master" to "今日头条极速版",
        
        // 游戏
        "com.tencent.tmgp.sgame" to "王者荣耀",
        "com.tencent.tmgp.pubgmhd" to "和平精英",
        "com.miHoYo.Yuanshen" to "原神",
        "com.netease.dwrg" to "第五人格",
        
        // 系统应用
        "com.android.settings" to "设置",
        "com.android.camera2" to "相机",
        "com.android.gallery3d" to "相册",
        "com.android.mms" to "短信",
        "com.android.contacts" to "联系人",
        "com.android.dialer" to "电话",
        "com.android.browser" to "浏览器",
        "com.android.vending" to "Google Play",
        
        // 小米系统应用
        "com.miui.home" to "桌面",
        "com.xiaomi.market" to "小米应用商店",
        "com.miui.gallery" to "相册",
        "com.android.fileexplorer" to "文件管理",
        
        // 华为系统应用
        "com.huawei.appmarket" to "华为应用市场",
        "com.huawei.browser" to "华为浏览器",
        
        // OPPO系统应用
        "com.oppo.market" to "OPPO软件商店",
        "com.coloros.filemanager" to "文件管理",
        
        // vivo系统应用
        "com.vivo.appstore" to "vivo应用商店",
        "com.vivo.browser" to "vivo浏览器",
        
        // 办公
        "com.tencent.wetype" to "腾讯文档",
        "com.microsoft.office.word" to "Word",
        "com.microsoft.office.excel" to "Excel",
        "com.microsoft.office.powerpoint" to "PowerPoint",
        "cn.wps.moffice_eng" to "WPS Office"
    )
    
    /**
     * 获取应用的中文名称
     * 
     * 优先级：
     * 1. 如果传入的 appName 不是包名格式，直接返回
     * 2. 从映射表中查找
     * 3. 从系统获取应用标签
     * 4. 返回包名
     * 
     * @param context Android上下文
     * @param packageName 应用包名
     * @param appName 数据库中的应用名称（可能是中文名或包名）
     * @return 友好的应用名称
     */
    fun getAppName(context: Context, packageName: String, appName: String): String {
        // 1. 如果 appName 不像是包名（不包含.或已经是中文），直接返回
        if (!appName.contains(".") || appName != packageName) {
            return appName
        }
        
        // 2. 从映射表中查找
        APP_NAME_MAP[packageName]?.let { return it }
        
        // 3. 尝试从系统获取应用标签
        try {
            val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            val label = context.packageManager.getApplicationLabel(applicationInfo).toString()
            if (label != packageName) {
                return label
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // 应用未安装，继续使用原名称
        }
        
        // 4. 返回原名称（可能是包名）
        return appName
    }
    
    /**
     * 批量转换应用名称
     */
    fun convertAppNames(context: Context, apps: List<com.example.time.data.model.AppUsageSummary>): List<com.example.time.data.model.AppUsageSummary> {
        return apps.map { app ->
            app.copy(
                appName = getAppName(context, app.packageName, app.appName)
            )
        }
    }
}

