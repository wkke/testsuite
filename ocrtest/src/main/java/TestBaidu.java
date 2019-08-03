import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class TestBaidu {
//设置APPID/AK/SK
public static final String APP_ID = "16911857";
public static final String API_KEY = "X6Ve8FKFQs8foNTYUtFjtmpr";
public static final String SECRET_KEY = "Zgo1Wga9cFyRpmI8X5CiUDSn64T2HZTe";
public static void main(String[] args) throws IOException {

    HashMap<String, String> options = new HashMap<String, String>();
    // 初始化一个AipOcr
    AipOcr client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);

    // 可选：设置网络连接参数
    client.setConnectionTimeoutInMillis(2000);
    client.setSocketTimeoutInMillis(60000);

    // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
  //  client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
  //  client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理

    // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
    // 也可以直接通过jvm启动参数设置此环境变量
    System.setProperty("aip.log4j.conf", "log4j.properties");

    // 调用接口
    String path = "d://code//123.jpg";
    byte[] file = getContent(path);
    JSONObject res = client.form(file, options);

  //  JSONObject res = client.form(path, new HashMap<String, String>());
    System.out.println(res.toString(2));
}

public static byte[] getContent(String filePath) throws IOException {
    File file = new File(filePath);
    long fileSize = file.length();
    if (fileSize > Integer.MAX_VALUE) {
        System.out.println("file too big...");
        return null;
    }
    FileInputStream fi = new FileInputStream(file);
    byte[] buffer = new byte[(int) fileSize];
    int offset = 0;
    int numRead = 0;
    while (offset < buffer.length
            && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
        offset += numRead;
    }
    // 确保所有数据均被读取
    if (offset != buffer.length) {
        throw new IOException("Could not completely read file "
                + file.getName());
    }
    fi.close();
    return buffer;
}
}
