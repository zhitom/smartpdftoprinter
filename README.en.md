# smartpdftoprinter

#### 介绍

[pdf auto printer scaling page]
对批量的pdf文件进行打印操作，可以配置打印机相关信息： 页面大小、边距大小、缩放比率、横版竖版等，打印的每个job任务可以配置等待时间或打印机等待队列最大大小，避免打印机被打爆。

#### 软件架构
使用pdfbox完成pdf文件的自动化打印操作

#### 安装教程

需要安装JDK1.8+

#### 使用说明

二进制下载地址： [smartpdftoprinter-1.0.0.jar](https://gitee.com/smartsnow/smartpdftoprinter/attach_files/861649/download/smartpdftoprinter-1.0.0.jar)

```bash
#配置文件独立时的运行：
java -jar target/smartpdftoprinter-1.0.0.jar --spring.config.location=src/main/resources/application.yml
#配置文件打包在jar里边的运行
java -jar target/smartpdftoprinter-1.0.0.jar
```

#### 参与贡献


#### TODO


