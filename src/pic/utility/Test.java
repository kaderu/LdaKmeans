package pic.utility;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import pic.algorithm.sift.ImageTransform;
import pic.ui.MainFrame;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			
		
      
		String source="D:\\images\\source.jpg";
		//String source="C:\\Users\\Administrator\\Desktop\\mySpace\\SimilarImageSearch\\images\\source.jpg";
	/*	String target1="C:\\Users\\Administrator\\Desktop\\mySpace\\SimilarImageSearch\\images\\jobs30.jpg";
		
		String target2="C:\\Users\\Administrator\\Desktop\\mySpace\\SimilarImageSearch\\images\\jobs2.jpg";
		
		String target3="D:\\images\\example2.jpg";
		String target4="D:\\images\\jobs20.jpg";
		String target5="C:\\Documents and Settings\\Administrator\\桌面\\mySpace\\SimilarImageSearch\\images\\s3.jpg";*/
//		String target3="C:\\Documents and Settings\\Administrator\\桌面\\mySpace\\SimilarImageSearch\\images\\example3.jpg";
		
		double start=System.currentTimeMillis();
		
		
		int [] pics=new int[15];
		for(int i=1;i<=1;i++){
			String target="D:\\images\\example13.jpg";
			new MainFrame(source, target).show(true);    	    
		}
      
		double end=System.currentTimeMillis(); 
      
      
      
		System.out.println(end-start);
      
      
     // System.out.println(Math.atan2(-2, 1)-Math.atan2(1, 2)+"\n"+Math.atan2(1, 2));
      
      
      
    /*
     ///打印高斯模板
       double baseSigma=1.6;
      int gaussS=6,s=3;
      
      double[] sig=new double[6];
		sig[0]=baseSigma;
		for(int i=1;i<gaussS;i++){
			double preSigma=baseSigma*Math.pow(2, (double)(i-1)/s);
			double nextSigma=preSigma*Math.pow(2, (double)1/s);
			sig[i]=Math.sqrt(nextSigma*nextSigma-preSigma*preSigma);
			
		}
      for(double d:sig){
    	  double[][] g=ImageTransform.getGaussTemplate(d);
    	  System.out.println("\n\n\nsigma:"+d);
    	   for(double[] g1:g){
    		   System.out.print("{");
    	    	  for(double g2:g1){
    			  System.out.print(g2+",");
    		  }
    		  System.out.print("},");
        	  System.out.println("");
    	  }
    	  
      }*/
      
      
      
      
	}	
}
