Java爬虫常用工具类

使用方法:  
控制台命令:mvn clean install

日常针对爬虫经常遇到的一些问题，设计的一个常用类。

下载文件，简单示例:  
NetworkFileUtils.connect(url)  
.filename("QQ")  //设置文件名，文件名会替换非法字符，不设置会自动获取文件名   
.retry(4，1000)  //重试次数，以及重试等待间隔  
.multithread(10)  //多线程下载,无法获取文件大小转为全量下载,默认线程16  
.interval(100)  //多线程异步间隔,同步访问网址会丢失数据,不应低于36  
.errorExit(true)  //下载失败抛出执行异常  
.download(folder); //设置存放的文件夹

一些网络访问类，使其可以进行重试  
JsoupUtils HttpsUtils HtmlUnitUtils  
简单示例:  
Document doc = JsoupUtils.connect(url)
.proxy(proxyHost, proxyPort)  
.retry(MAX_RETRY, MILLISECONDS_SLEEP)  //重试次数，重试等待间隔   
.get(); // post()  
HtmlUnitUtils默认可运行JS最大1秒 - waitJSTime方法修改

调用Aria2，简单示例:  
Aria2Utils.connect("127.0.0.1", 6800)  //地址以及端口  
.addUrl(url)  //添加url  
.setToken("12345")  //设置token  
.setProxy(); //为所有链接添加代理  
.send(); //get()、post()

文件读写，简单示例:  
String str = ReadWriteUtils.orgin(filePath).text(); //读取文件文本  
List<String> lists = ReadWriteUtils.orgin(filePath).list(); //按行读取文件文本  
ReadWriteUtils.orgin(filePath).text(str); //字符串按行写入文本 ReadWriteUtils.orgin(file).copy(out); // 文件复制

文件压缩，简单示例:  
ZipUtils.origin(file).out(qq_tempfile).addFiles(file);  
文件解压，简单示例:  
ZipUtils.origin(file).charset("GBK").deCompress(temppath);

获取本地浏览器cookie  
//home方法,默认win版edge用户路径,其它浏览器添加参数,路径至User Data目录  
Map<String, String> cookies = LocalCookies.home()
.getCookiesForDomain("pixiv.net"); //获取对应域  
获取本地浏览器LoginData(账号和密码)  
Map<String, String> cookies = LoginData.home()
.getLoginDatasForDomain("pixiv.net");