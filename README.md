Java常用工具类

时间：2021/4/24 14:27  
-NetworkFileUtils  
--修复文件下载未获取全部数据却返回成功的BUG

时间：2021/4/19 21:56  
-HtmlUnitUtils  
-HttpsUtils  
-JsonUtils  
--为Network工具类构造参数添加accept-encoding请求头属性

时间：2021/4/18 15:34  
-ReadWriteUtils  
--增加二进制文件读写

时间：2021/4/18 15:22  
-NetworkFileUtils  
--改进down文件配置参数  
--使用down文件可以直接断点续传

时间：2021/4/18 4:18  
-NetworkFileUtils  
--修改为多线程下载  
--下载项增加断点续传  
--改进文件名获取方式  
--增加md5验证文件完整性

时间：2021/4/15 3:27  
-ReadWriteUtils  
--将ReadFilesInfo和WriteFilesInfo合并并修改为工具类  
--增加RandomAccessFile、FileChannel、MappedByteBuffer读写方式  
--增加FileChannel、MappedByteBuffer方式文件复制，支持文件合并  
