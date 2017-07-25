package pic.algorithm.sift;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pic.utility.Image_Utility;
import pic.utility.Math_Utility;

/**
 * sift算法的图像变换类
 * @author Administrator
 *
 */
public class ImageTransform {
	
	
	/**
	 * * 将图像进行高斯模糊：先利用模糊函数计算高斯模板矩阵，然后进行卷积运算。
	 *  
	 *	@高斯模糊 :斯模糊是一种图像滤波器，它使用正态分布(高斯函数)计算模糊模板，并使用该模板与原图像做卷积运算，达到模糊图像的目的。
	 *	在实际应用中，在计算高斯函数的离散近似时，在大概3σ距离之外的像素都可以看作不起作用，这些像素的计算也就可以忽略。
	 *	通常，图像处理程序只需要计算的矩阵就可以保证相关像素影响。
	 *  
	 * @param source
	 * @param index 表示
	 * @return double[][] 模糊后的图像信息矩阵
	 */
	private static double[][] gaussTran(double[][] source,int index){
		
		int height=source.length;
		int width=source[0].length;
		///保存高斯过滤后的结果
		double[][] result=new double[height][width];
		///获取高斯模板
		double[][] template=GaussTemplate.getTemplate(index);
		int tWH=template[0].length;///模板维数
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				
				///进行模糊处理——————卷积运算
				double sum=0.0;///卷积结果
				for(int m=0;m<tWH;m++){
					for(int n=0;n<tWH;n++){
						///计算与模板对应的图像上的位置
						int x=j-(int)tWH/2+n;
						int y=i-(int)tWH/2+m;
						
						//如果模板数据没有超过边界
						if(x>=0&&x<width&&y>=0&&y<height){
							sum=sum+source[y][x]*template[m][n];
						}
					}
				}
				
				result[i][j]=sum;
				
			}
		}
		int i=0;
		i++;
		return result;
	}
	
		
	/**
	 * 内部私有方法： 根据sigma参数计算高斯模糊模板
	 * @param sigma
	 * @return
	 */
	private static double[][] getGaussTemplate(double sigma){
		//sigma=1.6;
		int width,height;
		width=(int) (6*sigma+1);
		if(width<6*sigma+1){
			///6*sigma+1结果为小数，需要对矩阵维度加一
			width++;
		}
		height=width;
		
		double[][] template=new double[height][width];
		double sum=0.0;///用于归一化高斯模板
 
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				double value;	
				double index;//自然底数e的指数
				index=(i-height/2)*(i-height/2)+(j-width/2)*(j-width/2);
				index=-index/(2*sigma*sigma);
				
				value=(1/(2*sigma*Math.PI))*(Math.pow(Math.E, index));
				
				template[i][j]=value;//赋值给模板对应位置
				sum=sum+value;
			}
		}
		///归一化模板
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				template[i][j]=template[i][j]/sum;//赋值给模板对应位置	
//				System.out.print("    "+template[i][j]);
			}
