# FiveGSwitcher

### 下载

<a href='https://play.google.com/store/apps/details?id=com.ysy.switcherfiveg&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'><img alt='Get it on Google Play' src='https://raw.githubusercontent.com/ysy950803/FiveGSwitcher/master-3/google-play-badge.png'/></a>

### 添加快捷开关到MIUI通知中心

进入通知中心的编辑模式，您可以在底部“未添加开关”中找到“5G开关”。添加完成后，长按可以打开详细设置。

### 支持ADB Shell命令

方便ROOT权限的设备运行自动化脚本：[issue#2](https://github.com/ysy950803/FiveGSwitcher/issues/2)

```shell
# true表示开启5G，关闭则为false
am start -n com.ysy.switcherfiveg/.MainActivity --ez enable_5g true
```

### FAQ

**0、其他厂商ROM可以使用吗？**

此工具只能MIUI系统使用哦，谢谢各位的支持。

**1、已经是MIUI系统，但添加开关后不可用（灰色状态）？**

目前在新版MIUI稳定版部分机型上可能会有这个问题，快捷方式TileService被系统杀死并重启（我尝试了MIUI开发版和原生Android就不会），暂时没有百分百的解决办法。可尝试开启本应用的自启动权限（一般不建议），并重新添加快捷开关到你通知中心的第一页（即下拉后第一眼就能看见的位置），最后重启手机。

**2、点击开关后，图标状态没有改变（没反应）或者很久才响应？**

大致原因和上述第1点类似，MIUI在TileService这块大概率有魔改，自定义的快捷方式各种回调不稳定，目前在基于Android 12的MIUI系统上问题比较明显，所以本应用增加了长按后的强制开关来兜底。
