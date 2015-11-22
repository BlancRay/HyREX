package run;

import java.util.*;

import run.Util;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.*;
import weka.core.Instance;
import weka.core.Utils;
@SuppressWarnings("serial")
public class Tritrainer extends Classifier{

	private Instances unlabeledIns =null; //未标记的大样本集；
	private Instances labeledIns = null; //标记的小样本集；
	private Instances testIns = null; //测试样本集合；
	
	
	private Instances[] ins_Array = new Instances[3];
	
	private Classifier[] class_Array = new Classifier[3];  //初始构造的三个分类器；
	private Classifier classifier1 = new J48();
	private Classifier classifier2 = new J48();
	private Classifier classifier3 = new J48();
	
	
	
	private int[] length ={0,0,0};
	private int j=0,k=0;
	
	private boolean update1 = true;
	private boolean update2 = true;
	private boolean update3 = true;
	private boolean[] update = {true,true,true};
	
	private double error[] = {0.5,0.5,0.5};  //初始错误率；
	
	private double[] err_Array = new double[3];
	private double[] err_Classifier = {0.0,0.0,0.0};
	
	private Instances[] instan_Array = new Instances[3];
	
	private Random m_Random = new Random();  //随机数;
	/**
	 * @param args
	 */
	private void Init()
	{
		testIns.setClassIndex(testIns.numAttributes()-1);
		labeledIns.setClassIndex(labeledIns.numAttributes()-1);
		unlabeledIns.setClassIndex(unlabeledIns.numAttributes()-1);
		
		class_Array[0] = classifier1;
		class_Array[1] = classifier2;
		class_Array[2] = classifier3;
	}
	
	//构造一个tri-trainer分类器。
	public Tritrainer(String classifier,String trainingIns_File,String testIns_File,double precentage)
	{
		try
		{
			this.classifier1 = (Classifier)Class.forName(classifier).newInstance();
			this.classifier2 = (Classifier)Class.forName(classifier).newInstance();
			this.classifier3 = (Classifier)Class.forName(classifier).newInstance();
		
			Instances trainingInstances = Util.getInstances(trainingIns_File);
		
			//将trainIns_File按照precentage和(1-precentage)的比例切割成labeledIns和unlabeledIns;
			int length = trainingInstances.numInstances();
			int i = new Double(length*precentage).intValue();
			labeledIns = new Instances(trainingInstances,0);
			for(int j = 0; j < i; j ++)
			{
				labeledIns.add(trainingInstances.firstInstance());
				trainingInstances.delete(0);
			}
			unlabeledIns= trainingInstances;
			testIns = Util.getInstances(testIns_File);
			
			Init();
		}
		catch(Exception e)
		{
		
		}
		
		
	}
	public void buildClassifier(Instances data) throws Exception 
	{
        //print the errorRate of each classifier in the TriTrainer
		this.bootstrap();
		
		//print the errorRate of the TriTrainer Classifier before training;
		System.out.println("the TriTrainer's errorRate by Majority voting before training:" + this.errorRateByMajorityVoting());
		
		//training this classifier;
		this.training();
		System.out.println("");
		
		//print the errorRate of each classifier in the TriTrainer
		this.argMax();
		
        //print the errorRate of the TriTrainer Classifier after training;
		System.out.println("the TriTrainer's errorRate by Majority voting after training:" + this.errorRateByMajorityVoting());
		
	}
	
	//use the TriTrainer Classifier to classify Instance;
	public double classifyInstance(Instance instance) throws Exception
	{
		double result;
	    double[] dist;
	    int index;
		dist = distributionForInstance(instance);//分类概率
		
		if (instance.classAttribute().isNominal()) {
		  index = Utils.maxIndex(dist);//返回概率最大的
		  if (dist[index] == 0)
		    result = Instance.missingValue();
		  else
		    result = dist[index];
		}
		else if (instance.classAttribute().isNumeric()){
		  result = dist[0];
		}
		else {
		  result = Instance.missingValue();
		}
		return result;
	}
	
