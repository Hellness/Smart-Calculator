package com.company;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main{
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        String[][] variables = new String[2][100];
        int variableCounter = 0;

        Pattern invalidP = Pattern.compile("[^A-Za-z+\\-/*=()\\d\\s]");
        Pattern lengthP = Pattern.compile("\\d+|[A-Za-z]+|[+]+|[-]+|[*/]|[()]|=");
        Pattern stackableP = Pattern.compile("[+]+|[-]+");
        Pattern notStackableP = Pattern.compile("[*/]{2,}");
        Pattern variableP = Pattern.compile("[A-Za-z]+");
        Pattern withoutOperatorsP = Pattern.compile("([A-Za-z]+(\\s+)?\\d+)|(\\d+(\\s+)?[A-Za-z]+)|([A-Za-z]+\\s+[A-Za-z]+)|(\\d+\\s+\\d+)");
        Pattern withoutOperatorBracesP = Pattern.compile("(([A-Za-z]+|\\d+)\\()|(\\)([A-Za-z]+|\\d+))");

        Matcher lengthM;
        Matcher notStackableM;
        Matcher withoutOperatorsM;
        Matcher stackableM;
        Matcher invalidM;
        Matcher withoutOperatorBracesM;
        Matcher variableM;

        while(true){
            String line = scanner.nextLine();
            String result = "";

            //control if line is empty
            if(line.length()==0){
                continue;
            }
            // control the validity of commands and execute them
            int temp1 = controlCommands(line);
            if(temp1 == 0){
                break;
            }
            else if(temp1== 1){
                continue;
            }

            //control if there is an operator at the end
            if(controlWrongEnd(line)){
                continue;
            }

            //control if there is invalid repeated operators
            notStackableM = notStackableP.matcher(line);
            if(notStackableM.find()){
                System.out.println("Invalid expression");
                continue;
            }

            //control if there is something wrong with variable names
            if(controlVariableNames(variableP,line)){
                continue;
            }

            //control if there isn't an operator between expressions
            withoutOperatorsM = withoutOperatorsP.matcher(line);
            if(withoutOperatorsM.find()){
                System.out.println("Invalid expression");
                continue;
            }

            //find length
            lengthM = lengthP.matcher(line);
            int lengthCounter = 0;
            while(lengthM.find()){
                lengthCounter++;
            }

            //assign values to elements array
            String[] elements = new String[lengthCounter+2];
            lengthM = lengthP.matcher(line);
            elements[0] = "(";
            int temp2= 1;
            while(lengthM.find()){
                String tempS =line.substring(lengthM.start(),lengthM.end());
                stackableM = stackableP.matcher(tempS);
                if(stackableM.find()){
                    if(tempS.contains("+")){
                        tempS = "+";
                    }
                    else{
                        if((stackableM.end()-stackableM.start())%2==1){
                            tempS = "-";
                        }
                        else{
                            tempS = "+";
                        }
                    }
                }
                elements[temp2++] = tempS;
            }
            elements[elements.length-1] = ")";

            //control if +-+ -+- +* -*
            boolean controlContinue = false;
            for(int i=0;i<elements.length;i++){
                if(elements[i].equals("+")&&i< elements.length-2){
                    if(elements[i+1].equals("-")&&elements[i+2].equals("+")){
                        System.out.println("Invalid expression");
                        controlContinue = true;
                        break;
                    }
                }
                else if(elements[i].equals("-")&&i< elements.length-2){
                    if(elements[i+1].equals("+")&&elements[i+2].equals("-")){
                        System.out.println("Invalid expression");
                        controlContinue = true;
                        break;
                    }
                }
                if(elements[i].equals("+")&&i< elements.length-1){
                    if(elements[i + 1].equals("*") || elements[i + 1].equals("/")){
                        System.out.println("Invalid expression");
                        controlContinue = true;
                        break;
                    }
                }
                if(elements[i].equals("-")&&i< elements.length-1){
                    if(elements[i + 1].equals("*") || elements[i + 1].equals("/")){
                        System.out.println("Invalid expression");
                        controlContinue = true;
                        break;
                    }
                }
            }
            if(controlContinue){
                continue;
            }
            //correct a = -x

            //control if there are more than one equal signs
            int temp3=0;
            for (String element : elements) {
                if (element.equals("=")) {
                    temp3++;
                }
            }
            if(temp3>1){
                System.out.println("Invalid expression");
                continue;
            }

            //control if there are unwanted characters
            invalidM = invalidP.matcher(line);
            if(invalidM.find()){
                System.out.println("Invalid expression");
            }

            //control if every brace is closed correctly
            int openBracesCounter = countChar(line,'(');
            int closeBracesCounter = countChar(line,')');
            if(openBracesCounter>closeBracesCounter){
                System.out.println("Unclosed parenthesis");
                continue;
            }
            else if(closeBracesCounter>openBracesCounter){
                System.out.println("Invalid expression");
                continue;
            }
            withoutOperatorBracesM = withoutOperatorBracesP.matcher(line);
            if(withoutOperatorBracesM.find()){
                System.out.println("Invalid expression");
                continue;
            }

            //replace variables with their values
            boolean equation = false;
            for(String element:elements){
                if(element.equals("=")){
                    equation = true;
                    break;
                }
            }
            for(int i = 0;i<elements.length;i++){
                if(equation&&i==1){
                    i++;
                }
                variableM = variableP.matcher(elements[i]);
                int max =-1;
                if(variableM.matches()){
                    for(int j=0;j<variableCounter;j++){
                        if(variables[0][j].equals(elements[i])){
                            max =j;
                        }
                    }
                    if(max!=-1){
                        elements[i] = variables[1][max];
                    }
                }
            }

            // assign values to variables
            if(equation){
                variableM = variableP.matcher(elements[1]);
                if(variableM.matches()){
                    variables[0][variableCounter] = elements[1];
                    String[] elementsClone = elements.clone();
                    calculate(elementsClone,2,elements.length);
                    variables[1][variableCounter++] = elementsClone[3];
                }
                else{
                    System.out.println("Invalid expression");
                    continue;
                }
            }

            //control if there is a non-digit character except newly assigned variable
            for(int i = 0;i<elements.length;i++){
                variableM = variableP.matcher(elements[i]);
                int max =-1;
                if(variableM.matches()){
                    for(int j=0;j<variableCounter;j++){
                        if(variables[0][j].equals(elements[i])){
                            max =j;
                        }
                    }
                    if(max!=-1){
                        elements[i] = variables[1][max];
                    }
                }
            }
            boolean cond = false;
            for(String element:elements){
                variableM = variableP.matcher(element);
                if(variableM.matches()){
                    System.out.println("Invalid expression");
                    cond = true;
                    break;
                }
            }
            if(cond){
                continue;
            }

            //calculate the result
            if(!equation){
                String[] elementsClone = elements.clone();
                calculate(elementsClone,0,elements.length);
                result = elementsClone[1];
            }

            //print the result
            if(result.length()!=0){
                System.out.println(result);
            }
        }
    }
    public static int controlCommands(String line){
        String lineWithoutSpaces = line.replaceAll(" ","");
        if(lineWithoutSpaces.equals("/exit")){
            System.out.println("Bye!");
            return 0;
        }
        else if(lineWithoutSpaces.equals("/help")){
            System.out.println("The program calculates the sum of numbers");
            return 1;
        }
        else if(lineWithoutSpaces.charAt(0) =='/'){
            System.out.println("Unknown command");
            return 1;
        }
        return -1;
    }
    public static boolean controlWrongEnd(String line){
        char lastChar = line.charAt(line.length()-1);
        if(lastChar=='+'||lastChar == '-'|| lastChar == '/'|| lastChar == '*' || lastChar == '('|| lastChar == '='){
            System.out.println("Invalid expression");
            return true;
        }
        return false;
    }
    public static boolean controlVariableNames(Pattern variableP,String line){
        Matcher variableM = variableP.matcher(line);
        int whichVariable = 0;
        boolean cond1 = false;
        while(variableM.find()){
            int posBefore = variableM.start()-1;
            int posAfter = variableM.end();
            if((posBefore!=-1&&line.charAt(posBefore)>='0'&&line.charAt(posBefore)<='9')||
                    posAfter!=line.length()&&(line.charAt(posAfter)>='0'&&line.charAt(posAfter)<='9')){
                if(whichVariable == 0){
                    System.out.println("Invalid identifier");
                }
                else{
                    System.out.println("Invalid assignment");
                }
                cond1 = true;
                break;
            }
            whichVariable++;
        }
        return cond1;
    }
    public static int countChar(String str, char c) {
        int count = 0;
        for(int i=0; i < str.length(); i++)
        {    if(str.charAt(i) == c)
            count++;
        }
        return count;
    }
    public static void calculate(String[] elements,int start,int end){

        Pattern operatorP = Pattern.compile("[+\\-/*]");
        Pattern priorP = Pattern.compile("[*/]");
        Pattern normalP = Pattern.compile("[-+]");

        Matcher operatorM;
        Matcher priorM;
        Matcher normalM;

        int bracesCounter = 0;
        for (int i = start;i<end;i++) {
            if (elements[i].equals("(")) {
                bracesCounter++;
            }
        }

        if(bracesCounter>1){
            int[][] bracesPos = new int[2][bracesCounter];
            int[][] pairs = new int[bracesCounter][3];
            int ix1 = 0;
            int ix2 = 0;
            for(int i = start;i<end;i++){
                if(elements[i].equals("(")){
                    bracesPos[0][ix1++] = i;
                }
                else if(elements[i].equals(")")){
                    bracesPos[1][ix2++] = i;
                }
            }
            //(( )) | ( )( ) | (( ) ( )) | (( )) ( ) | (( ) ( )) ( )
            int[] occupiedPos = new int[bracesCounter];
            Arrays.fill(occupiedPos,-1);
            int pairsCounter = 0;
            for(int i=bracesCounter-1;i>=0;i--){
                for(int j=0;j<bracesCounter;j++) {
                    if(bracesPos[1][j]-bracesPos[0][i]>0&&isNotOccupied(occupiedPos,j,pairsCounter)){
                        pairs[pairsCounter][0] = bracesPos[0][i];
                        pairs[pairsCounter][1] = bracesPos[1][j];
                        occupiedPos[pairsCounter++] = j;
                        break;
                    }
                }
            }
            for(int i=0;i<bracesCounter;i++){
                int layer = bracesCounter;
                for(int j = pairs[i][0];j<pairs[i][1];j++){
                    if(elements[j].equals("(")){
                        layer--;
                    }
                }
                pairs[i][2] = layer;
            }
            int[] sequence = new int[bracesCounter];
            for(int i=0;i<bracesCounter;i++){
                sequence[i] = i;
            }
            for (int i = 0; i < bracesCounter - 1; i++) {
                int indic = i;
                for (int j = i; j < bracesCounter; j++){
                    if (pairs[sequence[j]][2] < pairs[sequence[indic]][2]){
                        indic = j;
                    }
                    int temp= sequence[indic];
                    sequence[indic] = sequence[i];
                    sequence[i] = temp;
                }
            }
            for(int i = bracesCounter-1;i>=0;i--){
                calculate(elements,pairs[sequence[i]][0],pairs[sequence[i]][1]);
                removeParenthesis(elements,pairs[sequence[i]][0],pairs[sequence[i]][1],bracesCounter);
            }
        }
        else{
            int operatorCounter = 0;
            for(int i = start;i<end;i++){
                operatorM = operatorP.matcher(elements[i]);
                if(operatorM.matches()){
                    operatorCounter++;
                }
            }
            if(operatorCounter==0){
                return;
            }
            int[] sequence = new int[operatorCounter];
            operatorCounter = 0;
            for(int i = start;i<end;i++){
                priorM = priorP.matcher(elements[i]);
                if(priorM.matches()){
                    sequence[operatorCounter++] = i;
                }
            }
            for(int i = start;i<end;i++){
                normalM = normalP.matcher(elements[i]);
                if(normalM.matches()){
                    sequence[operatorCounter++] = i;
                }
            }
            for(int i =0;i<operatorCounter;i++){
                switch (elements[sequence[i]]) {
                    case "/":
                        elements[sequence[i]] = Integer.toString(Integer.parseInt(elements[sequence[i]-1])/Integer.parseInt(elements[sequence[i]+1]));
                        fillElements(elements,sequence[i],start,end);
                        break;
                    case "*":
                        elements[sequence[i]] = Integer.toString(Integer.parseInt(elements[sequence[i]-1])*Integer.parseInt(elements[sequence[i]+1]));
                        fillElements(elements,sequence[i],start,end);
                        break;
                    case "+":
                        if(elements[sequence[i]-1].equals("=")||sequence[i]==1){
                            elements[sequence[i]] = Integer.toString(Integer.parseInt(elements[sequence[i]+1]));
                        }
                        else{
                            elements[sequence[i]] = Integer.toString(Integer.parseInt(elements[sequence[i]-1])+Integer.parseInt(elements[sequence[i]+1]));
                        }
                        fillElements(elements,sequence[i],start,end);
                        break;
                    case "-":
                        if(elements[sequence[i]-1].equals("=")||sequence[i]==1){
                            elements[sequence[i]] = Integer.toString(-Integer.parseInt(elements[sequence[i]+1]));
                        }
                        else{
                            elements[sequence[i]] = Integer.toString(Integer.parseInt(elements[sequence[i]-1])-Integer.parseInt(elements[sequence[i]+1]));
                        }
                        fillElements(elements,sequence[i],start,end);
                        break;
                }
            }
        }
    }
    public static void fillElements(String[] elements,int index,int start,int end){
        String result = elements[index];
        Pattern operatorP = Pattern.compile("[+\\-/*]");

        Matcher operatorM;

        for(int i=index-1;i>=start;i--){
            operatorM = operatorP.matcher(elements[i]);
            if(operatorM.matches()){
                break;
            }
            else if(elements[i].equals("(")||elements[i].equals(")")){
                elements[i] =result;
                break;
            }
            else{
                elements[i] = result;
            }
        }
        for(int i=index+1;i<=end;i++){
            if(i==elements.length){
                i--;
            }
            operatorM = operatorP.matcher(elements[i]);
            if(operatorM.matches()){
                break;
            }
            else if(elements[i].equals("(")||elements[i].equals(")")){
                elements[i] =result;
                break;
            }
            else{
                elements[i] = result;
            }
        }
    }
    public static boolean isNotOccupied(int[] occupiedPos,int j,int range){
        for(int i=0;i<range;i++){
            if(occupiedPos[i] == j){
                return false;
            }
        }
        return true;
    }
    public static void removeParenthesis(String[] elements,int start,int end,int bracesCounter){
        String result = elements[start+1];
        Pattern integerP = Pattern.compile("\\d+");
        boolean cond = true;

        for(int i=start+1;i<end;i++){
            Matcher integerM = integerP.matcher(elements[i]);
            if(!integerM.matches()){
                cond = false;
                break;
            }
        }
        if(cond&&elements[start].equals("(")&&elements[end].equals(")")){
            elements[start] = result;
            elements[end] = result;
            bracesCounter--;
            if(bracesCounter>0&&start>0&&end< elements.length-1){
                removeParenthesis(elements,--start,++end,bracesCounter);
            }
        }
    }
}