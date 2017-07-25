package pic.utility;

/**
 * 图片工具类
 * 
 * 
 * 	颜色、形状、纹理
 * 
 * 
 * 	1、点增强处理：（灰度变化）、伸缩旋转变换、
 * 	2、空域增强（位置形状大小）：物体边缘轮廓由于灰度值变化出现高频特征，平滑物体灰度值均匀呈现低频。
 * 			锐化：增强高频——————突出轮廓。
 * 			平滑处理：增强低频————————消除图像噪音。去除噪音包括去除孤立点，改变孤立点的灰度值。
 * 		图像空间域增强方法：采用模板卷积方法，见图片————模板空域增强。
 * 	3、频域增强：
 * 	4、倾斜缩放旋转校正 ？？
 * 
 * @author Administrator
 *
 */
public class Pic_Utility {
	
	/**
	 * 
	 * @param rgb
	 * @return
	 */
	public int rgbToGray(int rgb){
		int gray=0;
		
		
		return gray;
	}
	
}
