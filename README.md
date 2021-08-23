Java爬虫常用工具类

针对爬虫经常遇到的一些问题，设计的一个常用类。

下载文件，NetworkFileUtils类简单示例  
NetworkFileUtils.connect(url)  
.filename("QQ")  //设置文件名，文件名会替换非法字符，不设置会自动获取文件名   
.retry(4，1000)  //重试次数，以及重试等待间隔  
.errorExit(true)  //下载失败抛出执行异常  
.download(folder); //设置存放的文件夹

对jsoup专门设计了一个类，使其可以进行重试，简单示例  
Document doc = JsoupUtils.connect(url)
.timeout(1000)  
.proxy(proxyHost, proxyPort)  
.retry(MAX_RETRY, MILLISECONDS_SLEEP)  //重试次数，以及重试等待间隔  
.errorExit(errorExit)  //抛出执行异常  
.GetDocument();

对应文件读写ReadWriteUtils类，简单示例  
String str = ReadWriteUtils.orgin(filePath).text(); //读取文件文本  
List<String> lists = ReadWriteUtils.orgin(filePath).list(); //按行读取文件文本  
ReadWriteUtils.orgin(filePath).text(str); //字符串按行写入文本

对于调用Aria2的Aria2Utils类，简单示例  
Aria2Utils.connect("127.0.0.1", 66553)  //地址以及端口  
.addUrl("https:\\www.qq.com")  //添加url  
.setToken("12345")  //设置token  
.setProxy(); //为所有链接添加代理  
.send(); //get()、post()
