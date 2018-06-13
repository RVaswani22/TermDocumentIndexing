import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Lucenefinal {
		public static void main(String []args) throws IOException
	{
		Path indexdir = FileSystems.getDefault().getPath(args[0]);
		Directory directory = FSDirectory.open(indexdir);
		IndexReader ireader = DirectoryReader.open(directory); 
		String[] langlist = new String[]{"text_nl","text_fr","text_de","text_ja","text_ru","text_pt","text_es",
				"text_it","text_da","text_no","text_sv"};
		HashMap<String, ArrayList<Integer>> plist = new HashMap<String, ArrayList<Integer>>();
		for(String s:langlist)
		populatemap(s,plist);
		File fileDir = new File(args[2]);
 		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF8"));
		String str,path=args[1];
		while ((str = in.readLine()) != null) {
			if(str.startsWith("\ufeff"))
				str.replace("\ufeff", "");
			String[] querylist=str.split(" ");
			ArrayList<Integer> temp= new ArrayList<Integer>();
						for(String s:querylist)
			{
    		    Writer out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,true),"UTF-8"));
				out.write("GetPostings \n"+s+"\nPostings List: ");
				temp.addAll(plist.get(s));
				String op="";
				for(Integer xx:temp)
				{
					op+=xx+" ";
				}		
				op+=" \n";
				out.write(op);
				temp.clear();
				out.close();
			}
			taatand(str,plist,path);
			taator(str,plist,path);
			daatand(str,plist,path);	
			daator(str,plist,path);
				
			}
		        in.close();
                System.out.print("DONE");
	}
	
	public static void populatemap(String lang,HashMap<String,ArrayList<Integer>> hash) throws IOException
	{
/* This function accepts the hashmap and the language of the terms to be added from main(). Terms for the corresponding language are retrieved
 * and added to the hash map where, the key is the term and the value is an arraylist of type int saving the postings list*/
		Path indexdir = FileSystems.getDefault().getPath("index");
		Directory directory = FSDirectory.open(indexdir);
		IndexReader ireader = DirectoryReader.open(directory);
		Terms t = MultiFields.getTerms(ireader,lang);
		TermsEnum t2 = t.iterator();
	while(t2.next()!=null)
		{
		PostingsEnum postings=t2.postings(null);
		ArrayList<Integer> myArray= new ArrayList<Integer>();
		while(postings.nextDoc()!=PostingsEnum.NO_MORE_DOCS)
		{
				
			myArray.add(postings.docID());
					}
		
		hash.put(t2.term().utf8ToString(), myArray);
		}
	}
	
	public static void taator(String query,HashMap<String,ArrayList<Integer>> hash,String path) throws IOException
	{
	 //Implementation of TaatOr
	// Postings lists are compares two at a time, the ORed list is saved and acts as one of the two lists(here the list is finalarray) to be compared in the next iteration
	String[] querylist=query.split(" "); //get list of terms
	ArrayList<Integer> finalarray= new ArrayList<Integer>(); 
	finalarray.addAll(hash.get(querylist[0])); //initialise array with 0th list
	int c=0;
	for(int i=1;i<querylist.length;i++) //start from second element(if it exists) 
	{
		ArrayList<Integer> t1= new ArrayList<Integer>();
		ArrayList<Integer> temp= new ArrayList<Integer>();
		t1.addAll(hash.get(querylist[i]));
		int t1pointer=0,t2pointer=0;
		while(t1pointer<t1.size()&&t2pointer<finalarray.size())
		{
			if(t1.get(t1pointer)<finalarray.get(t2pointer))//if element pointed to in first array is smaller, add it to the list and progress pointer
			{
				temp.add(t1.get(t1pointer));
				c++;
				t1pointer++;
			}
			else if(t1.get(t1pointer)>finalarray.get(t2pointer))//if element pointed to in second array is smaller, add it to the list and progress pointer
			{
				temp.add(finalarray.get(t2pointer));
				c++;
				t2pointer++;
			}
			else //both elements are equal, add element to final list and move both pointers ahead
			{
				temp.add(t1.get(t1pointer));
				c++;
				t1pointer++;
				t2pointer++;
			}
		}
		if(t1pointer<t1.size()) //Above loop terminates when one of the lists are completely traversed, here we are adding the remaining elements
		{
			for(int ii=t1pointer;ii<t1.size();ii++)
			{
				temp.add(t1.get(ii));
			}
		}
		else if(t2pointer<finalarray.size())
		{
			for(int j=t2pointer;j<finalarray.size();j++)
			{
				temp.add(finalarray.get(j));
			}
		}

		finalarray.clear();
		finalarray.addAll(temp);//prepare list finalarray to act as input for next iteration. ORed list is copied to finalarray
	}
	Writer out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,true),"UTF-8"));
	out.write("TaatOr");
	out.write("\n"+query);
	if(finalarray.size()==0)
		out.write("\nResults: empty");
	else
		out.write("\nResults: ");
	String op="";
	for(Integer d:finalarray) {
		System.out.println(d);
        op+=d+" ";   
     }
	op+="\n";
	out.write(op);
	out.write("Number of documents in results: "+finalarray.size()+"\n");
	out.write("Number of comparisons: "+c+"\n");
    out.close();
	}
	
	public static void taatand(String query,HashMap<String,ArrayList<Integer>> hash,String path) throws IOException
	{
		//Implementation of TaatAnd
		/* Here, like above two arrays are compared and ANDed in a nested loop. At the next iteration, the AND result of the previous loop is used as the input for
		 * the next iteration*/
    Writer out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,true),"UTF-8"));
	String[] querylist=query.split(" ");
	ArrayList<Integer> finalarray= new ArrayList<Integer>();
	ArrayList<Integer> temp= new ArrayList<Integer>();
	finalarray.addAll(hash.get(querylist[0]));
	int maxval=finalarray.get(0),flag=0,maxindex=1,c=0;
	if(querylist.length==1);
	else
	{
	
	for(int i=1;i<querylist.length;i++)
	{
		ArrayList<Integer> t1= new ArrayList<Integer>();

		t1.addAll(hash.get(querylist[i]));
		int t1pointer=0,t2pointer=0;
		while(t1pointer<t1.size()&&t2pointer<finalarray.size()) //continue while no arrays are completely traversed
		{
			if(t1pointer==t1.size() || t2pointer==finalarray.size())
			{
				flag=1;
				break;
			}
			else if(t1.get(t1pointer)==maxval && maxindex!=0)
			{
				temp.add(t1.get(t1pointer));
				c++;
				t1pointer++;
				t2pointer++;
				
			}
			else if(t1.get(t1pointer)>maxval)
			{
				maxval=t1.get(t1pointer);
				c++;
				maxindex=0;
			}
			else if(t1.get(t1pointer)<maxval)
			{
				c++;
				t1pointer++;				
			}
			if(t1pointer==t1.size() || t2pointer==finalarray.size())
			{
				flag=1;
				break;
			}
			else if(finalarray.get(t2pointer)==maxval && maxindex!=1)
			{
				temp.add(finalarray.get(t2pointer));
				t1pointer++;
				c++;
				t2pointer++;
			}
			else if(finalarray.get(t2pointer)>maxval)
			{
				maxval=finalarray.get(t2pointer);
				maxindex=1;
				c++;
			}
			else if(finalarray.get(t2pointer)<maxval)
			{
				t2pointer++;
				c++;
			}
		}
		}
	}
	out.write("TaatAnd \n");
	out.write(query+" \n");
	if(temp.size()==0)
		out.write("Results: empty");
	else
		out.write("Results: ");
	String op="";
	for(Integer d:temp) {
        op+=d+" ";
    }
	op+="\n";
	out.write(op);
	out.write("Number of documents in results: "+temp.size()+"\n");
	out.write("Number of comparisons: "+c+"\n");
    out.close();
	}
	
	
	public static void daator(String query,HashMap<String,ArrayList<Integer>> hash,String path) throws IOException
	{
		/*Implementation of Daator
		 * A 2d array list is maintained to save the postings for corresponding terms. At the same time, an array named "Markers" of size 'array' is maintained. 
		 * Each element in markers acts as a pointer index for the corresponding sub array in the 2D array. This array keeps track of which element we are supposed to compare next
		 * in the sub arrays */
		 Writer out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,true),"UTF-8"));
		String[] querylist=query.split(" ");
		int arrlen,row=0,col=0,minval=0,minindex=0,c=0;
		ArrayList<ArrayList<Integer>> Daat = new ArrayList<ArrayList<Integer>>();
	    ArrayList<Integer> finalarray= new ArrayList<Integer>();
	    if(querylist.length==1)
	    {
	    	finalarray.addAll(hash.get(querylist[0]));
	    }
		else
		{
		for(String i :querylist)
		{
			Daat.add(hash.get(i));
		}
		int[] markers=new int[Daat.size()];
		int flag=0;
		for(int i=0;;i++)
		{
			minval=99999;	//used as a global maximum, no posting value will exceed this. This ensures we get a minimum value at each iteration.	
			for(int j=0;j<Daat.size();j++)
			{
	
				if(markers[j]>Daat.get(j).size());
				
			else if(markers[j]==Daat.get(j).size())
				{
					flag+=1;
					break;
				}
				else if(Daat.get(j).get(markers[j])==minval)
				{
					markers[j]++;
					c++;
				}
				else if(Daat.get(j).get(markers[j])<minval)
				{
					minval=Daat.get(j).get(markers[j]);
					c++;
					minindex=j;
				}
			}
			if(flag==markers.length)
				break;
			if(minval!=99999)
			finalarray.add(minval);
			markers[minindex]++;
			}
		}
	    out.write("DaatOr \n");
		out.write(query+"");
		if(finalarray.size()==0)
			out.write("\nResults: empty");
		else
			out.write("\nResults: ");		String op="";
		for(Integer d:finalarray) {
	        op+=d+" ";
	    }
		op+="\n";
		out.write(op);
		out.write("Number of documents in results: "+finalarray.size()+"\n");
		out.write("Number of comparisons: "+c+"\n");
	    out.close();		
	}
	
	public static void daatand(String query,HashMap<String,ArrayList<Integer>> hash,String path) throws IOException
	{
		/*
		 * Implementation of Daatand
		 * Like above, a 2D arraylist is used. Variable is maintained which holds the maximum value of the current elements being pointed to.
		 * An array named markers is maintained to hold the pointer index for the corresponding sub array.*/
	    Writer out=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path,true),"UTF-8"));
		String[] querylist=query.split(" ");
		int len=querylist.length;
		int arrlen,row=0,col=0,maxval=0,maxindex=0,c=0;
		ArrayList<ArrayList<Integer>> Daat = new ArrayList<ArrayList<Integer>>();
	    ArrayList<Integer> finalarray= new ArrayList<Integer>();
	    ArrayList<Integer> counterarray= new ArrayList<Integer>(); //maintains list of arrays which hold the current max value
	    if(querylist.length==1)
	    {
	    	finalarray.addAll(hash.get(querylist[0]));
	    }
	    else
	    {
	    for(String i :querylist)
		{
			Daat.add(hash.get(i));
		}
		int[] markers=new int[Daat.size()];
		int flag=0;
		int counter=0;
		maxval=Daat.get(0).get(markers[0]);
		maxindex=0;
		for(int i=0;;i++)
		{
			
			for(int j=0;j<Daat.size();j++)
			{
				if(markers[j]==Daat.get(j).size())
				{
					flag=1;
					break;
				}
				else if(Daat.get(j).get(markers[j])==maxval && (counterarray.size()==0 || checkdata(counterarray,j)))
				{
					counterarray.add(j);
					counter++;
					c++;
					if(counter==markers.length)
					{	flag=2;
			      	  break;}
				}
				else if(Daat.get(j).get(markers[j])<maxval)
				{
					markers[j]++;
					c++;
				}
				else if(Daat.get(j).get(markers[j])>maxval)
				{
					counter=1;
					counterarray.clear();
					c++;
					counterarray.add(j);
					maxval=Daat.get(j).get(markers[j]);
				}
			}
			if(flag==1)//Break loop when first array has been completely traversed
				break;
			if(flag==2)//Flag will be 2 when all the arrays hold the max value, then we will add the max value (doc id holding term t) to the final list
			{
				finalarray.add(maxval);
				counterarray.clear();
				counter=0;
				flag=0;
			for(int x=0;x<markers.length;x++)
				markers[x]++;
			}
		}
	    }
	    out.write("DaatAnd \n");
		out.write(query);
		if(finalarray.size()==0)
			out.write("\nResults: empty");
		else
			out.write("\nResults: ");		String op="";
		for(Integer d:finalarray) {
	        op+=d+" ";
	    }
		op+="\n";
		out.write(op);
		out.write("Number of documents in results: "+finalarray.size()+"\n");
		out.write("Number of comparisons: "+c+"\n");
	    out.close();
		}
	static boolean checkdata(ArrayList<Integer> counter,int j)//Used to check if the current list being traversed which holds the max value has already been accounted for
	{
		int flag=0;
		for(int i=0;i<counter.size();i++)
		{
			if(counter.get(i)!=j)
			{
				flag=1;
			}
		}
		if(counter.size()==0)
		{
			return true;
		}
		if(flag==1)
			return true;
		else
			return false;
	}
}
