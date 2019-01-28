import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

/**
 * Tan Shi Terng Leon
 * 4000602
 * SSS.java
 */

/**
 * @author User
 *
 */
public class SSS {

	/**
	 * @param args
	 */
	public static int SIZE = 8;
	public static BigInteger t, n, s, p;
	public static Vector<BigInteger> coeff, y, x;
	
	public static void main(String[] args) {
		
		if (args.length == 1)
			SIZE = Integer.parseInt(args[0]);
		
		Scanner sc = new Scanner(System.in);
		String input;
		char option;
		
		do {
			System.out.print("Shamir Secret Sharing\n" +
			"1. Share Generation\n" +
			"2. Share Reconstruction\n" +
			"3. Exit\n" +
			"\nEnter your option: ");
			
			input = sc.nextLine();
			if (input.length() == 0)
				option = 'i';
			else
				option = input.trim().charAt(0);
				
			switch (option) {
			case '1':
				shareGeneration(sc);
				break;
			case '2':
				shareReconstruction(sc);
				break;
			case '3':
				System.out.println("Cya!");
				break;
			default:
				System.out.println("Invalid option");
			}
			System.out.println();
		} while (option != '3');
	}
	
	public static void shareReconstruction(Scanner sc) {
		int t;
		String input;
		BigInteger xValue, yValue, secret;
		
		do {
			System.out.print("Enter t: ");	//Getting t from user
			t = sc.nextInt();
			if (t <= 1)
				System.out.println("t must be greater than 1");
		} while (t <= 1);
		
		sc.nextLine();
		
		do {
			System.out.print("Enter p: ");	//Getting p from user
			
			input = sc.nextLine();
			p = new BigInteger(input);
			if (!p.isProbablePrime(80)) {
				System.out.println("p is not prime, please enter again");
			}
			else if (p.compareTo(new BigInteger(String.valueOf(t))) <= 0) {
				System.out.println("p must be greater than t (" + t + ")");
			}
		} while (!p.isProbablePrime(80) || p.compareTo(new BigInteger(String.valueOf(t))) <= 0);
		
		System.out.println();
		
		x = new Vector<BigInteger>();
		y = new Vector<BigInteger>();
		
		for (int i = 0; i < t; i++) {	//Getting the shares from user
			System.out.print("Enter x" + (i + 1) + ": ");
			input = sc.nextLine();
			xValue = new BigInteger(input);
			
			System.out.print("Enter y" + (i + 1) + ": ");
			input = sc.nextLine();
			yValue = new BigInteger(input);
			
			x.add(xValue);
			y.add(yValue);
			System.out.println();
		}
		
		secret = getSecret();	//Computes the secret
		
		System.out.println("Secret is: " + secret);
	}
	
	public static BigInteger getSecret() {
		BigInteger denom, numer;
		s = new BigInteger("0");	//Initialize s to zero
		for (int i = 0; i < y.size(); i++) {	//For each share
			numer = computeNumerator(i);
			denom = computeDenominator(i);
			s = s.add(numer.multiply(denom.modInverse(p))).mod(p);
		}
		return s;
	}
	
	public static BigInteger computeDenominator(int idx) {
		BigInteger value = new BigInteger("1");
		
		for (int i = 0; i < x.size(); i++) {
			if (i != idx)
				value = value.multiply(x.get(idx).subtract(x.get(i))).mod(p);
		}
		
		return value;
	}
	
	public static BigInteger computeNumerator(int idx) {
		BigInteger value = y.get(idx);
		
		for (int i = 0; i < x.size(); i++) {
			if (i != idx)
				value = value.multiply(x.get(i).negate()).mod(p);
		}
		
		return value;
	}
	
	public static void shareGeneration(Scanner sc) {
		Random rand = new Random();
		String input;
		
		do {	//Input t the threshold
			System.out.print("Enter t: ");
			input = sc.nextLine();
			t = new BigInteger(input);
			
			if (t.compareTo(new BigInteger("1")) <= 0)
				System.out.println("Please enter a value more than 1");
		} while (t.compareTo(new BigInteger("1")) <= 0);
		
		do {	//Input n the number of shares to generate
			System.out.print("Enter n: ");
			input = sc.nextLine();
			n = new BigInteger(input);
			
			if (n.compareTo(t) == -1)	//If n < t
				System.out.println("Please enter a value more or equal than the threshold (" + t + ")");
		} while (n.compareTo(t) == -1);
		
		s = new BigInteger(SIZE, rand);	//Generate the shared secret s
		
		System.out.println("The secret is: " + s);
		
		p = s.max(n).nextProbablePrime();	//Get a prime p > max(s,n)
		
		System.out.println("p is: " + p);
		
		coeff = new Vector<BigInteger>();	//Contains the coefficients of the equation
											//The position = the power of x
		
		coeff.add(s);	//First coefficient the constant (coefficient of x^0)
		for (int i = 0; i < t.intValue() - 1; i++) {
			coeff.add(new BigInteger(SIZE, rand).mod(p));	//Generates the rest of the coefficients
		}
		
		y = new Vector<BigInteger>();	//The y values of the shares
										//The corresponding x values are the position + 1
		
		System.out.println("The shares are:");
		
		BigInteger yValue, xValue, exp;
		
		for (int x = 1; x <= n.intValue(); x++) {	//For x = 1 to x = n
			yValue = new BigInteger("0");	//Initialize the y value to zero
			xValue = new BigInteger(String.valueOf(x));
			
			for (int i = 0; i < coeff.size(); i++) {	//Gets the corresponding y value
				exp = new BigInteger(String.valueOf(i));	//The power of the x value
				
				//Adds (ai * x^i) to the sum where ai is the coefficient of x^i
				yValue = yValue.add(coeff.get(i).multiply(xValue.modPow(exp, p))).mod(p);
			}
			
			y.add(yValue);	//Adds the y value
			System.out.println(xValue + " " + yValue);
		}
		
		sharesToFile();
		
		System.out.println("Shares stored in Shares.txt");
	}
	
	public static void sharesToFile() {
		FileWriter fw;
		try {
			fw = new FileWriter(new File("Shares.txt"));
			fw.write("Secret is " + s + "\n" +
				"p is " + p + "\n" +
				"t is " + t + "\n" +
				"n is " + n + "\n" +
				"Equation is:\n" + s);
			for (int i = 1; i < coeff.size(); i++) {
				fw.write(" + " + coeff.get(i) + "x^" + i);
			}
			fw.write("\nThe shares are:\n");
			for (int i = 0; i < y.size(); i++) {
				fw.write((i + 1) + "," + y.get(i) + "\n");
				
			}
		
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
