import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
class ReadInput{
	static List<String> queries = new ArrayList<String>();
	static List<String> knowledgeBase = new ArrayList<String>();
	static int noOfQueries;
	static int KBsize;

	static void readInput() {
		String fileName = "input.txt";
		try (BufferedReader fileBufferReader = new BufferedReader(new FileReader(fileName))) {
			noOfQueries = Integer.parseInt(fileBufferReader.readLine());
			for(int i = 0; i < noOfQueries ; i++) {
				queries.add(fileBufferReader.readLine());
			}
			KBsize = Integer.parseInt(fileBufferReader.readLine());
			for(int i = 0; i < KBsize ; i++) {
				knowledgeBase.add(fileBufferReader.readLine());
			}
			fileBufferReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void writeOutput(List<String> results) {
		try {
			File file = new File("output.txt");
			FileWriter fos = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fos);
			String output = "";
			for(String s: results) {
				output = output + s + "\n";
			}
			bw.write(output.substring(0, output.length()-1));
			bw.flush();
			bw.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
class Preprocess{
	static List<String> sentences = new ArrayList<String>();
	static int noOfVariables = 0;
	public static void printKB() {
		System.out.println("Knowledge Base");
		System.out.println("---------------");
		for(String s: sentences)
			System.out.println(s);
	}

	public static String removeSpaces(String s) {
		String[] splitedArray = s.split("\\s+");
		String withoutSpaces = "";
		for(int i = 0 ;i< splitedArray.length ;i++)
			withoutSpaces = withoutSpaces + splitedArray[i];
		return withoutSpaces;
	}
	public static String replaceImplicationSign(String s) {
		s= s.replace("=>", "=");
		return s;
	}

	public static void ModifyKnowledgeBase() {
		for(int i = 0 ; i < ReadInput.knowledgeBase.size();i++) {
			String cnfString = "";
			String sentence = ReadInput.knowledgeBase.get(i);
			String senetenceWithoutSpaces = removeSpaces(sentence);
			senetenceWithoutSpaces = replaceImplicationSign(senetenceWithoutSpaces);
			if(senetenceWithoutSpaces.contains("=")) {
				cnfString = convertToCnf(senetenceWithoutSpaces);
				sentences.add(cnfString);
			}
			else
				sentences.add(senetenceWithoutSpaces);
		}
	}

	public static String convertToCnf(String s) {
		String[] sp = s.split("=");
		String res = "";
		String s1 = sp[0];
		boolean flag = false;
		for(int i = 0; i < s1.length(); i++) {
			if(s1.charAt(i)=='&') {
				res = res + "|";
			}
			else if(s.charAt(i)=='(') {
				res = res + s.charAt(i);
				flag = true;
			}
			else if(s.charAt(i)==')') {
				res = res + s.charAt(i);
				flag = false;
			}
			else if(s1.charAt(i)>='A' && s1.charAt(i)<='Z'& !flag) {
				res = res + "~" + s1.charAt(i);
				flag = true;
			}
			else
				res = res + s.charAt(i);
		}
		res = res.replace("~~", "");
		res = res + "|"+ sp[1];
		return res;
	}


	public static Hashtable<String, String> getTable(String str){
		Hashtable<String, String> store  = new Hashtable<String, String>();
		for(int i = 0; i < str.length(); i++) {
			if(str.charAt(i) == '(') {
				StringBuilder args = new StringBuilder();
				i++;
				while(str.charAt(i) != ')') {
					args.append(str.charAt(i));
					i++;
				}
				String arguments = args.toString();
				if(!arguments.contains(",")) {
					if(arguments.charAt(0)<='z' && arguments.charAt(0)>='a') {
						if(!store.containsKey(arguments)) {
							noOfVariables++;
							String subs = "r" + noOfVariables;
							store.put(arguments, subs);
						}
					}
				}
				else {
					String[] arguemntsList = arguments.split(",");
					for(String s: arguemntsList) {
						if(s.charAt(0)<='z' && s.charAt(0)>='a') {
							if(!store.containsKey(s)) {
								noOfVariables++;
								String subs = "r" + noOfVariables;
								store.put(s, subs);
							}
						}
					}
				}
			}
		}
		return store;
	}

	public static List<String> standardizedKB(){
		List<String> standard = new ArrayList<String>();
		for(String str: sentences) {
			String add = "";
			Hashtable<String, String> store = getTable(str);
			Set<String> storeKeys = store.keySet();
			String copy = str;
			for(String key: storeKeys) {
				String temp = "";
				for(int i = 0; i < copy.length() ; i++) {
					if(copy.charAt(i) != '(') {
						temp = temp + copy.charAt(i);
					}
					else {

						temp = temp + copy.charAt(i);
						i++;
						String mod = "";
						while(copy.charAt(i)!=')') {
							mod = mod + copy.charAt(i);
							i++;
						}
						temp = temp + mod.replace(key.toString(), store.get(key).toString());
						temp = temp + ")";
					}
				}
				copy = temp;
				add = temp;
			}
			if(add.equals(""))
				standard.add(copy);
			else
				standard.add(add);

		}
		return standard;
	}
}

class KnowledgeBase{
	static Hashtable<String, List<String>> KB = new Hashtable<String, List<String>>();
	static Hashtable<String, List<String>> copyKB;
	public static int iterations = 0;
	static int unifyvar = 0;
	static List<String> listOfConstants = new ArrayList<String>();
	static int level = Math.min(40, ReadInput.KBsize*3);
	public static boolean isUnifiable(List<String> queryArguments,List<String> KBArguments)
    {
		if(queryArguments.size()!=KBArguments.size())
			return false;
        int possible=0;
        int n = queryArguments.size();
        for(int i = 0; i< n; i++) {
        	char q = queryArguments.get(i).charAt(0);
        	char k = KBArguments.get(i).charAt(0);
        	if(queryArguments.get(i).equals(KBArguments.get(i)))
        		possible++;
        	else if(q >= 'a' && q<='z' && k >= 'A' && k <='Z')
        		possible++;
        	else if(q >= 'A' && q<='Z' && k >= 'a' && k <='z')
        		possible++;
        	else if(q >= 'a' && q<='z' && k >= 'a' && k <='z')
        		possible++;
        }
        if(possible!=n)
    		return false;
    	else
    		return true;
    }


	public static void addToKB(String s) {
		if(s.contains("|")) {
			String splitString[] = s.split("\\|");
			for(String split: splitString) {
				String temp[] = split.split("\\(");
				String predicate = temp[0];
				if(KB.containsKey(predicate)) {
					List<String> predList = KB.get(predicate);
					predList.add(s);
					KB.remove(predicate);
					KB.put(predicate, predList);
				}
				else {
					List<String> predList = new ArrayList<String>();
					predList.add(s);
					KB.put(predicate,predList);
				}
			}
		}
		else {
			String temp[] = s.split("\\(");
			String predicate = temp[0];
			if(KB.containsKey(predicate)) {
				List<String> predList = KB.get(predicate);
				predList.add(s);
				KB.remove(predicate);
				KB.put(predicate, predList);
			}
			else {
				List<String> predList = new ArrayList<String>();
				predList.add(s);
				KB.put(predicate,predList);
			}
		}
	}
	public static void ListToMap(List<String> newKB) {
		for(String s: newKB) {
			addToKB(s);
		}
	}

	public static String getArguments(String current) {
		String currentArguments ="";
		for(int i = 0; i < current.length(); i++) {
			if(current.charAt(i)=='(') {
				i++;
				while(current.charAt(i)!=')') {
					currentArguments += current.charAt(i);
					i++;
				}
				break;
			}
		}
		return currentArguments;
	}


	public static void getListOfConstants(List<String> newKB) {
		for(String str: newKB) {
			for(int i = 0; i < str.length(); i++) {
				String args = "";
				if(str.charAt(i)=='(') {
					i++;
					while(str.charAt(i)!=')') {
						args = args + str.charAt(i);
						i++;
					}
				String[] arguments = args.split(",");
				for(String s: arguments) {
					if(s.charAt(0)>='A' && s.charAt(0)<='Z') {
						if(!listOfConstants.contains(s))
							listOfConstants.add(s);
					}
				}
				}
			}
		}
	}


	public static List<String> getQueryArguments(int k, String query) {
		String sub = query.substring(k,query.length()-1);
		List<String> queryArgumentsList = new ArrayList<String>();
		String[] a = sub.split(",");
		for(String s: a) {
			queryArgumentsList.add(s);
		}
		return queryArgumentsList;
	}
	public static List<String> getKBArguments(String a) {
		List<String> KBArgumentsList = new ArrayList<String>();
		String[] s = a.split(",");
		for(String str: s) {
			KBArgumentsList.add(str);
		}
		return KBArgumentsList;
	}

	public static List<String> getSplittedList(String s){
		String a[] = s.split("\\|");
		List<String> atomic = new ArrayList<String>();
		for(String str: a)
			atomic.add(str);
		return atomic;
	}

	public static boolean resolveQuery(Stack<String> qst, int depth) {
		if(depth == level) {
			return false;
		}

		while(!qst.isEmpty()) {
			String query = qst.pop();
			if(!query.contains("~"))
				query = "~" + query;
			else
				query = query.substring(1);
			String predicate = "";
			int k = -1;
			for(int i = 0; i < query.length(); i++) {
				while(query.charAt(i)!='(') {
					predicate = predicate + query.charAt(i);
					i++;
				}
				k = i;
				break;
			}
			List<String> queryArguments = getQueryArguments(k+1,query);

			if(copyKB.containsKey(predicate))
			{
				List<String> predicateStrings = copyKB.get(predicate);
				for(String str: predicateStrings) {
					List<String> atomicLiterals = new ArrayList<String>();
					List<String> atomic = getSplittedList(str);

					String found= "";
					for(int i = 0; i < atomic.size(); i++) {
						atomicLiterals.add(atomic.get(i));
						if(atomic.get(i).contains(predicate))
							found = atomic.get(i);
					}
					String arguments = "";
					for(int i = 0; i < found.length(); i++) {
						if(found.charAt(i)=='(') {
							i++;
							while(found.charAt(i)!=')') {
								arguments = arguments + found.charAt(i);
								i++;
							}
							break;
						}
					}
					List<String> KBArguments = getKBArguments(arguments);
					boolean unify = isUnifiable(queryArguments, KBArguments);
					if(unify) {
						Hashtable<String, String> temp = new Hashtable<String, String>();
						for(int i = 0; i < queryArguments.size(); i++) {
							String qargs = queryArguments.get(i);
							String kargs = KBArguments.get(i);
							if(!temp.containsKey(kargs)) {
								temp.put(kargs, qargs);
							}
						}
						int n = qst.size();
						String[] queryInStack = qst.toArray(new String[n]);
						List<String> queryList = new ArrayList<String>();
						for(int i = 0; i < queryInStack.length; i++) {
							queryList.add(queryInStack[i]);
						}
						String put = "";
						for(String current: atomicLiterals) {
							Set<String> keys = temp.keySet();
							String currentArguments = getArguments(current);
							for(String key: keys) {
								if(currentArguments.contains(key))
									currentArguments = currentArguments.replace(key.toString(), temp.get(key).toString());
							}
							String search = "";
							for(int i =0 ; i< current.length();i++) {
								while(current.charAt(i)!='(') {
									search = search + current.charAt(i) + "";
									i++;
								}
								break;
							}
							String newCurrent = search + "(" + currentArguments +")";
							if(!newCurrent.equals(query)) {
								String off = "";
								if(!newCurrent.contains("~"))
									off = "~" + newCurrent;
								else
									off = newCurrent.substring(1);
								boolean flag = false;
								  for (Iterator<String> it = queryList.iterator(); it.hasNext();) {
	                                    String s = it.next();
	                                    if (s.equals(off)) {
	                                        //it.remove();
	                                        flag = true;
	                                    }
	                                }
								if(!flag) {
									if(!queryList.contains(newCurrent))
										queryList.add(newCurrent);
								}
								put = put + newCurrent +"|";
							}
						}

						Stack<String> newStack = new Stack<String>();
						for(int i = 0; i < queryList.size(); i++) {
							newStack.push(queryList.get(i));
						}
						boolean result = resolveQuery(newStack, depth + 1);
						if(result)
							return true;
					}
				}
				return false;
			}
			else
				return false;
		}
		return true;
	}

	public static List<String> sortedKB(List<String> newKB){
		List<String> sortedKB = new ArrayList<String>();
		List<String> left = new ArrayList<String>();
		for(String str: newKB) {
			if(!str.contains("|")) {
				sortedKB.add(str);
			}
			else {
				left.add(str);
			}
		}
		sortedKB.addAll(left);
		return sortedKB;
	}
	public static Hashtable<String, List<String>> copyKB(){
		Hashtable<String, List<String>> copy = new Hashtable<String, List<String>>();
		for(Map.Entry<String, List<String>> entry : KB.entrySet()) {
			List<String> li = new ArrayList<String>();
			String key = entry.getKey();
			List<String> fromKB = entry.getValue();
			for(int i = 0; i< fromKB.size(); i++) {
				li.add(fromKB.get(i));
			}
			copy.put(key, li);
		}
		return copy;
	}



	public static void resolution() {
		List<String> results = new ArrayList<String>();

		for(String query: ReadInput.queries) {
			copyKB = copyKB();
			KnowledgeBase.iterations = 0;
			Stack<String> qst = new Stack<String>();
			query = query.replace(" ", "");

			if(!query.contains("~"))
				query = "~" + query;
			else
				query = query.substring(1);
			String qp = "";
			for(int i = 0; i < query.length(); i++) {
				while(query.charAt(i)!='(') {
					qp = qp + query.charAt(i);
					i++;
				}
				break;
			}
			if(copyKB.containsKey(qp)) {
				List<String> li = copyKB.get(qp);
				li.add(query);
				copyKB.remove(qp);
				copyKB.put(qp, li);
			}
			else {
				List<String> li = new ArrayList<String>();
				li.add(query);
				copyKB.put(qp, li);
			}
			qst.push(query);
			boolean output = resolveQuery(qst,0);
			if(output)
				results.add("TRUE");
			else
				results.add("FALSE");
		}
		ReadInput.writeOutput(results);
	}
}

public class homework {
	public static void main(String[] args) {
		ReadInput.readInput();
		Preprocess.ModifyKnowledgeBase();
		List<String> newKB = Preprocess.standardizedKB();
		List<String> sortedKB = KnowledgeBase.sortedKB(newKB);
		KnowledgeBase.getListOfConstants(newKB);
		KnowledgeBase.ListToMap(sortedKB);
		KnowledgeBase.resolution();
	}
}
