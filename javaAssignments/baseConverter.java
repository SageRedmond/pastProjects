import java.util.Scanner;

public class baseConverter {   
    public static void main(String args[]){
        Scanner scan = new Scanner(System.in);
        int base1 = scan.nextInt();
        int base2 = scan.nextInt();
        String N = scan.next();
        scan.close();
        //System.out.println(N);
        System.out.println(baseToBaser(base1, base2, N));
    }
    public static String baseToBaser(int b1, int b2, String N){
        String symbols = "0123456789ABCDEF"; //index of symbol = it's value
        String result = "";
        String finalResult = "";
        int decimal = 0;
        
        //convert to decimal
        for(int i = 0, n = N.length()-1; i < N.length(); i++, n--){decimal += symbols.indexOf(N.charAt(i))*Math.pow(b1,n);}
        
        if(b2 <= 10){
            //convert to new base
            while(decimal != 0)
            {
                result += (decimal%b2);
                decimal /= b2;
            }
            //flip
            for(int i = result.length()-1; i >= 0; i--){finalResult += result.charAt(i);}
            return finalResult;
        }
        else
        {
            while(decimal != 0)
            {
                result += symbols.charAt(decimal%b2);
                decimal /= b2;
            }
            //flip
            for(int i = result.length()-1; i >= 0; i--){finalResult += result.charAt(i);}
        }
        return finalResult;
    }
}