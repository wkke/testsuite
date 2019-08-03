import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.opencv.presets.opencv_core;
import org.bytedeco.tesseract.TessBaseAPI;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

import static org.bytedeco.leptonica.global.lept.*;

public class TestCv {

String test_file_path = "D:\\code\\testsuite\\ocrtest";

public  TessBaseAPI api;

static {
    //加载动态链接库时，不使用System.loadLibrary(xxx);。 而是使用 绝对路径加载：System.load(xxx);

    /*
     * 加载动态库
     *
     * 第一种方式 --------------System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
     * loadLibrary(Core.NATIVE_LIBRARY_NAME); //使用这种方式加载，需要在 IDE 中配置参数.
     * Eclipse 配置：http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-eclipse
     * IDEA 配置 ：http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-other-ides-experimental
     *
     * 第二种方式 --------------System.load(path of lib);
     * System.load(your path of lib) ,方式比较灵活，可根据环境的系统，位数，决定加载内容
     */
    loadLibraries();


}


public static void main(String[] args) {
    TestCv tv=new TestCv();
    tv.initOcr();
    tv.readTable();
    tv.disdroyOcr();

}
public void readTable(){

    Mat source_image = Imgcodecs.imread(test_file_path + "/123.jpg");
    //灰度处理
    Mat gray_image = new Mat(source_image.height(), source_image.width(), CvType.CV_8UC1);
    Imgproc.cvtColor(source_image,gray_image,Imgproc.COLOR_RGB2GRAY);

    //二值化
    Mat thresh_image = new Mat(source_image.height(), source_image.width(), CvType.CV_8UC1);
    // C 负数，取反色，超过阈值的为黑色，其他为白色
    Imgproc.adaptiveThreshold(gray_image, thresh_image,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7,-2);
    this.saveImage("out-table/1-thresh.png",thresh_image);

    //克隆一个 Mat，用于提取水平线
    Mat horizontal_image = thresh_image.clone();

    //克隆一个 Mat，用于提取垂直线
    Mat vertical_image = thresh_image.clone();

    /*
     * 求水平线
     * 1. 根据页面的列数（可以理解为宽度），将页面化成若干的扫描区域
     * 2. 根据扫描区域的宽度，创建一根水平线
     * 3. 通过腐蚀、膨胀，将满足条件的区域，用水平线勾画出来
     *
     * scale 越大，识别的线越多，因为，越大，页面划定的区域越小，在腐蚀后，多行文字会形成一个块，那么就会有一条线
     * 在识别表格时，我们可以理解线是从页面左边 到 页面右边的，那么划定的区域越小，满足的条件越少，线条也更准确
     */
    int scale = 10;
    int horizontalsize = horizontal_image.cols() / scale;
    // 为了获取横向的表格线，设置腐蚀和膨胀的操作区域为一个比较大的横向直条
    Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalsize, 1));
    // 先腐蚀再膨胀 new Point(-1, -1) 以中心原点开始
    // iterations 最后一个参数，迭代次数，越多，线越多。在页面清晰的情况下1次即可。
    Imgproc.erode(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1),1);
    Imgproc.dilate(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1),1);
    this.saveImage("out-table/2-horizontal.png",horizontal_image);

    // 求垂直线
    scale = 30;
    int verticalsize = vertical_image.rows() / scale;
    Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalsize));
    Imgproc.erode(vertical_image, vertical_image, verticalStructure, new Point(-1, -1),1);
    Imgproc.dilate(vertical_image, vertical_image, verticalStructure, new Point(-1, -1),1);
    this.saveImage("out-table/3-vertical.png",vertical_image);

    /*
     * 合并线条
     * 将垂直线，水平线合并为一张图
     */
    Mat mask_image = new Mat();
    Core.add(horizontal_image,vertical_image,mask_image);
    this.saveImage("out-table/4-mask.png",mask_image);

    /*
     * 通过 bitwise_and 定位横线、垂直线交汇的点
     */
    Mat points_image = new Mat();
    Core.bitwise_and(horizontal_image, vertical_image, points_image);
    this.saveImage("out-table/5-points.png",points_image);

    /*
     * 通过 findContours 找轮廓
     *
     * 第一个参数，是输入图像，图像的格式是8位单通道的图像，并且被解析为二值图像（即图中的所有非零像素之间都是相等的）。
     * 第二个参数，是一个 MatOfPoint 数组，在多数实际的操作中即是STL vectors的STL vector，这里将使用找到的轮廓的列表进行填充（即，这将是一个contours的vector,其中contours[i]表示一个特定的轮廓，这样，contours[i][j]将表示contour[i]的一个特定的端点）。
     * 第三个参数，hierarchy，这个参数可以指定，也可以不指定。如果指定的话，输出hierarchy，将会描述输出轮廓树的结构信息。0号元素表示下一个轮廓（同一层级）；1号元素表示前一个轮廓（同一层级）；2号元素表示第一个子轮廓（下一层级）；3号元素表示父轮廓（上一层级）
     * 第四个参数，轮廓的模式，将会告诉OpenCV你想用何种方式来对轮廓进行提取，有四个可选的值：
     *      CV_RETR_EXTERNAL （0）：表示只提取最外面的轮廓；
     *      CV_RETR_LIST （1）：表示提取所有轮廓并将其放入列表；
     *      CV_RETR_CCOMP （2）:表示提取所有轮廓并将组织成一个两层结构，其中顶层轮廓是外部轮廓，第二层轮廓是“洞”的轮廓；
     *      CV_RETR_TREE （3）：表示提取所有轮廓并组织成轮廓嵌套的完整层级结构。
     * 第五个参数，见识方法，即轮廓如何呈现的方法，有三种可选的方法：
     *      CV_CHAIN_APPROX_NONE （1）：将轮廓中的所有点的编码转换成点；
     *      CV_CHAIN_APPROX_SIMPLE （2）：压缩水平、垂直和对角直线段，仅保留它们的端点；
     *      CV_CHAIN_APPROX_TC89_L1  （3）or CV_CHAIN_APPROX_TC89_KCOS（4）：应用Teh-Chin链近似算法中的一种风格
     * 第六个参数，偏移，可选，如果是定，那么返回的轮廓中的所有点均作指定量的偏移
     */
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    Mat hierarchy = new Mat();
    Imgproc.findContours(mask_image,contours,hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE,new Point(0,0));


    List<MatOfPoint> contours_poly = contours;
    Rect[] boundRect = new Rect[contours.size()];

    LinkedList<Mat> tables = new LinkedList<Mat>();







    //循环所有找到的轮廓-点
    for(int i=0 ; i< contours.size(); i++){

        MatOfPoint point = contours.get(i);
        MatOfPoint contours_poly_point = contours_poly.get(i);

        /*
         * 获取区域的面积
         * 第一个参数，InputArray contour：输入的点，一般是图像的轮廓点
         * 第二个参数，bool oriented = false:表示某一个方向上轮廓的的面积值，顺时针或者逆时针，一般选择默认false
         */
        double area = Imgproc.contourArea(contours.get(i));
        //如果小于某个值就忽略，代表是杂线不是表格
        if(area < 100){
            continue;
        }

        /*
         * approxPolyDP 函数用来逼近区域成为一个形状，true值表示产生的区域为闭合区域。比如一个带点幅度的曲线，变成折线
         *
         * MatOfPoint2f curve：像素点的数组数据。
         * MatOfPoint2f approxCurve：输出像素点转换后数组数据。
         * double epsilon：判断点到相对应的line segment 的距离的阈值。（距离大于此阈值则舍弃，小于此阈值则保留，epsilon越小，折线的形状越“接近”曲线。）
         * bool closed：曲线是否闭合的标志位。
         */
        Imgproc.approxPolyDP(new MatOfPoint2f(point.toArray()),new MatOfPoint2f(contours_poly_point.toArray()),3,true);

        //为将这片区域转化为矩形，此矩形包含输入的形状
        boundRect[i] = Imgproc.boundingRect(contours_poly.get(i));

        // 找到交汇处的的表区域对象
        Mat table_image = points_image.submat(boundRect[i]);

        List<MatOfPoint> table_contours = new ArrayList<MatOfPoint>();
        Mat joint_mat = new Mat();
        Imgproc.findContours(table_image, table_contours,joint_mat, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        //从表格的特性看，如果这片区域的点数小于4，那就代表没有一个完整的表格，忽略掉
        if (table_contours.size() < 4)
            continue;

        //保存图片
        tables.addFirst(source_image.submat(boundRect[i]).clone());

        //将矩形画在原图上
        Imgproc.rectangle(source_image, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 255, 0), 1, 8, 0);

    }

    List<Map<String, String>>  results=new ArrayList<Map<String, String>>();
    System.out.println(tables.size());
    Map<String, String> item=new HashMap<String, String>();
    for(int i=0; i< tables.size(); i++ ){
        Mat _perimg=tables.get(i);

        System.out.println("c"+(i/3));

        item.put("c"+(i/3), ocrText(_perimg));




       if ((i%3==0)&&(i>0))
       {
           results.add(item);
           item=new HashMap<String, String>();
           System.out.println("new line");
       }



        //拿到表格后，可以对表格再次处理，比如 OCR 识别等
//        this.saveImage("out-table/6-table-"+(i+1)+".png",tables.get(i));
    }

    System.out.println(results);

    this.saveImage("out-table/7-source.png",source_image);

}