	public double[] distributionForInstance(Instance instance) throws Exception 
	{
		return this.distributionForInstanceMajorityVoting(instance);
	}
	
	
    //this method MajorityVoting to decide the probs of the Instance;
	//
	protected double[] distributionForInstanceMajorityVoting(Instance instance) throws Exception {

		    double[] probs = new double[instance.classAttribute().numValues()];
		    double[] votes = new double[probs.length];
		    
		    for (int i = 0; i < class_Array.length; i++) {
		      probs = class_Array[i].distributionForInstance(instance);
		      
		      int maxIndex = 0;
		      for(int j = 0; j<probs.length; j++) {
		          if(probs[j] > probs[maxIndex])
		        	  maxIndex = j;
		      }
		     
		      // Consider the cases when multiple classes happen to have the same probability
		      for (int j=0; j<probs.length; j++) {
			if (probs[j] == probs[maxIndex])
			  votes[j]++;
		      }
		    }
		   
		    int tmpMajorityIndex = 0;
		    for (int k = 1; k < votes.length; k++) {
		      if (votes[k] > votes[tmpMajorityIndex])
			tmpMajorityIndex = k;
		    }
		  
		    // Consider the cases when multiple classes receive the same amount of votes
		    Vector<Integer> majorityIndexes = new Vector<Integer>();
		    for (int k = 0; k < votes.length; k++) {
		      if (votes[k] == votes[tmpMajorityIndex])
			majorityIndexes.add(k);
		     }
		   // System.out.println("forth");
		    // Resolve the ties according to a uniform random distribution
		    int majorityIndex = majorityIndexes.get(m_Random.nextInt(majorityIndexes.size()));
		 
		    //set the probs of the classes which have not been voted to 0
		    for (int k = 0; k<probs.length; k++)
		      probs[k] = 0;
            //the class that have been voted the most receives 1   
		    probs[majorityIndex] = 1; 
		    
		    return probs;
	}
    //	计算h1,h2分类器共同的分类错误率；
	public double measureBothError(Classifier h1,Classifier h2, Instances test)  
	{
		int m = test.numInstances();
		double value1,value2,value ;
		int error = 0,total = 0;
		try
		{
			for(int i=0; i < m; i++)
			{
				value = test.instance(i).classValue();
				value1 = h1.classifyInstance(test.instance(i));
				value2 = h2.classifyInstance(test.instance(i));
				
				
              //两分类器做出相同决策
				if(value1 == value2)
				{
					//两分类器做出相同决策的样本数量
					total ++;
					
					//两分类器做出相同错误决策
					if(value != value1 )
					{
                    //	两分类器做出相同错误决策的样本数量
						error ++;
					}
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		//System.out.println("m:=" + m);
		//System.out.println("error:=" + error +"; total:=" + total);
		
		//两个分类器的分类错误率= 两分类器做出相同错误决策的样本数量/两分类器做出相同决策的样本数量
		return (error*1.0)/total;
	}
	
	 //通过h1,h2分类器学习样本集，将h1,h2分类决策相同的样本放入L中，得到标记集合；
	public void updateL(Classifier h1,Classifier h2,Instances L,Instances test) 
	{
		int length = unlabeledIns.numInstances();
		double value1 = 0.0,value2 =0.0;
		try
		{
			for(int i =0; i< length; i++)
			{
				value1 = h1.classifyInstance(test.instance(i));
				value2 = h2.classifyInstance(test.instance(i));
				if(value1 ==value2)
				{
                   //当两个分类器做出相同决策时重新标记样本的类别；
					test.instance(i).setClassValue(value1);  
					L.add(test.instance(i));
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		//return false;
	}
	
    //获得最大下界整数；
	public int getDownInt(double e1,double e2)  
	{
		double m = e1/(e2-e1) + 1;
		String m_str = Double.toString(m);
		String[] m_str_Array = m_str.split("\\.");
		int i = Integer.parseInt(m_str_Array[0]);
		return i;
	}
	
    //获得最小上界整数
	public int getUpInt(double e1,double e2,int l)  
	{
		double m = (e2*l)/e1 - 1;
		String m_str = Double.toString(m);
		String[] m_str_Array = m_str.split("\\.");
		int i = Integer.parseInt(m_str_Array[0]);
		return i + 1;
	}
	
    //将样本集中裁剪提取成m个样本组成的集合；
	public void SubSample(Instances inst,int m)   
	{
		inst.randomize(new Random());
		while(inst.numInstances()!=m)
		{
			inst.delete(0);
		}
		//System.out.println("subsample:=" + inst.numInstances() + " m:=" + m );
	}
	
	 //tri-training学习过程；
	public void training()  
	{
		int length_L = 0;
		int up_int = 0;
		double temp = 0.0;
		
		//直到没有分类器发生更新时，跳出循环；
		while(update1 || update2 || update3)
		{
			update1 = false;
			update2 = false;
			update3 = false;
			for(int i=0; i< 3; i++)
			{
				
				ins_Array[i] = new Instances(testIns,0);
				
				ins_Array[i].setClassIndex(testIns.numAttributes()-1);
				
				
				switch (i)
				{
				case 0:j=1;k=2;break;
				case 1:j=0;k=2;break;
				case 2:j=0;k=1;break;
				}
				
				//获得用于加强第i个分类器的其它两个分类器j,k的分类错误率；
				err_Array[i] = measureBothError(class_Array[j],class_Array[k], this.unlabeledIns);
				
				//如果这个两个分类器j,k的分类错误率小于前一次的时候，运用这两个分类器为第i个分类器标记样本；
				if(err_Array[i]< error[i])
				{
					 
					//获得两个分类器j,k分类做出相同决策得到的样本集合ins_Array[i]
					this.updateL(class_Array[j], class_Array[k], ins_Array[i], this.unlabeledIns);
					
					length_L = ins_Array[i].numInstances();
					
					if(length[i]==0)
					{
					
					//	System.out.println("err_array[i] =" + err_Array[i] + " err=" + error );
						length[i] = this.getDownInt(err_Array[i],error[i]);
					//	System.out.println("length[i] =" + length[i]);
					}
					
					if(length[i] < length_L)
					{
						if(err_Array[i]*length_L < error[i]*length[i])
						{
							this.update[i] = true;
						}
						else if(length[i] > (err_Array[i]/(error[i] - err_Array[i])))
						{
							up_int = this.getUpInt(err_Array[i], error[i], length[i]);
						//	System.out.println("err_array[i] =" + err_Array[i] + " err=" + error + "length:=" + length[i]);
						//	System.out.println("up_int=" + up_int );
							this.SubSample(this.ins_Array[i], up_int);
							this.update[i] =true;
						}
					}
				}
			}
			
			//更新分类器
			for(int i = 0; i < 3; i ++)
			{
				//如果第i个分类器的update为true,更新该分类器；
				if(this.update[i])
				{
					try{
						this.class_Array[i].buildClassifier(Util.add(this.instan_Array[i], this.ins_Array[i]));
						temp = Util.errorRate(this.class_Array[i],this.testIns);
						
						//如果分类器更新以后的分类错误率比以前高，则恢复分类器到未更新时的状态。 这一点与论文中的算法有一点点不同。
						//论文中没有这一步的判断。
						if(temp > err_Classifier[i])
						{
							this.update[i] = false;
							this.class_Array[i].buildClassifier(this.instan_Array[i]);
						}
						else
						{
							//如果分类器的分类错误率下降了，则更新length[i]以及error[i];
							length[i] = this.ins_Array[i].numInstances();
							error[i]= err_Array[i];
						}
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
			}
		}
		
		
	}
	
    //打印出最终三个分类器的分类错误率；
	public void argMax()   
	{
		
		System.out.println("after training：");
		for(int i =0; i<3; i ++)
		{
			err_Classifier[i] = Util.errorRate(this.class_Array[i],this.testIns);
			System.out.println("classifier[" + i + "]:=" + err_Classifier[i]);
		}
		
	}
	
    //使用bootstrap方法从固定小样本集中获得三个样本集，并用这三个样本集训练三个不同的分类器；
	public void bootstrap()    
	{
		Instances x = new Instances(labeledIns,0);
		System.out.println("before training：");
		try
		{
			for(int i=0; i < 3; i++)
			{
				x = this.labeledIns.resample(new Random());
				this.instan_Array[i]=x;
				class_Array[i].buildClassifier(x);
				err_Classifier[i] = Util.errorRate(this.class_Array[i],this.testIns);
				System.out.println("classifier[" + i + "]:=" +Util.errorRate(this.class_Array[i],this.testIns));
				
			}
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		
	}
	
	
	//this method use MajorityVoting to decide the tritrainer's errorRate;
	public double errorRateByMajorityVoting()
	{
		return Util.errorRate(this, this.testIns);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*Trainer1 tra = new Trainer1("weka.classifiers.bayes.NaiveBayes");
		tra.bootstrap();
		tra.training();*/
		Evaluation.runTrainer(args);
		/*String classifier_Name = args[0];   //args[0]为分类器类名；
		String unlabeledIns_File = args[1];  // args[1]未标记的大样本集文件名；
		String labeledIns_File = args[2];   // args[2]标记的小样本集文件名；
		String testIns_File = args[3];   //arg[3]测试样本集文件名
		
		Tri_trainer tra = new Tri_trainer(classifier_Name,unlabeledIns_File,labeledIns_File,testIns_File);
		tra.bootstrap();
		tra.training();
		/*try
		{
		
			Classifier classifier1 = (Classifier)Class.forName("weka.classifiers.trees.J48").newInstance();
			Classifier classifier2 = new J48();
			Classifier classifier4 = classifier2;
			Classifier classifier3 = (Classifier)Class.forName("weka.classifiers.trees.J48").newInstance();
			//if(classifier1 == null)
			System.out.println("classifier1 =" + classifier1.hashCode());
			System.out.println("classifier2 =" + classifier2.hashCode());
			System.out.println("classifier3 =" + classifier3.hashCode());
			System.out.println("classifier4 =" + classifier4.hashCode());
		}
		catch(Exception e)
		{
			
		}*/
		
	}

}