//			System.out.println();
		}
		////System.out.println("sigma:"+sigma);
	    return template;	
	}
	
	
	
	
	/**
	 *  构建高斯金字塔
	 * @param source  图像信息数组
	 * @param minSize 金字塔顶层图片大小： 金字塔层数octave=(int) (Math.log(small)/Math.log(2.0));
	 * @param s		    每一组有s个尺度，所以每一组的高斯图像数目为S+3（一般s取值为3）
	 * @param baseSigma	 基准sigma	
	 *   
	 * @return		 返回一个包含该金字塔所有图片的HashMap<Integer,double[][]> ，按组和组内（0、1…）层的顺序
	 */
	public static HashMap<Integer,double[][]> getGaussPyramid(double[][] source,int minSize,int s,double baseSigma){
		int width=source[0].length;
		int height=source.length;
		int small=width>height?height:width;
		///求金子塔层数
		int octave=(int) (Math.log(small/minSize)/Math.log(2.0));
		///每一组高斯图像数目为S+3
		int gaussS=s+3;
		
		double[] sig=new double[6];
		sig[0]=baseSigma;
		for(int i=1;i<gaussS;i++){
			double preSigma=baseSigma*Math.pow(2, (double)(i-1)/s);
			double nextSigma=preSigma*Math.pow(2, (double)1/s);
			sig[i]=Math.sqrt(nextSigma*nextSigma-preSigma*preSigma);
		}
		 
		
		HashMap<Integer,double[][]> gaussPyramid=new HashMap<Integer, double[][]>();///用来保存结果		
		double[][] tempSource=gaussTran(source, 0);;	//临时存储
		////迭代生成一张张高斯图像
		for(int i=0;i<octave;i++){
			//tempSource =gaussTran(tempSource, baseSigma);
			int j=0;///组内层数
			int index = 0;///每张图片在数组(hashmap)里面的索引
			for(;j<gaussS;j++){
				if(0==j){
					///第一张不进行模糊处理
					index=i*gaussS+j;
					///存入高斯金字塔
					gaussPyramid.put(index, tempSource);
					continue;
				}				
				///计算得到下一张图片的组内尺度		
				//sigma=sigma*Math.pow(2, (double)1/s);
				double start=System.currentTimeMillis();

				///进行高斯模糊
				tempSource=gaussTran(tempSource, j);
				double end=System.currentTimeMillis();
				System.out.println("模糊"+(end-start));
				index=i*gaussS+j;
				///存入高斯金字塔
				gaussPyramid.put(index, tempSource);
				
			}
//			System.out.println(i+":"+j);
			
			///选每一组的倒数第三张图片进行降采样,注意此时的j是6而不是5,所以减去3而不是2
			tempSource=getGapSimpleImg(gaussPyramid.get(index-2));
			
		}
		return gaussPyramid;
	}
	
	
	/**
	 * 私有方法进行降采样（隔点采样）
	 * @param source
	 * @param width
	 * @param height
	 * @return
	 */
	private static double[][] getGapSimpleImg(double[][] source){
		
		///计算每一组高斯图的大小,**********************隔点采样按采取偶数位上的点进行，否则大小计算有误！！！！！
		int width=(int) source[0].length/2;
		int height=(int) source.length/2;
		///存储采样结果
		double[][] result=new double[height][width];
		
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				///计算原图上的对应坐标
				int y=2*i;
				int x=2*j;
				result[i][j]=source[y][x];
			}
		}
		return result;
	}
	
	
	/**
	 * 获取高斯差分金字塔（DoG金子塔）————近似高斯拉普拉斯函数，来获取非常稳定的极大极小值——特征点。
	 * @param source
	 * @param num	每一层高斯金字塔（组）有多少张图像
	 * @return
	 */
	public static HashMap<Integer,double[][]> gaussToDog(HashMap<Integer,double[][]> gaussPyramid,int num){
		
		 HashMap<Integer,double[][]> dogPyramid=new HashMap<Integer, double[][]>();
		
		 ////获取高斯金字塔里面的图像
		Set<Integer> iset=gaussPyramid.keySet();
		int length=iset.size();
		for(int i=0;i<length-1;i++){
			double[][]  source1=gaussPyramid.get(i);
			double[][] source2=gaussPyramid.get(i+1);
			int width=source1[0].length;
			int height=source1.length;
			
			 double[][] dogImage=new double[height][width];///临时dog图像
						
			///做差分计算
			if(((i+1)%num)!=0){
				///如果不是每一组的最后一张图像
				for(int m=0;m<height;m++){
					for(int n=0;n<width;n++){
						dogImage[m][n]=source2[m][n]-source1[m][n];						
					}
				}
				
				///存入dog金字塔
				dogPyramid.put(i, dogImage);				
			}///end of if 
			
			
		}
		
		
		return dogPyramid;
	}
	
	
	/**
	 * 初步检测DoG图像的极值点
	 * @param dogPyramid  	这个hashmap的key不是连续的数字，而是在高斯金子塔的图像索引值
	 * @param num 		高斯金子塔（而不是dog金字塔）每层的图像数目
	 * @return			HashMap<Integer,List<MyPoint>>  integer是高斯金子塔内的图像的索引
	 */
	public static HashMap<Integer, List<MyPoint>> getRoughKeyPoint(HashMap<Integer,double[][]> dogPyramid,int num){
		HashMap<Integer, List<MyPoint>> resultMap=new HashMap<Integer, List<MyPoint>>();         
		Set<Integer> dogIndex=dogPyramid.keySet();
		///增强for循环
		for(int i:dogIndex){
			///对于dog金字塔每组的第一张和最后一张（也就是高斯的倒数第二张）图像不进行求极值处理
			if(((i%num)!=0)&&((i%num)!=num-2)){
				double[][] dogImage=dogPyramid.get(i);
				///获取该层图空间位置上的下一层和上一层
				double[][] dogImageDown=dogPyramid.get(i-1);
				double[][] dogImageUp=dogPyramid.get(i+1);
				
				List<MyPoint> mpList=new ArrayList<MyPoint>();
				
				int width =dogImage[0].length;
				int height= dogImage.length;
				////对该张dog图像求极值点
				for(int y=0;y<height;y++){
					for(int x=0;x<width;x++){
						if((x>0)&&x<width-1&&y>0&&y<height-1){
							////图像的边缘点默认不是极值点
							///比较上下尺度以及8领域的共26个点
							double[] values=new double[26];		
							
							double keyValue=dogImage[y][x];///关键点的值
							
							values[0]=dogImage[y-1][x-1];
							values[1]=dogImage[y-1][x];
							values[2]=dogImage[y-1][x+1];
							values[3]=dogImage[y][x-1];
							values[4]=dogImage[y][x+1];
							values[5]=dogImage[y+1][x-1];
							values[6]=dogImage[y+1][x];
							values[7]=dogImage[y+1][x+1];
							
							values[8]=dogImageDown[y-1][x-1];///下一层
							values[9]=dogImageDown[y-1][x];
							values[10]=dogImageDown[y-1][x+1];
							values[11]=dogImageDown[y][x-1];
							values[12]=dogImageDown[y][x];
							values[13]=dogImageDown[y][x+1];
							values[14]=dogImageDown[y+1][x-1];
							values[15]=dogImageDown[y+1][x];
							values[16]=dogImageDown[y+1][x+1];
							
							values[17]=dogImageUp[y-1][x-1];///上一层
							values[18]=dogImageUp[y-1][x];
							values[19]=dogImageUp[y-1][x+1];
							values[20]=dogImageUp[y][x-1];
							values[21]=dogImageUp[y][x];
							values[22]=dogImageUp[y][x+1];
							values[23]=dogImageUp[y+1][x-1];
							values[24]=dogImageUp[y+1][x];
							values[25]=dogImageUp[y+1][x+1];
							
							boolean isOrNote=Math_Utility.isExtremeVlaue(values, keyValue);
							
							//if(keyValue)
							///如果是极值
							if(true==isOrNote){
								
								MyPoint mp=new MyPoint();
								/*mp.setPreX((int) (x*Math.pow(2, (int)(i/6))));
								mp.setPreY((int) (y*Math.pow(2, (int)(i/6))));
								*/
								mp.setX(x);
								mp.setY(y);
								mp.setOctave(i/6);
								mp.setS(i%6);
								
								mpList.add(mp);
								/////
							/*	if(Math.abs(keyValue)<0.03){
								System.out.println(keyValue);
							}*/
							}
							
						}
					}
				}///一张图像遍历完毕
				
				////保存该图像对应的特征点集
				resultMap.put(i, mpList);
			
				
			}
			
		}
		return resultMap;
	}
	
	
	
	
	
	/**
	 * 在原图上“画”出特征点
	 * @param gaussPyramid
	 * @param keyPoints
	 * @return
	 */
	public static BufferedImage drawPoints(HashMap<Integer,double[][]> gaussPyramid,HashMap<Integer, List<MyPoint>> keyPoints){
	
		 HashMap<Integer,double[][]> result=new HashMap<Integer, double[][]>();
			
		 List<MyPoint> testPoint=new ArrayList<MyPoint>();
		 
		 ////获取高斯金字塔里面的图像
		Set<Integer> iset=gaussPyramid.keySet();
		int length=iset.size();
		for(int i=0;i<length;i++){
			
			 double[][] tempImage =gaussPyramid.get(i);///临时图像			
			 List<MyPoint> imagePoint=keyPoints.get(i);
			 
			 if(null!=imagePoint){
				 
				 	///获取特征点，并比较				 
					List<MyPoint> vector=getFeatureVector(tempImage, imagePoint,6,1.6,8);
					if(vector==null){
						result.put(i, tempImage);
						continue;
					}
				/*	BufferedImage bimg=getP2P(tempImage, vector, null, null);
					tempImage=Image_Utility.imageToDoubleArray(bimg);*/
					
					testPoint.addAll(vector);
						
					///如果该图像是求了极值点的图像
					for(MyPoint mp:imagePoint){
						int x=mp.getX();
						int y=mp.getY();
						/*int x=mp.getX();
						int y=mp.getY();*/
						//int index=mp.getOctave()*6+mp.getS();
						//System.out.println(tempImage[y][x]);
						tempImage[y][x]=255;
						tempImage[y+1][x]=255;
						tempImage[y][x+1]=255;
						tempImage[y-1][x]=255;
						tempImage[y][x-1]=255;
						//System.out.println(++n);
						
					}
		
				}
			 
//				System.out.println("\n\n\n");
				
				///存入高斯金字塔
				result.put(i, tempImage);				
			}///end of for 
		
			
		BufferedImage bimg=getP2P(result.get(1), testPoint, null, null);
		
		
		return bimg;
	}
	
	/**
	 * 过滤关键点，得到更加稳定的特征点——————————去除对比度低（方差）和边缘点（hessian矩阵去边缘点）
	 * @param dogPyramid
	 * @param keyPoints
	 * @param r  在Lowe的文章中，取r＝10。图4.2右侧为消除边缘响应后的关键点分布图。
	 * @param contrast 对比度阈值
	 * @return
	 */
	public static HashMap<Integer, List<MyPoint>>  filterPoints(HashMap<Integer,double[][]> dogPyramid,
			HashMap<Integer, List<MyPoint>> keyPoints,int r,double contrast ){
		
		HashMap<Integer, List<MyPoint>> resultMap=new HashMap<Integer, List<MyPoint>>();         
//		Set<Integer> gaussIndex=gaussPyramid.keySet();
		Set<Integer> pSet=keyPoints.keySet();
//		int length=pSet.size()-1;
		for(int i:pSet){
			List<MyPoint> points=keyPoints.get(i);
			List<MyPoint> resultPoints=new ArrayList<MyPoint>();
			///获取对应的dog图像
			double[][] gaussImage=dogPyramid.get(i);
			for(MyPoint mp:points){
				int x=mp.getX();
				int y=mp.getY();
				////对极值点进行精确定位
				/*mp=adjustLocation(dogPyramid, mp, 6, 6);
				if(null==mp){
					///如果精确定位返回null，则抛弃该点
					continue;
				}
				x=mp.getX();
				y=mp.getY();*/
				
				double xy00=gaussImage[y-1][x-1];
				double xy01=gaussImage[y-1][x];
				double xy02=gaussImage[y-1][x+1];
				double xy10=gaussImage[y][x-1];
				double xy11=gaussImage[y][x];
				double xy12=gaussImage[y][x+1];
				double xy20=gaussImage[y+1][x-1];
				double xy21=gaussImage[y+1][x];
				double xy22=gaussImage[y+1][x+1];
				
				double dxx=xy10+xy12-2*xy11;
				double dyy=xy01+xy21-2*xy11;
				double dxy=(xy22-xy20)-(xy02-xy00);
				///hessian矩阵的对角线值和行列式
				double trH=dxx+dyy;
				double detH=dxx*dyy;//-dxy*dxy;
				
				///邻域的均值
				double avg=(xy00+xy01+xy02+xy10+xy11+xy12+xy20+xy21+xy22)/9;
				///领域方差
				double DX=(xy00-avg)*(xy00-avg)+(xy01-avg)*(xy01-avg)+(xy02-avg)*(xy02-avg)+(xy10-avg)*(xy10-avg)+
						(xy11-avg)*(xy11-avg)+(xy12-avg)*(xy12-avg)+(xy20-avg)*(xy20-avg)+(xy21-avg)*(xy21-avg)+(xy22-avg)*(xy22-avg);
				DX=DX/9;
				
				double threshold=(double)(r+1)*(r+1)/r;
				if((detH>0&&(trH*trH)/detH<threshold)&&(DX>=contrast)){
					///主曲率小于阈值，则不是需要剔除的边缘响应点;方差大于0.03的为高对比度
					resultPoints.add(mp);
				}
				
				//System.out.println(DX);
				
				
			}///end of inner for
			
			
			resultMap.put(i, resultPoints);
		}
		
		
		return resultMap;
	
	}
	
	/**
	 * 精确定位极值点
	 * @param dogPyramid
	 * @param keyPoint
	 * @return
	 */
	private static  MyPoint adjustLocation(HashMap<Integer,double[][]> dogPyramid,MyPoint kpoint,int MAX_STEP,int nLayer){
		
		int index=kpoint.getOctave()*nLayer+kpoint.getS();
		int octave=kpoint.getOctave();
		int x=kpoint.getX();
		int y=kpoint.getY();
		int layer=kpoint.getS();
		
		double offL=0,offY=0,offX = 0;
		
		
		///在max_step次数内完成
		int i=0;
		for(;i<MAX_STEP;i++){

			double[][] img=dogPyramid.get(index);
			double[][] pre=dogPyramid.get(index-1);
			double[][] next=dogPyramid.get(index+1);
			
			int width=img[0].length;
			int height=img.length;			
			if(pre!=null&&next!=null&&y>1&&x>1&&y<height-2&&x<width-2){	
				///三维空间的梯度向量
				double[] dvector=new double[3];
				///三维梯度矩阵
				double[][] dMatrix=new double[3][3];
				///赋值
				dvector[0]=(img[y][x+1]-img[y][x-1])/2;
				dvector[1]=(img[y+1][x]-img[y+1][x])/2;
				dvector[2]=(next[y][x]-pre[y][x])/2;
				/**
				 * dxx, dxy, dxs,  
	               dxy, dyy, dys,  
	               dxs, dys, dss
				 */
				double dxx=img[y][x+1]+img[y][x+1]-2*img[y][x];
				double dxy=(img[y+1][x+1]-img[y+1][x-1]-(img[y-1][x+1]-img[y-1][x-1]))/4;
				double dxs=(next[y][x+1]-next[y][x-1]-(pre[y][x+1]-pre[y][x-1]))/4;
				double dyy=img[y+1][x]+img[y][x+1]-2*img[y][x];
				double dys=(next[y+1][x]-next[y-1][x]-(pre[y+1][x]-pre[y-1][x]))/4;
				double dss=next[y][x]-next[y][x]-2*img[y][x];
				
				dMatrix[0][0]=dxx;
				dMatrix[0][1]=dxy;
				dMatrix[0][2]=dxs;
				dMatrix[1][0]=dxy;
				dMatrix[1][1]=dyy;
				dMatrix[1][2]=dys;
				dMatrix[2][0]=dxs;
				dMatrix[2][1]=dys;
				dMatrix[2][2]=dss;
				///偏移量向量
				double[] offset=new double[3];
				offset=Math_Utility.get3Inv_Vector(dMatrix, dvector);
				
				offL=-offset[2];
				offY=-offset[1];
				offX=-offset[0];
				
				if((Math.abs(offL)<0.5&&Math.abs(offY)<0.5&&Math.abs(offX)<0.5)){
					///如果已经收敛
					break;
				}
				
				layer=layer+Math.round((float)offL);
				y=y+Math.round((float)offY);
				x=x+Math.round((float)offX);
				
				if(layer<1||layer>nLayer||x<1||x>width-1||y<1||y>height-1){
					///如果越界，则返回null
					return null;
				}				
			}
			
		}//end of for 
		
		if(i>=MAX_STEP){
			///如果是通过for循环次数超过最大次数，则抛弃该点；
			return null;
		}
		
		///修改关键点的三维尺度坐标
		kpoint.setPreX((int)(x+offX)*(1<<octave));
		kpoint.setPreY((int)(y+offY)*(1<<octave));
		kpoint.setX(x);
		kpoint.setY(y);
		kpoint.setS(layer);
		
		return kpoint;
	}
	
	
	
	
	
	/**
	 *  *返回该高斯图像的特征向量集合:此处采用关键点周围的棋盘距离作为代数圆半径进行采样，距离分别为2、4、6……,然后对梯度方向方向按24°均分统计，
	 *   共得到largestDistance/2x15维的特征向量描述子；然后依次统计所有特征点*
	 * @param image  图像
	 * @param keyPoints	该图像对应的特征点集合
	 * @param largestDistances 棋盘距离的最大距离 ：8最合适
	 * @return
	 */
	private static List<MyPoint> getFeatureVector(double[][] image,List<MyPoint> keyPoints,int nLayer,double baseSigma,int largestDistance){
		List<MyPoint> vectorList=new ArrayList<MyPoint>();///保存特征向量的list
		//int keyR=0.6*Math.pow(2, i+(double)j/gaussS);
		if(keyPoints.isEmpty()){
			return null;
		}
		int s=keyPoints.get(0).getS();
		int octave=keyPoints.get(0).getOctave();
		
		
		///int n=0;///n用来记录特征点数
		for(MyPoint mp:keyPoints){
			int x=mp.getX();
			int y=mp.getY();
			
			int width=image[0].length;
			int height=image.length;
			double y2=image[y+1][x];
			double y1=image[y-1][x];
			double x2=image[y][x+1];
			double x1=image[y][x-1];
			
			//关键点梯度模值
//			double m=Math.sqrt((y2-y1)*(y2-y1)+(x2-x1)*(x2-x1));
			//关键点梯度方向,转为成0~360°之间的角度值
			double theta=Math.atan2(y2-y1, x2-x1)*(180/Math.PI)+180;
			
			//统计该特征点一定范围内的36个方向的模值和角度分布情况
			double[] keyTM=new double[45];
			double[] keyAngle=new double[45];
			double[]angleRatio=new double[45];////该变量用来记录主方向范围内（如0~10）的所有点的高斯核的比例总数，用keyAngle除以对应的比例就得到该主方向的高斯加权方向


			double max=0;///记录最大模值
			int index = 0;			
			
			
			
			double s_oct=baseSigma*Math.pow(2, (double)mp.getS()/nLayer);
			double sigma=1.5*s_oct;////高斯模糊核
			double[][] gtemplate=getGaussTemplate(sigma);////用于对模值进行高斯加权
			int radius=(int) (3*sigma);///领域采样半径
			int diameter=2*radius;
			
			int gtCenter=gtemplate.length/2;
			if(x>=diameter&&x<width-diameter&&y>=diameter&&y<height-diameter){	
				///sigma=9
				for(int j=0;j<=2*radius;j++ ){
					for(int i=0;i<=2*radius;i++){
						/*if((j==radius)&&(i==radius)){
							continue;
						}*/						
						double ty2=image[y-radius+j+1][x-radius+i];
						double ty1=image[y-radius+j-1][x-radius+i];
						double tx2=image[y-radius+j][x-radius+i+1];
						double tx1=image[y-radius+j][x-radius+i-1];
						//关梯度模值
						double tM=Math.sqrt((ty2-ty1)*(ty2-ty1)+(tx2-tx1)*(tx2-tx1));
						//梯度方向，转为成0~360°之间的角度值
						double tTheta=Math.atan2(ty2-ty1, tx2-tx1)*(180/Math.PI)+180;					
						int section=(int) (tTheta/9);		
						if(360-Math.abs(tTheta)<0.0001){
							///如果角度为360°，则和零一样算在第一个一区间内
							section=0;
						}
						keyTM[section]=keyTM[section]+tM*gtemplate[gtCenter-radius+j][gtCenter-radius+i];
						keyAngle[section]=keyAngle[section]+tTheta*gtemplate[gtCenter-radius+j][gtCenter-radius+i];////按比例对主方向产生角度贡献;
						angleRatio[section]+=gtemplate[gtCenter-radius+j][gtCenter-radius+i];
					}				
				}
				
				for(int key=0;key<keyTM.length;key++){
					if(keyTM[key]>max){
						max=keyTM[key];
						index=key;
					}
				}
				theta=keyAngle[index]/angleRatio[index];
			}
			

			
			
			

			///对关键如果有多个辅方向，就复制成多个关键点
			for(int key=0;key<keyTM.length;key++){
				if(keyTM[key]>max*0.8){
					///大于最大值得80%都作为主方向之一，复制成一个关键点
					theta=keyAngle[key]/angleRatio[key];////获得辅方向
				//	System.out.println("theta:"+theta);
			
			///计算每个代数圆内的梯度方向分布
			if(x>=largestDistance+1&&x<width-1-largestDistance&&y>=largestDistance+1&&y<height-1-largestDistance){	
				
				int secNum=15;//每个代数圆内多少扇形
				int secAngle=360/secNum;///多大的角为一个扇形
				double[] grads=new double[secNum*(largestDistance/2)];///保存多维向量的数组
				
				double sum=0;
				for(int j=y-largestDistance;j<=y+largestDistance;j++){
					for(int i=x-largestDistance;i<=x+largestDistance;i++){	
						
						if((j==y)&&(i==x)){
							continue;
						}
						
						double ty2=image[j+1][i];
						double ty1=image[j-1][i];
						double tx2=image[j][i+1];
						double tx1=image[j][i-1];
						
						//tx1=tx1*Math.cos(theta)-txy1
						//梯度模值
						double tM=Math.sqrt((ty2-ty1)*(ty2-ty1)+(tx2-tx1)*(tx2-tx1));
						sum=sum+tM;///；累加模值，便于后面归一化
						//梯度方向，转为成0~360°之间的角度值
						double tTheta=Math.atan2(ty2-ty1, tx2-tx1)*(180/Math.PI)+180;
						///减去关键点的方向，取得相对方向！！ 此处要不要取绝对值？？或者360-Math.abs(tTheta-theta)等？？
						double absTheta=tTheta-theta;
						///因为得到的theta的结果在-pi到+pi之间
						
						int section=(int) (absTheta/secAngle);	
						if(360-Math.abs(absTheta)<0.0001){
							///如果角度为360°，则和零一样算在第一个一区间内
							section=0;
						}
						if(section<0){
							///如果角度为负，应该加一个secNUm	转为正数
							section=section+secNum;
						}
						///计算棋盘距离
						int distance=Math.max(Math.abs(y-j),Math.abs(x-i));	
					
						if(distance<=2){
							///如果在距离2的棋盘距离内
							///梯度模值累加！！
							grads[section]=grads[section]+tM;							
						}else if(distance<=4){
							///
							///梯度模值累加！！
							grads[section+1*secNum]=grads[section+1*secNum]+tM;							
						}else if(distance<=6){
							///梯度模值累加！！
							grads[section+2*secNum]=grads[section+2*secNum]+tM;							
						}else if(distance<=8){
							///梯度模值累加！！
							grads[section+3*secNum]=grads[section+3*secNum]+tM;							
						}else if(distance<=10){
							///梯度模值累加！！
							grads[section+4*secNum]=grads[section+4*secNum]+tM;							
						}			
						
					}					
				}//// end  of  outer for  clause			
				///归一化向量
				grads=Math_Utility.normalize(grads, sum);
				///存储到mypoint对象里
				MyPoint rmp=new MyPoint(x, y, s, octave, theta, grads,false);				
				vectorList.add(rmp);
			
			}
				}}
		} 
		
		return vectorList;
	}
	
	
	
	
	/**
	 * 获取某张高斯图像的图片的所有特征向量集合
	 * @param gaussPyramid
	 * @param keyPoints
	 * @return
	 */
	private static  List<MyPoint> getVectors(HashMap<Integer,double[][]> gaussPyramid,HashMap<Integer, List<MyPoint>> keyPoints){
	
		 List<MyPoint> testPoint=new ArrayList<MyPoint>();		 
		 ////获取高斯金字塔里面的图像
		Set<Integer> iset=gaussPyramid.keySet();
		int length=iset.size();
		for(int i=0;i<length;i++){
			
			 double[][] tempImage =gaussPyramid.get(i);///临时图像			
			 List<MyPoint> imagePoint=keyPoints.get(i);
			 
			 if(null!=imagePoint){
				 
				 	///获取特征点，并比较				 
					List<MyPoint> vector=getFeatureVector(tempImage, imagePoint,6,1.6,8);
					if(vector==null){
						continue;
					}			
					testPoint.addAll(vector);
			 }
			 
		}///end of for 
		
		return testPoint;
	}
	
	
	

	
	
	/**
	 * 计算相似特征点的数目
	 * 计算向量间的夹角和欧式距离
	 * @param sourceArray
	 * @param vectors1
	 * @param targetArray
	 * @param vectors2
	 * @return
	 */
	public static int getSimilarPointsNum(List<MyPoint> vectors1,List<MyPoint> vectors2){
		
		
		int similarNum=0;
		
		List<MyPoint> vt1=vectors1;
		List<MyPoint> vt2=vectors2;
	
		MyPoint mp1=null;
		int index1=0;
		for(;index1<vt1.size();index1++){			
			mp1=vt1.get(index1);
			if(mp1.isMatched()==true){
				//匹配过的点不再继续匹配
				continue;
			}
			
			double minED=10;///最小欧式距离
			double sMinED=0;///次小欧式距离
			
			MyPoint mp2=null;
			//double theta2=0;
			int index2=0;
			for(;index2<vt2.size();index2++){

				mp2=vt2.get(index2);
				if(mp2.isMatched()==true){
					//匹配过的点不再继续匹配
					continue;
				}
				
				double tempED=0.0;
				double[] v1=mp1.getGrads();
				double[] v2=mp2.getGrads();
				for(int i=0;i<v1.length;i++){
					tempED=tempED+(v1[i]-v2[i])*(v1[i]-v2[i]);
				}
				tempED=Math.sqrt(tempED);///计算出欧式距离
				if(tempED<minED){
					sMinED=minED;
					minED=tempED;
				}
			}
			
			/**
			 * ratio=0. 4　对于准确度要求高的匹配；
			 * ratio=0. 6　对于匹配点数目要求比较多的匹配； 
			 * ratio=0. 5　一般情况下。
			 */
			if(minED/sMinED>0.8||minED>0.1){
				////距离太大，则舍弃
				continue;
			}
			mp1.setMatched(true);
			mp2.setMatched(true);
			
			vt1.set(index1, mp1);
			vt2.set(index2-1, mp2);			
		
			similarNum++;///匹配上的点数目加一  
		}
		
		
		return similarNum;
	} 
	
	
	
	
	
	
	/**
	 * 找出对应点，并连线
	 * 计算向量间的夹角和欧式距离
	 * @param sourceArray
	 * @return
	 */
	public static BufferedImage getP2P(double[][] sourceArray,List<MyPoint> vectors1,double[][] targetArray,List<MyPoint> vectors2){
		
		BufferedImage resultImage=Image_Utility.doubleArrayToGreyImage(sourceArray);//new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	 
		List<MyPoint> vt1=vectors1;
		List<MyPoint> vt2=vectors1;
		
		
			
		MyPoint mp1=null;
		int index1=0;
		for(;index1<vt1.size();index1++){
			
			mp1=vt1.get(index1);
			
			mp1.setPreX(mp1.getX()*(int)Math.pow(2, mp1.getOctave()));
			mp1.setPreY(mp1.getY()*(int)Math.pow(2, mp1.getOctave()));
			
			
			if(mp1.getPreX()>sourceArray[0].length*2/3||(mp1.isMatched()==true)){
				continue;
			}
			double minED=10;///最小欧式距离
			double sMinED=0;///次小欧式距离
			int x2 = 0;
			int y2 = 0;
			MyPoint mp2=null;
			//double theta2=0;
			int index2=0;
			for(;index2<vt2.size();index2++){

				mp2=vt2.get(index2);
				
				mp2.setPreX(mp2.getX()*(int)Math.pow(2, mp2.getOctave()));
				mp2.setPreY(mp2.getY()*(int)Math.pow(2, mp2.getOctave()));
				///不和自己比较
				/*if(mp2.getX()==mp1.getX()||(mp2.isMatched()==true)||(mp2.getX()<sourceArray[0].length/2)){
					continue;
				}*/
				if(mp2.getPreX()==mp1.getPreX()||(mp2.isMatched()==true)||(mp2.getPreX()<sourceArray[0].length*2/3)){
					continue;
				}
				
				double tempED=0.0;
				double[] v1=mp1.getGrads();
				double[] v2=mp2.getGrads();
				for(int i=0;i<v1.length;i++){
					tempED=tempED+(v1[i]-v2[i])*(v1[i]-v2[i]);
				}
				tempED=Math.sqrt(tempED);///计算出欧式距离
				if(tempED<minED){
					sMinED=minED;
					minED=tempED;
					x2=mp2.getX();
					y2=mp2.getY();
					
					x2=mp2.getPreX();
					y2=mp2.getPreY();
					//theta2=mp2.getTheta();
				}
			}
			
			//System.out.println(minED/sMinED+"哈哈："+minED);
			
			
			/**
			 * ratio=0. 4　对于准确度要求高的匹配；
			 * ratio=0. 6　对于匹配点数目要求比较多的匹配； 
			 * ratio=0. 5　一般情况下。
			 */
			if(minED/sMinED>0.4||minED>0.1){
				////距离太大，则舍弃
				continue;
			}
//			System.out.println(minED/sMinED+"哈哈："+minED);
			
			
			mp1.setMatched(true);
			mp2.setMatched(true);
			
			vt1.set(index1, mp1);
			vt1.set(index2-1, mp2);
			vt2.set(index1, mp1);
			vt2.set(index2-1, mp2);
			
			Graphics2D g= resultImage.createGraphics();
			g.setColor(Color.black);
			//g.setPaint(Color.magenta);
			//g.setBackground(Color.black);
			g.setStroke(new BasicStroke(4));//设置线条粗细
			
			/*g.drawLine(mp1.getPreX(), mp1.getPreY(),(int)( mp1.getPreX()+40*Math.cos(mp1.getTheta())),(int)( mp1.getPreY()+40*Math.sin(mp1.getTheta())));
			g.drawLine(x2, y2,(int)( x2+40*Math.cos(theta2)),(int)( y2+40*Math.sin(theta2)));*/
			g.drawLine(mp1.getPreX(), mp1.getPreY(), x2, y2);
			
			/*g.drawLine((int)(mp1.getX()*Math.pow(2, mp1.getOctave())), 
					(int)(mp1.getY()*Math.pow(2, mp1.getOctave())), 
					(int)(x2*Math.pow(2, mp2.getOctave())),
					(int)(y2*Math.pow(2, mp2.getOctave())));*/
			
			resultImage.setRGB(x2, y2,255<<16);resultImage.setRGB(x2+1, y2+1,255<<16);
			resultImage.setRGB(mp1.getPreX(), mp1.getPreY(),255<<16);resultImage.setRGB(mp1.getPreX()+1, mp1.getPreY()+1,255<<16);
			
			///mp1.set
		}
		
		
		return resultImage;
	}
	
	
	 
	/**
	 * 获取该张图片的所有特征向量的集合
	 * @param img1
	 * @return
	 */
	public static List<MyPoint> getCharacterVectors(BufferedImage img){
		
		double start=System.currentTimeMillis();
		///生成高斯金字塔
		HashMap<Integer,double[][]> result=ImageTransform.getGaussPyramid(Image_Utility.imageToDoubleArray(img), 20, 3,1.6);
		double end=System.currentTimeMillis();
		System.out.println("高斯金字塔费时："+(end-start));
		
		//生成dog金字塔
		HashMap<Integer,double[][]> dog=ImageTransform.gaussToDog(result, 6);
		//初步获取特征点
		HashMap<Integer,List<MyPoint>> keyPoints=ImageTransform.getRoughKeyPoint(dog,6);
		///特征点过滤
		keyPoints=ImageTransform.filterPoints(dog, keyPoints, 10,0.03);
		
		List<MyPoint> vctors=getVectors(result, keyPoints);
		
		return vctors;
	}
	
	
	
	
	}