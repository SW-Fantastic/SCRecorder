# 屏幕录像工具

新坑一个，基于java和ffmpeg的。

ui还是FXApplication的项目，基于高版本jdk。这里ffmpeg直接引用bytedeco的话
会有一个问题，因为不能从两个模块读取同名的package，将会导致编译失败。

所以我将ffmpeg的包进行重新打包了，就在libs里面，但是不能github不能上传超过100m的文件
所以他被拆分成了2个zip的分卷，直接使用7zip或者其他压缩软件解压然后放入libs目录即可。