private  String ocrText(Mat mat)
{
    String ret="";
    MatOfByte bytes = new MatOfByte();

    Imgcodecs.imencode(".jpg", mat, bytes);
    ByteBuffer buff = ByteBuffer.wrap(bytes.toArray());

    BytePointer outText;


    PIX image=pixReadMem(buff, (buff.capacity()));

    // Open input image with leptonica library
//    PIX image = pixRead("D:\\code\\123.jpg");
    api.SetImage(image);



    // Get OCR result
    outText = api.GetUTF8Text();
   ret=outText.getString();

    // Destroy used object and release memory

    outText.deallocate();
    pixDestroy(image);
    return ret;
}

private void saveImage(String path,Mat image){

    String outPath = this.test_file_path + File.separator + path;

    File file = new File(outPath);
    //目录是否存在
    this.dirIsExist(file.getParent());

    Imgcodecs.imwrite(outPath,image);

}



private void dirIsExist(String dirPath){
    File dir = new File(dirPath);
    if(!dir.exists()){
        dir.mkdirs();
    }
}

private   void initOcr()
{
    this.api = new TessBaseAPI();
    if (api.Init("D:\\code\\", "chi_sim") != 0) {
        System.err.println("Could not initialize tesseract.");
        System.exit(1);
    }
}

private   void disdroyOcr()
{
    api.End();
}
/**
 * 加载动态库
 */
private static void loadLibraries() {

    try {
        String osName = System.getProperty("os.name");
        String opencvpath = "D:\\dev\\sdk\\opencv\\";

        //windows
        if(osName.startsWith("Windows")) {
            int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
            //32位系统
            if(bitness == 32) {
                opencvpath=opencvpath+"x86//opencv_java410.dll";
            }
            //64位系统
            else if (bitness == 64) {
                opencvpath=opencvpath+"x64//opencv_java410.dll";
            } else {
                opencvpath=opencvpath+"x86//opencv_java410.dll";
            }
        }
        // mac os
        else if(osName.equals("Mac OS X")){
            opencvpath = "/usr/local/Cellar/opencv/3.4.0_1/share/OpenCV/java/libopencv_java340.dylib";
        }
        System.out.println(opencvpath);
        System.load(opencvpath);
    } catch (Exception e) {
        throw new RuntimeException("Failed to load opencv native library", e);
    }
}

}
