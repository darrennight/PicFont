PicFont
=======

图片字生成器，即点阵字库生成

将字符图片压缩为二进制数据，使用时再用来生成图片。
用来在不同的移动设备上统一字符风格，制作额外的美术风格，如包边字等。

数据结构为：
byte  byte  byte  byte  [-----------byte-------------] <--- REPEAT
   4byte开头为字的索引	   压缩后的的像素数据串数据
    UTF-8最宽最byte        每个byte表示8个为上的像素
