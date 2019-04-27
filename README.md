# 实现一些有点意思的东东
## 功能一：在指定目录下，将文件的指定名字改为自己想要的名字
包：com.pdc.updatefilename
### 配置文件解释
```properties
Director=G:\\Test #目录
OldName=your #要替换的名字
NewName=My #信名字
IsRecursionReadDirector=true #如果此目录下还有目录，是否递归执行
```
使用者：
```java
Start.start();
```
### 亮点

> 使用KMP算法，可使用BM算法、Sunday算法代替：https://www.cnblogs.com/cherryljr/p/6519748.html

## 功能二：JSON解析器

包：com.pdc.jsonparser
### 步骤
#### 1.对于输入的JSON字符串，解析成token流
#### 2.根据token流解析成对应的JSON对象或JSON数组
##### 类顺序简介(详解可看代码)

1.TokenType：枚举类，保存解析的常量

2.Token：存储每一个token对应的值，映射成tokenType和value

3.CharReader：在解析的过程中通过字符流来不断的读取字符，并且需要经常根据相应的字符来判断状态的跳转。所以封装一个ReaderChar类，以便更好地操作字符流。

4.TokenStore：存储解析出来的token流

5.exception：自定义异常，方便debug

6.Tokenizer：JSON词法分析器，解析所有的token，并存到list中

7.JSONArray和JSONObject：实体类

8.Parser：把token解析成实体类

9.JSONParser：解析入口类，方法为fromJSON

10.FormatUtil：规范输出

11.HttpUtil：从网络上获取JSON串

### 重难点和相关bug：

>1.对特殊字符的处理，如转义字符，unicode编码的字符等,JSON中允许出现的转义字符有以下九种：\"  \\  \b  \f  \n  \r  \t  \u  \/  , 处理Unicode编码时要特别注意一下u的后面会出现四位十六进制数 , 当读取到一个双引号或者读取到了非法字符（’\r’或’、’\n’）循环退出；
>
>2.对数字的处理，如frac、exp等(此处注意有个bug，0.xxx需要先得到第一个x，后面的才能循环添加)；
>
>3.next和back需要配合好同时出现，不然会出莫名奇妙的bug(控制思路：每次拼接完，用back保持最后一个已读取的字符；每次开始拼接，用next获取第一个还没读取的数)
>
>4.parseJsonObject中，需要考虑每种情况(如复杂Json，嵌套对象/数组)，不能只考虑常规情况(一套键值对)，如{的出现，需要递归，目的是为了达到健壮性
>
>5.idea的注释出现\u之类的转义字符时，要用\\\ u，不然报错
>
>6.关于HttpURLConnection：https://www.cnblogs.com/hotsmile/p/7286845.html

### 亮点：

> 1.使用位运算加快速率
>
> 2.每次case后更新expectToken,使用checkExpectToken方法来判断tokenType，增加了代码简洁性和复用率，思路跟next和back的配合一样

### Parser中对于Json格式解析总结：

​             <font color="red"> JsonObject </font>                                                                            <font color="red"> JsonArray </font>       

第一个字符为{，希望遇到" }-----------------------------------------第一个字符为[,希望遇到[ ] { } null 数字 boolean 字符串

{希望遇到，}------------------------------------------------------------- {希望遇到，]                                                                                        

[希望遇到，}--------------------------------------------------------------[希望遇到，]

null希望遇到，}----------------------------------------------------------null希望遇到，]

数字希望遇到， }--------------------------------------------------------数字希望遇到，]

boolean希望遇到， }---------------------------------------------------boolean希望遇到，]

字符串作为值时，希望遇到， }                                                                    
--------------------------------------------------------------------------------字符串希望遇到，]

字符串作为键时，希望遇到，

:希望遇到null 数字 boolean 字符串 { [

,希望遇到字符串----------------------------------------------------------,希望遇到字符串 null 数字 boolean [ {

}则返回----------------------------------------------------------------------]则返回

END_DOCUMENT则返回----------------------------------------------END_DOCUMENT则返回

### 调用：

```java
Object object = Start.parseJson(json);
```

### 参考资料：

https://gyl-coder.top/JSONParser/

https://yq.aliyun.com/articles/379553

http://tieba.baidu.com/p/385743053

https://baike.baidu.com/item/%E7%A7%91%E5%AD%A6%E8%AE%B0%E6%95%B0%E6%B3%95/1612882?fromtitle=%E7%A7%91%E5%AD%A6%E8%AE%A1%E6%95%B0%E6%B3%95&fromid=756685&fr=aladdin

https://www.cnblogs.com/smilefortoday/p/4021782.html





