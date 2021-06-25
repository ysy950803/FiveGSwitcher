# FiveGSwitcher

### 温馨提示

最新版本已在[酷安](https://www.coolapk.com/apk/280295)认领并发布。

近期发现网络上不少同学搬运我的成果，不管是[B站视频](https://www.bilibili.com/video/BV1mX4y1M77n)还是各种软件[下载站](https://www.baidu.com/link?url=kMfjWYWVzYz-IQbSMcRJp_6X0siaOPVatyqIx8WQNnxtmF-lvm9vZZwy4O0YxIhoNgl1pNa2PXzFrljuSb0P7K&wd=&eqid=acbc6524000172a6000000026059c566)，都没有注明来源。分享是好事，本来这也是开源的东西，而且也不算什么复杂的玩意，希望大家转载出去还是写一下来源，哪怕带个我的GitHub或者博客地址也好。下面这篇技术分析我只在[CSDN](https://blog.csdn.net/ysy950803)、[掘金](https://juejin.cn/post/6860735856861773832)、[少数派](https://sspai.com/post/62394)、[百度贴吧](https://tieba.baidu.com/p/6751513821)和我的[个人网站](https://blog.ysy950803.top/2020/08/13/%E7%BB%99MIUI%E5%BC%80%E5%8F%91%E4%B8%80%E4%B8%AA5G%E5%BF%AB%E6%8D%B7%E5%BC%80%E5%85%B3/)发布过，除此之外其余地方都非本人。

### 通知栏增加5G开关

在通知栏编辑模式中长按“5G开关”拖动到上方即可使用，弥补5G手机没有快捷开关的问题。

### 支持ADB Shell命令

方便ROOT权限的设备运行自动化脚本。[issue#2](https://github.com/ysy950803/FiveGSwitcher/issues/2)

```shell
# 开启5G，关闭则为false
am start -n com.ysy.fivegswitcher/.MainActivity --ez enable_5g true
```

### 技术分析

本项目涉及MIUI系统调用逆向分析，具体细节可见：
[给MIUI开发一个5G快捷开关](https://blog.csdn.net/ysy950803/article/details/107975344)

### FAQ

**0、其他厂商ROM可以使用吗？**

此工具只能MIUI系统使用哦，谢谢各位的支持。

**1、添加开关后不可用（灰色状态）？**

目前发现在MIUI 12和12.5稳定版部分机型上有这个问题，会导致快捷方式TileService被系统杀死并重启（我尝试了MIUI开发版和原生Android就不会），暂时没有百分百的解决办法。可尝试开启本应用的自启动权限，并重新添加快捷开关到你通知中心的第一页（即下拉后第一眼就能看见的位置），最后重启手机。

**2、点击开关后，图标状态没有改变（没反应）或者很久才响应？**

问题本质同1。

**3、长按5G开关后，无法跳转到设置页面？**

请在应用的权限设置中允许后台弹出界面或开启自启动。
